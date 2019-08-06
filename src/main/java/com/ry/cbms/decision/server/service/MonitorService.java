package com.ry.cbms.decision.server.service;

import com.ry.cbms.decision.server.dto.SimpleValData;

import java.util.List;
import java.util.Map;

public interface MonitorService {
    List<String> getAllVal();

    Map<String,Object> getValPrice(List<String> symbol,int pageNum,int pageSize);

    Map<String,Object> getSingleVal(String symbol);

    String accordPush(String type);

    String accordPushTrade(String type);
}
