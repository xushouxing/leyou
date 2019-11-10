package com.leyou.order.service;

import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.myenum.OrderStatusEnum;
import com.leyou.common.utils.IdWorker;
import com.leyou.order.config.LoginInterceptor;
import com.leyou.order.config.PayHelper;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.fenigClient.GoodsClient;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.myenum.PayStateEnum;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.pojo.Sku;
import com.leyou.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private PayHelper payHelper;

    /**
     * 创建订单
     *
     * @param orderDTO
     * @return
     */
    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        // 新增订单表
        //订单号
        long orderId = idWorker.nextId();
        Order order = new Order();
        order.setOrderId(orderId);
        //用户信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        order.setBuyerNick(userInfo.getUsername());
        order.setUserId(userInfo.getId());
        order.setBuyerRate(false);
        //收货人信息
        order.setReceiver("虎哥");
        order.setReceiverAddress("东湖路");
        order.setReceiverCity("武汉");
        order.setReceiverDistrict("武昌");
        order.setReceiverMobile("12993302002");
        order.setReceiverState("湖北");
        order.setReceiverZip("4302032");
        //金额
        List<CartDTO> carts = orderDTO.getCarts();
        Map<Long, Integer> Cartmap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        Set<Long> longs = Cartmap.keySet();
        ArrayList<Long> ids = new ArrayList<>(longs);
        List<Sku> skus = goodsClient.querySkuBySkuId(ids);
        long totalPay = 0L;
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Sku sku : skus) {
            //查询总价格
            totalPay += sku.getPrice() * Cartmap.get(sku.getId());
            // 订单详情
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setNum(Cartmap.get(sku.getId()));
            orderDetails.add(orderDetail);
        }
        order.setTotalPay(totalPay);
        order.setActualPay(totalPay - order.getPostFee());
        //将订单详情写入数据库
        int i = orderDetailMapper.insertList(orderDetails);
        if (i != orderDetails.size()) {
            log.error("订单详情写入失败");
            throw new LyException(ExceptionEnum.ORDERDTEAIL_FAIL);
        }
        //支付方式
        order.setPaymentType(orderDTO.getPaymentType());
        //创建时间
        order.setCreateTime(new Date());
        //将订单写入数据库
        int i1 = orderMapper.insertSelective(order);
        if (i1 != 0) {
            log.error("订单详情写入失败");
            throw new LyException(ExceptionEnum.ORDER_FAIL);
        }
        // 订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.code());
        orderStatus.setCreateTime(new Date());
        order.setOrderId(orderId);
        orderStatusMapper.insertSelective(orderStatus);
        // 减库存
        goodsClient.deleteStock(orderDTO.getCarts());
        return orderId;
    }

    /**
     * 查询订单
     *
     * @param id
     * @return
     */
    public Order queryOrderByOrderId(Long id) {
        //查询order
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_FAIL);
        }
        //查询订单详情
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> select = orderDetailMapper.select(orderDetail);
        if (CollectionUtils.isEmpty(select)) {
            throw new LyException(ExceptionEnum.ORDERDTEAIL_FAIL);
        }
        order.setOrderDetails(select);
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDERSTATUS_FAIL);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    /**
     * 生成订单url
     *
     * @param id
     * @return
     */
    public String createPayUrl(Long id) {
        Order order = queryOrderByOrderId(id);
        Integer status = order.getOrderStatus().getStatus();
        if (OrderStatusEnum.UN_PAY.code() != status) {
            throw new RuntimeException();
        }
        String desc = order.getOrderDetails().get(0).getTitle();
        String url = payHelper.createOrder(id, order.getTotalPay(), desc);
        return url;
    }

    /**
     * 处理微信回调
     *
     * @param data
     */
    public void handleNotify(Map<String, String> data) {
        //判断通信和业务是否成功
        payHelper.isSuccess(data);
        //检验签名
        payHelper.isValidSign(data);
        //检验金额
        String total_fee = data.get("total_fee");
        //订单号
        String out_trade_no = data.get("out_trade_no");
        if (StringUtils.isBlank(total_fee) || StringUtils.isBlank(out_trade_no)) {
            throw new LyException(ExceptionEnum.ORDER_PARAM_INVALID);
        }
        Order order = orderMapper.selectByPrimaryKey(Long.valueOf(out_trade_no));
        if (Long.valueOf(total_fee) != order.getActualPay()) {
            throw new LyException(ExceptionEnum.ORDER_MONEY_INVALID);
        }
        OrderStatus orderStatus1 = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
        if (orderStatus1.getStatus() == OrderStatusEnum.UN_PAY.code()) {
            //修改订单状态
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setOrderId(order.getOrderId());
            orderStatus.setPaymentTime(new Date());
            orderStatus.setStatus(OrderStatusEnum.PAYED.code());
            int i = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
            if (i != 1) {
                throw new LyException(ExceptionEnum.ORDER_STATUS_UPDATE_FAIL);
            }
        }
    }

    /**
     * 向微信查询订单状态
     *
     * @param id
     * @return
     */
    public PayStateEnum queryOrderStatus(Long id) {
        //查询数据库中订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        Integer status = orderStatus.getStatus();
        if (status == PayStateEnum.SUCCESS.getValue()) {
            //已付款
            return PayStateEnum.SUCCESS;
        }
        //状态表中状态不为1.不一定是为支付，可能已支付但微信还没来得及返回...
        PayStateEnum payStateEnum = payHelper.queryOrderStatus(id);  //向微信查询状态
        return payStateEnum;
    }
}
