package com.ry.cbms.decision.server.schedule;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.utils.Mt4LogInUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * @Author maoYang
 * @Date 2019/5/27 9:28
 * @Description 涮新mt4心跳数据
 */
@Slf4j
@Service
public class RefreshMt4Token {
    @Autowired
    private Mt4LogInUtil mt4LogInUtil;

    /**
     * 20s 一次
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void refreshHeartBeat() {
        try {
            mt4LogInUtil.refreshHeartBeat ();
        } catch (Exception e) {
            try {
                mt4LogInUtil.refreshHeartBeat ();
            } catch (Exception ex) {
                throw new GlobalException ("mt4 服务连接失败" + ex.toString ());
            }
        }
    }


}
