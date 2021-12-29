package com.ware.listener;

import com.common.to.mq.OrderTo;
import com.common.to.mq.StockLockedTo;
import com.rabbitmq.client.Channel;
import com.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * user:lufei
 * DATE:2021/12/28
 **/
@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;


    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        try {
            wareSkuService.handleStockLockedRelease(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    @RabbitHandler
    public void handleOrderClosedRelease(OrderTo order,Message message, Channel channel) throws IOException {
        System.out.println("订单关闭，解锁库存");
        try {
            wareSkuService.handleStockLockedRelease(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
