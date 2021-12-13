package com.order.service.impl;

import com.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.AMQImpl;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.utils.PageUtils;
import com.common.utils.Query;

import com.order.dao.OrderItemDao;
import com.order.entity.OrderItemEntity;
import com.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    // queues: 声明需要监听的队列
    @RabbitListener(queues = {"hello.java.queue"})
    public void receiveMessage(Message obj, OrderReturnReasonEntity content, Channel channel) {
        byte[] body = obj.getBody();
        MessageProperties messageProperties = obj.getMessageProperties();
        System.out.println("接收到消息..."+obj);
        long deliveryTag = messageProperties.getDeliveryTag();
        try {
            // 收货
            channel.basicAck(deliveryTag,false);
            // 退货
            channel.basicNack(deliveryTag,false,false);
            channel.basicReject(deliveryTag,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
