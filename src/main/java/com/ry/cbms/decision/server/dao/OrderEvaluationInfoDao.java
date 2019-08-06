package com.ry.cbms.decision.server.dao;

import com.ry.cbms.decision.server.model.OrderEvaluationInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;


/**
 * @Author maoYang
 * @Date 2019/6/10 14:53
 * @Description 订单评估
 */
@Mapper
public interface OrderEvaluationInfoDao {

    int save(@Param("evaluations") List<OrderEvaluationInfo> evaluations);

    @Delete("delete from decision.order_evaluation_info where id >0")
    int deleteAll();

    List<OrderEvaluationInfo> getAllOrderEvaluationInfo(@Param("offset") Integer offset, @Param("limit") Integer limit, @Param("account") String account);

    Integer getAllOrderEvaluationCount(@Param("account") String account);

    List<OrderEvaluationInfo> getOrderDataByHour(@Param("variety") String variety, @Param("orderType") String orderType, @Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);//以小时为颗粒度

    List<OrderEvaluationInfo> getOrderDataByDay(@Param("variety") String variety, @Param("orderType") String orderType, @Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);//以天为颗粒度

    List<OrderEvaluationInfo> getOrderDataByWeek(@Param("variety") String variety, @Param("orderType") String orderType, @Param("account") String account, @Param("startDate") String startDate, @Param("endDate") String endDate);//以周为颗粒度

    List<OrderEvaluationInfo> getOrderByCondition(@Param("variety") String variety, @Param("account") String account);


}
