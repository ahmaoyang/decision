package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName MonitorController
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/5 13:47
 * @Version 1.0
 **/
@Api(tags = "事件监控")
@RestController
@RequestMapping("/monitor")
public class MonitorController {


    @Autowired
    WebSocketServer webSocketServer;
    @Autowired
    MonitorService monitorService;

    @GetMapping("/valPrice")
    @ApiOperation(value = "具体品种价格")
    public void getSpercialVal(@ApiParam("品种类型") @RequestParam(value = "symbol", required = false) String symbol) {
    /*    try {
            List<String> allVal = monitorService.getAllVal();
            Map<String, Object> singlePriceMap = monitorService.getSinglePrice(allVal);
            String resultJson = JSON.toJSONString(singlePriceMap);
            webSocketServer.sendMessage(resultJson);
        } catch (IOException e) {
            throw new GlobalException("主动推送消息失败");
        }*/

     // webSocketServer.sendMessage();
    }
}
