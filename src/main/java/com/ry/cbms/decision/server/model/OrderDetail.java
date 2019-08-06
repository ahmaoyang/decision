package com.ry.cbms.decision.server.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author maoYang
 * @Date 2019/7/10 15:10
 * @Description 定单明细
 */
@Data
public class OrderDetail implements Serializable {
    private static final long serialVersionUID = -4401913568806243090L;

    private Long id;
    private String account;//账号

    private String customerOrderProfit;//客户订单盈亏

    private String clearAccProfit;//清算账户盈亏

    private String clearAccOrderNo;//清算账户订单号

    private String fee;//手续费

    private String brokerProfit;//经济商盈亏

    private Date createTime;//创建时间

    private String returnComm;//返佣

    private String customerOrder;//客户订单号

    private Boolean isRed;//存在问题账单


}
