package com.ry.cbms.decision.server.config;

import com.ry.cbms.decision.server.vo.Mt4Vo;
import com.ry.cbms.decision.server.schedule.AutoTask;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.Mt4LogInUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/5/27 15:54
 * @Description 开机启动
 */
@Component
@Slf4j
public class MyStartupRunner implements CommandLineRunner {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Mt4LogInUtil mt4LogInUtil;

    @Autowired
    private AutoTask autoTask;

    @Override
    public void run(String... args) {
        redisTemplate.delete (Constants.PREFIX + Constants.MT4_TOKEN);
        loginMt4 ();//登陆Mt4
      //  loginTerminus ();//登陆terminus
        autoTask.loadHomePageData ();//开机加载首页数据
       // mt4LogInUtil.refreshHeartBeat ();
    }

    public void loginMt4() {
        log.info ("开始登陆Mt4{}", new Date ().toString ());
        Mt4Vo mt4Vo = new Mt4Vo ();
        mt4Vo.setServer (Constants.Mt4LoginServer);
        mt4Vo.setUsername (Constants.Mt4LoginUserName);
        mt4Vo.setPassword (Constants.Mt4LoginPassword);
//        mt4Vo.setServer (Constants.MT4LoginServerReal);
//        mt4Vo.setUsername (Constants.Mt4LoginUserNameReal);
//        mt4Vo.setPassword (Constants.Mt4LoginPasswordReal);
        mt4Vo.setServerId (Constants.Mt4ServerId);
         mt4LogInUtil.logInMT4 (mt4Vo);
        log.info ("结束登陆Mt4{}", new Date ().toString ());
    }

    public void loginTerminus() {
        log.info ("开始登陆Terminus{}", new Date ().toString ());
        mt4LogInUtil.terminusLogIn ();
        log.info ("结束登陆Terminus{}", new Date ().toString ());
    }
}
