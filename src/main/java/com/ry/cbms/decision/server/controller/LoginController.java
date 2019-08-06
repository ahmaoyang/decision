package com.ry.cbms.decision.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ry.cbms.decision.server.Msg.CodeMsg;
import com.ry.cbms.decision.server.Msg.Result;
import com.ry.cbms.decision.server.redis.RedisKeyGenerator;
import com.ry.cbms.decision.server.utils.*;
import com.ry.cbms.decision.server.vo.LogInVo;
import com.ry.cbms.decision.server.vo.LogoutVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author maoYang
 * @Date 2019/7/26 11:29
 * @Description 登录相关
 */
@Api(tags = "登录相关")
@RestController
@RequestMapping("/decision")
public class LoginController {
    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "登录",notes = ApiNote.login)
    public Result login(@RequestBody LogInVo logInVo) {
        String username=logInVo.getUsername ();
        String password=logInVo.getPassword ();
       if(ParamCheckUtil.check (username,password)) {
           return Result.error ("登录账号或密码不能为空");
       }
        Map<String, Object> params = new HashMap<> ();
        params .put ("username", username);
        params .put ("password", password);
        String res;
        try {
           res= HttpUtil.httpPostWithForm (Constants.localUrl + "/login", params );
        } catch (IOException e) {
           return Result.error ("登陆失败");
        }
        JSONObject retObj= JSON.parseObject (res);
        if("401".equals (retObj.get("code"))){
            String message=retObj.getString ("message");
            return Result.error (message);
        }
        return Result.success (retObj);
    }


    @PostMapping(value = "/logout")
    @ApiOperation(value = "登出",notes = ApiNote.logout)
    public Result logout(@RequestBody LogoutVo logoutVo) {
        String token=logoutVo.getToken ();
        if(StringUtils.isEmpty (token)){
            return Result.error ("token 不能为空");
        }
        Object obj;
        try {
            HttpUtil2.doGetFor(Constants.localUrl+"/logout",token);
            obj=redisTemplate.opsForValue ().get(RedisKeyGenerator.getLogOutStatus (token));
        } catch (Exception e) {
            return Result.error ("登出失败");
        }
        if(null==obj){
            return Result.error ("登出失败");
        }
        return Result.success ("登出成功");
    }
}
