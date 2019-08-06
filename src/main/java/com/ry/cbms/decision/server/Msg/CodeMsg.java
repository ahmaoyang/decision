package com.ry.cbms.decision.server.Msg;

/**
 * @Author maoYang
 * @Date 2019/4/25
 * @Description 返回消息码类
 */
public class CodeMsg {

    private int code;
    private String msg;

    //通用的错误码
    public static final CodeMsg SUCCESS = new CodeMsg (0, "success");
    public static final CodeMsg SERVER_ERROR = new CodeMsg (500100, "服务端异常");
    public static final CodeMsg BIND_ERROR = new CodeMsg (500101, "参数校验异常：%s");
    public static final CodeMsg REQUEST_ILLEGAL = new CodeMsg (500102, "请求非法");
    public static final CodeMsg ACCESS_LIMIT_REACHED = new CodeMsg (500104, "访问太频繁！");
    public static final CodeMsg USER_INFO_DUPLICATE = new CodeMsg (500105, "用户存在");
    //登录模块 5002XX
    public static final CodeMsg SESSION_ERROR = new CodeMsg (500210, "Session不存在或者已经失效");
    public static final CodeMsg PASSWORD_EMPTY = new CodeMsg (500211, "登录密码不能为空");
    public static final CodeMsg MOBILE_EMPTY = new CodeMsg (500212, "手机号不能为空");
    public static final CodeMsg MOBILE_ERROR = new CodeMsg (500213, "手机号格式错误");
    public static final CodeMsg MOBILE_NOT_EXIST = new CodeMsg (500214, "用户不存在");
    public static final CodeMsg PASSWORD_ERROR = new CodeMsg (500215, "密码错误");
    public static final CodeMsg USER_NOT_EXIST = new CodeMsg (500216, "用户不存在");
    public static final CodeMsg LOGIN_ERROR = new CodeMsg (500217, "登录失败");
    public static final CodeMsg LOGOUT_ERROR = new CodeMsg (500217, "登出失败");
    public static final CodeMsg ON_LOGIN = new CodeMsg (500218, "您已经处于登录状态");
    public static final CodeMsg NOT_LOGIN = new CodeMsg (500219, "未登录");
    //terminus5003XX
    public static final CodeMsg TERMINUS_DOWN= new CodeMsg (500300, "Terminus 服务宕机");

    public CodeMsg() {
    }

    private CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public CodeMsg fillArgs(Object... args) {
        int code = this.code;
        String message = String.format (this.msg, args);
        return new CodeMsg (code, message);
    }

    @Override
    public String toString() {
        return "CodeMsg [code=" + code + ", msg=" + msg + "]";
    }


}
