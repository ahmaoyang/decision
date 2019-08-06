package com.ry.cbms.decision.server.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;


/**
 * @ClassName WebSocketConfig
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/5 13:52
 * @Version 1.0
 **/
@Configuration
@Component
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}