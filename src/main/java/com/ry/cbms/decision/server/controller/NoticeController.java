package com.ry.cbms.decision.server.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import com.ry.cbms.decision.server.vo.NoticeVO;
import com.ry.cbms.decision.server.model.Notice;
import com.ry.cbms.decision.server.model.SysUser;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.utils.UserUtil;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.dao.NoticeDao;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "公告")
@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeDao noticeDao;

    @LogAnnotation
    @PostMapping
    @ApiOperation(value = "保存公告")
    //@PreAuthorize("hasAuthority('notice:add')")
    public Result saveNotice(@RequestBody Notice notice) {
        if (ParamCheckUtil.check (notice.getTitle (), notice.getContent ())) {
            return Result.error ("参数不能为空");
        }
        Date currDate = new Date ();
        try {
            notice.setCreateTime (currDate);
            notice.setUpdateTime (currDate);
            noticeDao.save (notice);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success (notice);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取公告")
    //@PreAuthorize("hasAuthority('notice:query')")
    public Result get(@PathVariable Long id) {
        if (null == id || id < Constants.MIN_ID) {
            return Result.error ("id不能为空或格式不正确");
        }
        return Result.success (noticeDao.getById (id));
    }

    @GetMapping("/readNotice")
    @ApiOperation(value = "读取公告")
    public Result readNotice(@ApiParam("公告id") @RequestParam("id") Long id) {
        if (null == id || id < 1) {
            return Result.error ("id不能为空或格式不正确");
        }
        NoticeVO vo = new NoticeVO ();
        Notice notice = noticeDao.getById (id);
        if (null == notice || notice.getStatus () == Notice.Status.DRAFT) {
            return Result.success (vo);
        }
        vo.setNotice (notice);
        SysUser user = UserUtil.getLoginUser ();
        if (null == user) {
            return Result.error ("用户未登录");
        }
        noticeDao.saveReadRecord (notice.getId (), user.getId ());

        List<SysUser> users = noticeDao.listReadUsers (id);
        vo.setUsers (users);

        return Result.success (vo);
    }

    @LogAnnotation
    @PutMapping
    @ApiOperation(value = "修改公告")
    //@PreAuthorize("hasAuthority('notice:add')")
    public Result updateNotice(@RequestBody Notice notice) {
        Long id = notice.getId ();
        if (null == id || id < Constants.MIN_ID) {
            return Result.error ("id不能为空或格式不正确");
        }
        Notice no = noticeDao.getById (notice.getId ());
        if (no.getStatus () == Notice.Status.PUBLISH) {
            throw new IllegalArgumentException ("发布状态的不能修改");
        }
        try {
            noticeDao.update (notice);
        } catch (Exception e) {
            return Result.error (e.toString ());
        }
        return Result.success (notice);
    }

    @GetMapping
    @ApiOperation(value = "公告管理列表")
    //@PreAuthorize("hasAuthority('notice:query')")
    public Result listNotice(@ApiParam(value = "页数", required = true) @RequestParam(value = "offset") Integer offset,
                             @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                             @ApiParam(value = "开始时间") @RequestParam(value = "beginTime", required = false) String beginTime,
                             @ApiParam(value = "结束时间") @RequestParam(value = "endTime", required = false) String endTime,
                             @ApiParam(value = "标题") @RequestParam(value = "title", required = false) String title,
                             @ApiParam(value = "状态, 0 草稿(未发布)，1:已经发布") @RequestParam(value = "status", required = false) String status) {
        PageTableRequest request = new PageTableRequest ();
        Map<String, Object> paramMap = new HashMap<> ();
        paramMap.put ("beginTime", beginTime);
        paramMap.put ("endTime", endTime);
        paramMap.put ("title", title);
        paramMap.put ("status", status);
        ComUtil.setPageParam (request, offset, limit, paramMap);
        return Result.success (new PageTableHandler (request1 -> noticeDao.count (request1.getParams ()), request12 -> noticeDao.list (request12.getParams (), request12.getOffset (), request12.getLimit ())).handle (request));
    }

    @LogAnnotation
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除公告")
    //@PreAuthorize("hasAuthority('notice:del')")
    public Result delete(@PathVariable Long id) {
        if (null == id || id < 1) {
            return Result.error ("id不能为空或格式不正确");
        }
        try {
            noticeDao.delete (id);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success ();
    }

    @ApiOperation(value = "当前登陆用户未读公告数")
    @GetMapping("/count-unread")
    public Result countUnread() {
        SysUser user = UserUtil.getLoginUser ();
        if (null == user) {
            return Result.error ("用户未登录");
        }
        return Result.success (noticeDao.countUnread (user.getId ()));
    }

    @PostMapping("/published")
    @ApiOperation(value = "已发布公告列表(当前用户需登录，展示用户公告的读状态)")
    public Result listNoticeReadVO(@ApiParam(value = "分业查询的开始数", required = true) @RequestParam(value = "offset") Integer offset,
                                   @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                                   @ApiParam(value = "开始时间") @RequestParam(value = "beginTime", required = false) String beginTime,
                                   @ApiParam(value = "结束时间") @RequestParam(value = "endTime", required = false) String endTime,
                                   @ApiParam(value = "标题") @RequestParam(value = "title", required = false) String title,
                                   @ApiParam(value = "是否已读, 0 未读，1:已读") @RequestParam(value = "isRead", required = false) String isRead) {
        PageTableRequest request = new PageTableRequest ();
        Map<String, Object> paramMap = new HashMap<> ();
        paramMap.put ("beginTime", beginTime);
        paramMap.put ("endTime", endTime);
        paramMap.put ("title", title);
        paramMap.put ("isRead", isRead);
        ComUtil.setPageParam (request, offset, limit, paramMap);
        SysUser user = UserUtil.getLoginUser ();
        if (null == user) {
            return Result.error ("用户未登录");
        }
        request.getParams ().put ("userId", user.getId ());

        return Result.success (new PageTableHandler (request1 -> noticeDao.countNotice (request1.getParams ()), request12 ->
                noticeDao.listNotice (request12.getParams (), request12.getOffset (), request12.getLimit ())).handle (request));
    }
}
