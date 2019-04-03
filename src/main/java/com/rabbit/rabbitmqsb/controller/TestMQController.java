package com.rabbit.rabbitmqsb.controller;

import com.rabbit.rabbitmqsb.common.MQMsg;
import com.rabbit.rabbitmqsb.config.CallBackerSender;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestMQController implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private CallBackerSender callBackerSender;

    @RequestMapping("hellorabbitmq")
    public void send() {
        String context = "hello rabbitmq";
        this.amqpTemplate.convertAndSend("hello", context);
    }

    @RequestMapping("mysql")
    public void send1() {
        String context = "保存到数据库";
        this.amqpTemplate.convertAndSend(MQMsg.EXCHANGE, "test.msq", context);
    }

    @RequestMapping("email")
    public void send2() {
        String context = "为用户发送邮件";
        this.amqpTemplate.convertAndSend(MQMsg.EXCHANGE, "test.eml", context);
    }

    @RequestMapping("other")
    public void send3() {
        String context = "发送到其他";
        this.amqpTemplate.convertAndSend(MQMsg.EXCHANGE, "test.#", context);
    }


    @RequestMapping("callback")
    public void send4() {
        String context = "测试发送成功回掉";
        callBackerSender.send("test.eml",context);
    }

    /**
     * 如果消息没有到exchange,则confirm回调,ack=false
     * 如果消息到达exchange,则confirm回调,ack=true
     *
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        System.out.println("消息id:" + correlationData.getId());
        if (ack) {
            System.out.println("消息发送确认成功");
        } else {
            System.out.println("消息发送确认失败:" + cause);
        }
    }

    /**
     *
     * exchange到queue成功,则不回调return
     * exchange到queue失败,则回调return(需设置mandatory=true,否则不回回调,消息就丢了)
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("return--message:" + new String(message.getBody()) + ",replyCode:" + replyCode
                + ",replyText:" + replyText + ",exchange:" + exchange + ",routingKey:" + routingKey);
    }

}
