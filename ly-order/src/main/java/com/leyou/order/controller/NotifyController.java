package com.leyou.order.controller;

import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wxpay")
public class NotifyController {
    @Autowired
    private OrderService orderService;

    /**
     * 处理微信回调结果通知
     * @param data
     * @return
     */
    @PostMapping(value = "notify",produces = "application/xml")
    public  Map<String,String> notify(@RequestBody Map<String,String> data){
          orderService.handleNotify(data);
          Map<String,String> map=new HashMap<>();
          map.put("return_code","SUCCESS");
          map.put("return_msg","OK");
          return map;
    }
}
