package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.model.OrderDetail;
import com.ry.cbms.decision.server.model.ThrowInfo;
import com.ry.cbms.decision.server.vo.OrderDetailVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/6/11 10:57
 * @Description 订单信息相关
 */
@Mapper
public interface OrderInfoDao {

    @Select("select clearAcc,clearAccOrderId,throwProfit from decision.throw_info where orderId=#{orderId}")
    ThrowInfo getByOrderId(@Param("orderId") String orderId);

    ThrowInfo getThrowInfoHisSum(@Param("account") String account);//获取抛单历史累计

    List<ThrowInfo> getThrowInfoDataByDay(@Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);

    ThrowInfo getThrowInfoVolume(@Param("startDate") String startDate, @Param("endDate") String endDate); //抛单交易量


    List<ThrowInfo> getThrowInfoVolumeByDay(@Param("startDate") String startDate, @Param("endDate") String endDate); //抛单交易量

    List<ThrowInfo> getThrowInfoVolumeByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);//抛单交易量

    List<ThrowInfo> getTotalThrowInfoVolumeByDay(@Param("startDate") String startDate, @Param("endDate") String endDate); //非抛单交易量

   ThrowInfo getTotalThrowInfoVolume(@Param("startDate") String startDate, @Param("endDate") String endDate); //非抛单交易量



    List<ThrowInfo> getTotalThrowInfoVolumeByMonth(@Param("startDate") String startDate, @Param("endDate") String endDate);//非抛单交易量

    List<ThrowInfo> getThrowInfoDataByWeek(@Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);


    List<ThrowInfo> getThrowInfoDataByMonth(@Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Select("select sum(a.amount) from cbms.detail_closeprice_commission a,cbms.detail_closeprice b where  b.orderid=#{orderId} and a.closeId=b.id ")
    BigDecimal getThrowCommByOrderId(@Param("orderId") String orderId);//获每笔单返佣


    void save(@Param("infos") List<ThrowInfo> infos);

    void saveOrderDetail(@Param("orderDetailList") List<OrderDetail> orderDetailList);

    void deleteOrderDetail(@Param("customerOrder") String customerOrder);


    Integer getOrderDetailCount(@Param("account") String account, @Param("orderFlag") String orderFlag, @Param("startDate") String startDate, @Param("endDate") String endDate);


    List<HashMap> getOrderDetailByCondition(@Param("account") String account, @Param("orderFlag") String orderFlag, @Param("startDate") String startDate, @Param("endDate") String endDate, int offset, int limit);

    Map<String, Object> getOrderDetailSum(@Param("account") String account);

    @Select("select * from order_detail where account=#{account}")
    List<OrderDetailVo> getOrderRecordByAccount(@Param("account") String account);

    List<OrderDetailVo> getOrderRecordByAccountAndDate(@Param("account") String account,@Param("startDate") String startDate, @Param("endDate") String endDate);



}
