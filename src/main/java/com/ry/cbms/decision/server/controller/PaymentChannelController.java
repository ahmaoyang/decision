package com.ry.cbms.decision.server.controller;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.dao.AccessGoldCheckDao;
import com.ry.cbms.decision.server.utils.*;
import com.ry.cbms.decision.server.vo.BaseRequestVo;
import com.ry.cbms.decision.server.model.AccessGoldCheck;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.service.PaymentChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author maoYang
 * @Date 2019/5/17 15:24
 * @Description 支付通道相关接口
 */
@Api(tags = "支付通道对账")
@RestController
@RequestMapping("/check")
@Slf4j
public class PaymentChannelController {
    @Autowired
    private PaymentChannelService paymentChannelService;
    @Autowired
    private AccessGoldCheckDao accessGoldCheckDao;

    @LogAnnotation
    @PostMapping("/uploadCheckBill")
    @ApiOperation(value = "对账单上传")
    public Result uploadCheckBill(MultipartFile file) {
        if (null == file) {
            return Result.error ("上传的文件不能为空");
        }
        Result result = paymentChannelService.uploadCheckBill (file);
        return result;
    }


    @RequestMapping(value = "/downloadCheckBill",produces="application/json;charset=UTF-8",method = RequestMethod.GET)
    @ApiOperation(value = "下载对账单模板")
    public void downloadCheckBill(HttpServletRequest request,HttpServletResponse response){
        ExcelUtil.download (request,response,Constants.checkModulePath);
    }

    @GetMapping("/getCheckBillById")
    @ApiOperation(value = "查询对账单")
    public Result getCheckBillById(@ApiParam(value = "账单Id", required = true) @RequestParam("id") String id) {
        if (StringUtils.isEmpty (id)) {
            return Result.error ("参数不能为空");
        }
        Integer idNo;
        try {
            idNo = Integer.valueOf (id);
        } catch (NumberFormatException e) {
            return Result.error ("id 格式不正确");
        }
        if (idNo < Constants.MIN_ID) {
            return Result.error ("id 格式不正确");
        }
        AccessGoldCheck accessGoldCheck = accessGoldCheckDao.findById (idNo);
        return Result.success (accessGoldCheck);
    }

    @PostMapping("/addRemark")
    @ApiOperation(value = "添加对账备注")
    public Result addCheckBillRemark(@RequestBody AccessGoldCheck accessGoldCheck) {
        if (StringUtils.isEmpty (accessGoldCheck.getId ())) {
            return Result.error ("对账单ID不能为空");
        }
        if (null == accessGoldCheckDao.findById (accessGoldCheck.getId ())) {
            return Result.error ("对账单不存在");
        }
        if (StringUtils.isEmpty (accessGoldCheck.getRemark ())) {
            return Result.error ("备注内容不能为空");
        }
        try {
            accessGoldCheckDao.update (accessGoldCheck);
        } catch (Exception e) {
            return Result.error ("添加对账单备注失败");
        }
        return Result.success ();
    }

    @ApiOperation(value = "通道(出入金)对账列表", notes = ApiNote.findChannelCashIn)
    @GetMapping("/findChannelCashIn")
    public Result getChannelCashInList(@ApiParam(value = "开始页", required = true) @RequestParam("offset") Integer offset,
                                       @ApiParam(value = "每页显示条数", required = true) @RequestParam("limit") Integer limit,
                                       @ApiParam(value = "对账类别(1 入金，2出金)", required = true) @RequestParam("checkKind") String checkKind,
                                       @ApiParam(value = "开始时间") @RequestParam(value = "startTime", required = false) String startTime,
                                       @ApiParam(value = "截止时间") @RequestParam(value = "endTime", required = false) String endTime,
                                       @ApiParam(value = "系统流水号") @RequestParam("serialNum") String serialNum,
                                       @ApiParam(value = "对账状态(0 失败，1成功 3作废)") @RequestParam("checkResult") String checkResult,
                                       @ApiParam(value = "账户名(mt4账号)") @RequestParam("accountName") String accountName,
                                       @ApiParam(value = "支付通道") @RequestParam("payChannel") String payChannel) {

        if (null == checkKind || !(Constants.rCheckKind.equals (checkKind) || Constants.cCheckKind.equals (checkKind))) {
            return Result.error ("请输入正确对账类别");
        }
        if (StringUtils.isEmpty (startTime)) {
            startTime = DateUtil.parser (DateUtil.getBeginDayOfMonth ());
        }

        PageTableRequest request = new PageTableRequest ();
        if (null == offset) {
            offset = 0;
        } else if (offset > 0) {
            offset = offset - 1;
        }
        if (null == limit) {
            limit = 10;
        }
        Map<String, Object> params = new HashMap<> ();
        params.put ("checkKind", checkKind);
        params.put ("startTime", startTime);
        params.put ("endTime", endTime);
        params.put ("serialNum", serialNum);
        params.put ("checkResult", checkResult);
        params.put ("accountName", accountName);
        params.put ("payChannel", payChannel);
        request.setOffset (offset * limit);
        request.setLimit (limit);
        request.setParams (params);
        return Result.success (new PageTableHandler (request1 -> paymentChannelService.getChannelCashCount (request.getParams ()), request2 -> paymentChannelService.getChannelCashList (request.getParams (), request.getOffset (), request.getLimit ())).handle (request));

    }

    @ApiOperation(value = "通道对账", notes = "checkStat:对账状态（true 成功，false 失败）\n" + "checkOkNum：对账成功条数\n" + "checkNotOkNum：对账不成功数")
    @PostMapping("/checkBill")
    public Result checkBill(@RequestBody BaseRequestVo baseRequestVo) {
        String startTime = baseRequestVo.getStartTime ();
        String endTime = baseRequestVo.getEndTime ();
        String checkKind = baseRequestVo.getCheckKind ();//对账类别（1表示入金对账，2表示出金对账）
        if (null == startTime || null == endTime) {
            return Result.error ("请输入对账的起止日期");
        }
        if(!ComUtil.isDate (startTime) || !ComUtil.isDate (endTime)){
            return Result.error ("输入日期不正确");

        }
        if (StringUtils.isEmpty (checkKind)) {
            return Result.error ("请输入对账类别");
        }
        endTime=DateUtil.parser (DateUtil.addOrReduceDay (DateUtil.parse (endTime),1));
        Map<String, Object> resultMap = paymentChannelService.checkBill (startTime, endTime, checkKind);
        return Result.success (resultMap);
    }

    @ApiOperation(value = "出入金问题账单作处理")
    @PostMapping("/dealProblemBill")
    public Result dealProblemBill(@RequestBody BaseRequestVo baseRequestVo) {
        String id = baseRequestVo.getId ();
        String remarks = baseRequestVo.getRemark ();
        String checkResult = baseRequestVo.getCheckResult ();
        String checkKind = baseRequestVo.getCheckKind ();
        if (null == checkKind || !(Constants.rCheckKind.equals (checkKind) || Constants.cCheckKind.equals (checkKind))) {
            return Result.error ("请输入正确账单类别");
        }
        if (StringUtils.isEmpty (id)) {
            return Result.error ("账单Id不能为空");
        }
        if (StringUtils.isEmpty (remarks)) {
            return Result.error ("备注不能为空");
        }
        if (StringUtils.isEmpty (checkResult)) {
            return Result.error ("处理方式不能为空");
        }
        if (Integer.parseInt (checkResult) < Constants.check_Bill_INVALID) {
            return Result.error ("问题账单处理方式不对");
        }
        try {
            paymentChannelService.dealProbCashBill (id, remarks, baseRequestVo.getConflictAmount (), baseRequestVo.getImageUrl (), checkResult, checkKind);
        } catch (Exception e) {
            log.error ("操作失败{}", e);
            return Result.error (e.getMessage ());
        }
        return Result.success ();
    }
}
