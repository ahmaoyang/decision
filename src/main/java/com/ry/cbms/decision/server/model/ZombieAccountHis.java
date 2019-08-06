package com.ry.cbms.decision.server.model;

import lombok.Data;

/**
 * @Author maoYang
 * @Date 2019/5/15 17:00
 * @Description 僵尸账户历史
 */
@Data
public class ZombieAccountHis  extends  BaseEntity<Integer>{
 private String account;

 private String remark;

}
