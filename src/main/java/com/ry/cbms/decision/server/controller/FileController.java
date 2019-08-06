package com.ry.cbms.decision.server.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ry.cbms.decision.server.Exeption.GlobalException;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.Msg.Result;

import com.ry.cbms.decision.server.model.FileInfo;
import com.ry.cbms.decision.server.page.table.PageTableHandler;
import com.ry.cbms.decision.server.page.table.PageTableRequest;
import com.ry.cbms.decision.server.service.FileService;
import com.ry.cbms.decision.server.utils.ComUtil;
import com.ry.cbms.decision.server.utils.Constants;
import com.ry.cbms.decision.server.utils.ParamCheckUtil;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ry.cbms.decision.server.annotation.LogAnnotation;
import com.ry.cbms.decision.server.dao.FileInfoDao;
import com.ry.cbms.decision.server.dto.LayuiFile;
import com.ry.cbms.decision.server.dto.LayuiFile.LayuiFileData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "文件")
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private FileInfoDao fileInfoDao;

    @LogAnnotation
    @PostMapping
    @ApiOperation(value = "文件上传 MultipartFile 类型")
    public Result uploadFile(@RequestBody MultipartFile file) {
        if (ParamCheckUtil.check (file)) {
            return Result.error ("参数不能为空");
        }
        try {
            fileService.save (file);
        } catch (IOException e) {
            return Result.error (e.getMessage ());
        }
        return Result.success (CodeMsg.SUCCESS);
    }

    @LogAnnotation
    @PostMapping("/uploadImage")
    @ApiOperation(value = "图片上传")
    public Result uploadImage(MultipartFile file) {
        if (ParamCheckUtil.check (file)) {
            return Result.error ("参数不能为空");
        }
        String fullPath;
        try {
            FileInfo fileInfo = fileService.save (file);
            fullPath = fileInfo.getPath ();
        } catch (IOException e) {
            throw new GlobalException (e.getMessage ());
        }
        return Result.success (fullPath);
    }


    /**
     * layui富文本文件自定义上传
     *
     * @param file
     * @param domain
     * @return
     * @throws IOException
     */
    @LogAnnotation
    @PostMapping("/layui")
    @ApiOperation(value = "文本文件自定义上传")
    public Result uploadLayuiFile(MultipartFile file, @ApiParam("文件目录地址") @RequestParam(value = "domain") String domain) throws IOException {
        if (ParamCheckUtil.check (file, domain)) {
            return Result.error ("参数不能为空");
        }
        FileInfo fileInfo = fileService.save (file);
        LayuiFile layuiFile = new LayuiFile ();
        layuiFile.setCode (0);
        LayuiFileData data = new LayuiFileData ();
        layuiFile.setData (data);
        data.setSrc (domain + "/statics" + fileInfo.getUrl ());
        data.setTitle (file.getOriginalFilename ());
        return Result.success (layuiFile);
    }

    @GetMapping
    @ApiOperation(value = "文件查询")
    // @PreAuthorize("hasAuthority('sys:file:query')")
    public Result listFiles(@ApiParam(value = "页数", required = true) @RequestParam(value = "offset") Integer offset,
                            @ApiParam(value = "每页显示条数", required = true) @RequestParam(value = "limit") Integer limit,
                            @ApiParam(value = "开始时间") @RequestParam(value = "beginTime", required = false) String beginTime,
                            @ApiParam(value = "结束时间") @RequestParam(value = "endTime", required = false) String endTime) {
        PageTableRequest request = new PageTableRequest ();
        Map<String, Object> paramMap = new HashMap<> ();
        paramMap.put ("beginTime", beginTime);
        paramMap.put ("endTime", endTime);
        ComUtil.setPageParam (request, offset, limit, paramMap);
        return Result.success (new PageTableHandler (request1 -> fileInfoDao.count (request1.getParams ()), request12 -> {
            List<FileInfo> list = fileInfoDao.list (request12.getParams (), request12.getOffset (), request12.getLimit ());
            return list;
        }).handle (request));
    }

    @LogAnnotation
    @DeleteMapping("/{id}")
    @ApiOperation(value = "文件删除")
    //@PreAuthorize("hasAuthority('sys:file:del')")
    public Result delete(@PathVariable String id) {
        if (ParamCheckUtil.check (id)) {
            return Result.error ("参数不能为空");
        }
        try {
            if (Long.valueOf (id) < Constants.MIN_ID) {
                return Result.error ("参数格式不正确");
            }
        } catch (NumberFormatException e) {
            return Result.error ("参数格式不正确");
        }
        try {
            fileService.delete (id);
        } catch (Exception e) {
            throw new GlobalException (e.toString ());
        }
        return Result.success (CodeMsg.SUCCESS);
    }

}
