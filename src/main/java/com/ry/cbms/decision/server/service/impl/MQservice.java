package com.ry.cbms.decision.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.controller.WebSocketServer;
import com.ry.cbms.decision.server.dto.*;
import com.ry.cbms.decision.server.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import java.util.List;

/**
 * @ClassName MQservice
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/8 16:17
 * @Version 1.0
 **/
//@Component
@Service
@Slf4j
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling
public class MQservice {


    @Autowired
    WebSocketServer webSocketServer;

    @Autowired
    MonitorService monitorService;

    private List<String> removeDuplicationList = new ArrayList();
    /**
     * @return void
     * @Author XTH
     * @Description //TODO 从MQ接受到实时品种价格消息并通过websocket推送给前端
     * @Date 2019/7/9 17:23
     * @Param [text]
     **/
    @JmsListener(destination = "SingleValQue")
    public void receiveQueue(String text) {
        System.out.println(text+"为消息队列推送的品种价格");
        if(text.equals(removeDuplicationList.get(0))){

            return;
        }
        removeDuplicationList.clear();
        SingleValData singleValData = JSON.parseObject(text, SingleValData.class);
        SingleFormData singleFormData = new SingleFormData();
        singleFormData.setType("singleForm");
        SimpleValData simpleValData = new SimpleValData();
        simpleValData.setAsk(Double.toString(singleValData.getAsk()));
        simpleValData.setBid(Double.toString(singleValData.getBid()));
        simpleValData.setDirection(Integer.toString(singleValData.getDirection()));
        simpleValData.setSymbol(singleValData.getSymbol());
        singleFormData.setSimpleValData(simpleValData);
        Result<SingleFormData> success = Result.success(singleFormData);
        String resultStr = JSON.toJSONString(success);
        try {
            webSocketServer.sendInfo4table(resultStr);
            removeDuplicationList.add(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return void
     * @Author XTH
     * @Description //TODO 推送其它消息的方式为定时任务
     * @Date 2019/7/11 10:44
     * @Param []
     **/
    @Scheduled(cron = "0/5 * * * * ?")
    public void configureTasks() {
        webSocketServer.sendInfo();
    }
}
