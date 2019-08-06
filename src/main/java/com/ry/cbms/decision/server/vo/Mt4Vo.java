package com.ry.cbms.decision.server.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author maoYang
 * @Date 2019/5/24 16:37
 * @Description TODO
 */
@Data
@ApiModel
public class Mt4Vo {
    private String server;

    private String username;

    private String password;

    private String serverId;
}
