package com.ry.cbms.decision.server.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.model.Mail;
import com.ry.cbms.decision.server.model.MailTo;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.page.table.PageTableResponse;
import com.ry.cbms.decision.server.service.MailService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ry.cbms.decision.server.dao.MailDao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "邮件服务")
@RestController
@RequestMapping("/mails")
public class MailController {

    @Autowired
    private MailDao mailDao;
    @Autowired
    private MailService mailService;

    @LogAnnotation
    @PostMapping
    @ApiOperation(value = "(发送)保存邮件")
    //@PreAuthorize("hasAuthority('mail:send')")
    public Result save(@RequestBody Mail mail) {
        if (ParamCheckUtil.check (mail)) {
            return Result.error ("参数不能为空");
        }
        String toUsers = mail.getToUsers ().trim ();
        if (StringUtils.isBlank (toUsers)) {
            return Result.error ("收件人不能为空");
        }
        toUsers = toUsers.replace (" ", "");
        toUsers = toUsers.replace ("；", ";");
        String[] strings = toUsers.split (";");

        List<String> toUser = Arrays.asList (strings);
        if (!ComUtil.checkEmail (toUser)) {
            return Result.error ("接收邮箱存账号在错误，请填写正确邮件账号");
        }
        toUser = toUser.stream ().filter (u -> !StringUtils.isBlank (u)).map (u -> u.trim ()).collect (Collectors.toList ());
        mailService.save (mail, toUser);
        return Result.success (mail);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取邮件")
    //@PreAuthorize("hasAuthority('mail:all:query')")
    public Result get(@PathVariable Long id) {
        if (ParamCheckUtil.check (id)) {
            return Result.error ("参数不能为空");
        }
        if (id < Constants.MIN_ID) {
            return Result.error ("id格式不正确");
        }
        return Result.success (mailDao.getById (id));
    }

    @GetMapping("/{id}/to")
    @ApiOperation(value = "根据id获取邮件发送详情")
    //@PreAuthorize("hasAuthority('mail:all:query')")
    public Result getMailTo(@PathVariable Long id) {
        if (ParamCheckUtil.check (id)) {
            return Result.error ("参数不能为空");
        }
        if (id < Constants.MIN_ID) {
            return Result.error ("id格式不正确");
        }
        return Result.success (mailDao.getToUsers (id));
    }

    @GetMapping
    @ApiOperation(value = "邮件列表")
    // @PreAuthorize("hasAuthority('mail:all:query')")
    public Result list(@ApiParam(value = "页数", required = true) @RequestParam(value = "offset") Integer offset,
                       @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                       @ApiParam(value = "开始时间") @RequestParam(value = "beginTime", required = false) String beginTime,
                       @ApiParam(value = "结束时间") @RequestParam(value = "endTime", required = false) String endTime,
                       @ApiParam(value = "主题") @RequestParam(value = "subject", required = false) String subject) {
        PageTableRequest request = new PageTableRequest ();
        Map<String, Object> params = new HashMap<> ();
        ComUtil.setPageParamMap (params, beginTime, endTime);
        params.put ("subject", subject);
        ComUtil.setPageParam (request, offset, limit, params);
        return Result.success (new PageTableHandler (request1 -> mailDao.count (request1.getParams ()), request12 -> mailDao.list (request12.getParams (), request12.getOffset (), request12.getLimit ())).handle (request));
    }

}
