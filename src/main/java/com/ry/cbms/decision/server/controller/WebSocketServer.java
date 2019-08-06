package com.ry.cbms.decision.server.controller;


import com.alibaba.fastjson.JSON;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.dto.MonitorParam;
import com.ry.cbms.decision.server.dto.Pages;
import com.ry.cbms.decision.server.dto.ResultSymbols;
import com.ry.cbms.decision.server.dto.SimpleValData;
import com.ry.cbms.decision.server.service.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * @ClassName WebSocketServer
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/3 15:28
 * @Version 1.0
 **/
@ServerEndpoint("/monitorWebSocket/{sid}/{type}")
@Component
@Slf4j
@EnableScheduling
public class WebSocketServer {

    private static MonitorService monitorService;

    @Autowired
    public WebSocketServer(MonitorService monitorService) {
        this.monitorService = monitorService;
    }

    public WebSocketServer() {
    }

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    //接收sid
    private String sid;

    private String type;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid, @PathParam("type") String type) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        log.info("有新窗口开始监听:" + sid + ",当前在线数为" + getOnlineCount());
        this.sid = sid;
        try {
            if (null != type && !type.isEmpty()) {
                this.type = type;
                System.out.println("type为：" + type);
            } else {
                String symbol = sendpageVal();
                charPush(symbol);
                tradePush(symbol);

            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("收到前端" + sid + "的信息:" + message);

        //群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                hanlderMessage(message, item);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    /**
     * @return void
     * @Author XTH
     * @Description //TODO 处理客户端发送过来的请求
     * @Date 2019/7/9 18:10
     * @Param [message]
     **/
    public void hanlderMessage(String message, WebSocketServer item) {
        try {
            MonitorParam monitorParam = JSON.parseObject(message, MonitorParam.class);
            if ("page".equals(monitorParam.getType())) {
                List<String> allVal = monitorService.getAllVal();
                Map result = monitorService.getValPrice(allVal, monitorParam.getPageNum(), monitorParam.getPageSize());
                List<SimpleValData> valsPrice = (List)result.get("valsPrice");
                Pages pageInfo = (Pages)result.get("pageInfo");
                ResultSymbols resultSymbols = new ResultSymbols();
                resultSymbols.setSymbolList(valsPrice);
                resultSymbols.setPageInfo(pageInfo);
                resultSymbols.setType("page");
                Result<ResultSymbols> success = Result.success(resultSymbols);
                if(!allVal.isEmpty() && !valsPrice.isEmpty()){
                    item.sendMessage(JSON.toJSONString(success));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * @return void
     * @Author XTH
     * @Description //TODO  推送首页一个默认品种近6小时的数据
     * @Date 2019/7/15 14:35
     * @Param [symbol]
     **/
    public void charPush(String symbol)  {
        try {
            String str = monitorService.accordPush(symbol);
            if (!str.isEmpty()) {
                sendMessage(str);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * @return void
     * @Author XTH
     * @Description //TODO 推送首页一个默认品种交易价格的交易数量
     * @Date 2019/7/15 14:37
     * @Param [symbol]
     **/
    public void tradePush(String symbol) throws IOException {
        try {
            String str = monitorService.accordPushTrade(symbol);
            if (!str.isEmpty()) {
                sendMessage(str);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        synchronized (session) {
            this.session.getBasicRemote().sendText(message);
        }
    }

    public void pushMessage(String message) {
        try {
            if (this.session.isOpen()) {
                this.session.getAsyncRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 返回list中第一个交易品种用于默认展示
     * @Author XTH
     * @Description //TODO 第一次请求默认发送第一页，一共7行数据
     * @Date 2019/7/10 16:28
     * @Param []
     **/
    public String sendpageVal() throws IOException {
        try {
            List<String> allVal = monitorService.getAllVal();
            Map valPriceList = monitorService.getValPrice(allVal, 1, 7);
            List<SimpleValData> valsPrice = (List)valPriceList.get("valsPrice");
            Pages pages=(Pages)valPriceList.get("pageInfo");
            ResultSymbols symbols = new ResultSymbols();
            symbols.setPageInfo(pages);
            symbols.setSymbolList(valsPrice);
            symbols.setType("page");
            Result<ResultSymbols> success = Result.success(symbols);
            if(!valPriceList.isEmpty()&& !allVal.isEmpty()){
                sendMessage(JSON.toJSONString(success));
                return valsPrice.get(0).getSymbol();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return "";
    }


    /**
     * 群发自定义消息
     */
    public static void sendInfo() {
        for (WebSocketServer item : webSocketSet) {
           // System.out.println("websocket里面的sid" + item.sid);
            try {
                if (item.type == null || item.type.isEmpty()) {
                } else {
                    String singlePriceStr = monitorService.accordPush(item.type);
                    String tradeCountStr = monitorService.accordPushTrade(item.type);
                    if (!singlePriceStr.isEmpty()) {
                        item.sendMessage(singlePriceStr);
                    }
                    if (!tradeCountStr.isEmpty()) {
                        item.sendMessage(tradeCountStr);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                continue;
            }
        }
    }

    /**
     * 群发自定义消息
     */
    public static void sendInfo4table(String message) {
        for (WebSocketServer item : webSocketSet) {
           // System.out.println("websocket里面的sid" + item.sid);
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if (item.type == null || item.type.isEmpty()) {
                    item.sendMessage(message);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                continue;
            }
        }
    }


    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }


}