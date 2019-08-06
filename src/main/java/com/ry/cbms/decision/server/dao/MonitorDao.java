package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.dto.CharData;
import com.ry.cbms.decision.server.dto.SimpleValData;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component("MonitorDao")
public interface MonitorDao {

List<SimpleValData> getValsPrice(List<String> symbolList);

    List<CharData> getSinglePrice(String symbol);
}
