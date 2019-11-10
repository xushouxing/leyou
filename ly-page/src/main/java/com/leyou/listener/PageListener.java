package com.leyou.listener;

import com.leyou.service.GoodsHtmlService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PageListener {
    @Autowired
    private GoodsHtmlService goodsHtmlService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "leyou.create.web.queue", durable = "true"),
            exchange = @Exchange(
                    value = "leyou.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void listener(Long id){
     if (id==null || id.intValue()<0){
         return;
     }
            goodsHtmlService.createHtml(id);
    }
}
