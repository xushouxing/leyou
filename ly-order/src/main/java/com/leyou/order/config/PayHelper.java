package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.exception.LyException;
import com.leyou.common.myenum.ExceptionEnum;
import com.leyou.common.myenum.OrderStatusEnum;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.myenum.PayStateEnum;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PayHelper {
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private PayConfig payConfig;
    @Autowired
    private WXPay wxPay;
    public String createOrder(Long orderId,Long totalPay,String desc){
        Map<String, String> data = new HashMap<String, String>();
        data.put("body", desc);  //商品描述
        data.put("out_trade_no", orderId.toString());  //订单号
        //自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
      //  data.put("device_info", "");
        //标价币种
     //   data.put("fee_type", "CNY");
        data.put("total_fee", totalPay.toString()); //支付金额 单位分
        data.put("spbill_create_ip", "123.12.12.123");  //调用微信接口的电脑ip
        data.put("notify_url", "http://www.example.com/wxpay/notify");//微信回调地址。外网可访问的url
        data.put("trade_type", "NATIVE");  // 此处指定为扫码支付
        //trade_type=NATIVE时，此参数必传。此参数为二维码中包含的商品ID，商户自行定义
     //   data.put("product_id", "12");

        try {
            Map<String, String> resp = wxPay.unifiedOrder(data);
             isSuccess(resp);
             isValidSign(resp);
             return resp.get("code_url");

        } catch (Exception e) {
            log.error("[微信下单]创建支付订单异常失败"+e);
            return null;
        }
    }

    /**
     * 判断通信和业务标识
     * @param resp
     * @return
     */
    public void isSuccess(Map<String, String> resp) {
        //判断通信结果
        String return_code = resp.get("return_code");
        if(WXPayConstants.FAIL.equals(return_code)){
            log.error("微信下单通信异常{}"+resp.get("return_msg"));
            throw new LyException(ExceptionEnum.WXPAY_FAIL);
        }
        //判断业务结果
        String result_code = resp.get("result_code");
        if(WXPayConstants.FAIL.equals(result_code)){
            log.error("微信下单通信异常{}"+resp.get("err_code_des"));
            throw new LyException(ExceptionEnum.WXPAY_FAIL);
        }
    }

    /**
     * 检验签名
     * @param reqData
     */
    public void isValidSign(Map<String,String> reqData){
        try {
            //生成签名，并不知微信传过来的签名是哪种类型。两个签名类型都做判断
            String sing = WXPayUtil.generateSignature(reqData, payConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
            String sing1 = WXPayUtil.generateSignature(reqData, payConfig.getKey(), WXPayConstants.SignType.MD5);
            //获取微信传过来的签名
            String sign = reqData.get("sign");
            if(!StringUtils.equals(sign,sing) || !StringUtils.equals(sign,sing1)){
                //签名有误
                throw new LyException(ExceptionEnum.SING_FAIL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.SING_FAIL);
        }
    }

    /**
     * 向微信查询订单支付状态
     * @param id
     * @return
     */
    public PayStateEnum queryOrderStatus(Long id) {
        Map<String,String> data=new HashMap<>();
        data.put("out_trade_no",id.toString());
        try {
            Map<String, String> refund = wxPay.refund(data);
            //校验状态
            isSuccess(refund);
            //校验签名
            isValidSign(refund);
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
            /**
             * SUCCESS—支付成功
             *
             * REFUND—转入退款
             *
             * NOTPAY—未支付
             *
             * CLOSED—已关闭
             *
             * REVOKED—已撤销（付款码支付）
             *
             * USERPAYING--用户支付中（付款码支付）
             *
             * PAYERROR--支付失败(其他原因，如银行返回失败)
             *
             * 支付状态机请见下单API页面
             */
            OrderStatus orderStatus1 = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
            String trade_state = refund.get("trade_state");
            Boolean boo=orderStatus1.getStatus() == OrderStatusEnum.UN_PAY.code();
            if (trade_state.equals("SUCCESS") && boo){
                //修改订单状态
                OrderStatus orderStatus = new OrderStatus();
                orderStatus.setOrderId(order.getOrderId());
                orderStatus.setPaymentTime(new Date());
                orderStatus.setStatus(OrderStatusEnum.PAYED.code());
                int i = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
                if (i != 1) {
                    throw new LyException(ExceptionEnum.ORDER_STATUS_UPDATE_FAIL);
                }
                return PayStateEnum.SUCCESS;
            }
            if(trade_state.equals("USERPAYING")||trade_state.equals("NOTPAY")){
                return PayStateEnum.NOT_PAY;
            }
            return PayStateEnum.FAIL;
        } catch (Exception e) {
            e.printStackTrace();
            return PayStateEnum.NOT_PAY;
        }
    }
}
