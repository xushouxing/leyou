package com.leyou.order.controller;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        Long orderId=orderService.createOrder(orderDTO);
        return ResponseEntity.ok(orderId);
    }

    /**
     * 查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderByOrderId(@PathVariable("id")Long id){
        Order order=orderService.queryOrderByOrderId(id);
        return ResponseEntity.ok(order);
    }

    /**
     * 生成微信支付链接
     * @param id
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id")Long id){
         return ResponseEntity.ok(orderService.createPayUrl(id));
    }

    /**
     * 主动向微信查询订单状态
     * @param id
     * @return
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderStatus(@PathVariable("id")Long id){
       Integer code=orderService.queryOrderStatus(id).getValue();
       return ResponseEntity.ok(code);
    }
}
