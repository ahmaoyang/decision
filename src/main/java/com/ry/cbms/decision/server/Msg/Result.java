package com.ry.cbms.decision.server.Msg;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Author maoYang
 * @Date 2019/4/25 14:51
 * @Description 返回
 */
@ApiModel(value = "返回体")
public class Result<T> {
	@ApiModelProperty(value="返回码 0:表示成功，非0:表示失败")
	private int code;
	@ApiModelProperty(value="返回消息",example = "success")
	private String msg;
	@ApiModelProperty(value="返回数据")
	private T data;
	
	/**
	 *  成功时候的调用
	 * */
	public static  <T> Result<T> success(T data){
		return new Result<T>(data);
	}

	public static  <T> Result<T> success(CodeMsg codeMsg){
		return new Result<T>(codeMsg);
	}
	public static  <T> Result<T> success(){
		return new Result<T>(CodeMsg.SUCCESS);
	}
	/**
	 *  失败时候的调用
	 * */
	public static  <T> Result<T> error(CodeMsg codeMsg){
		return new Result<T>(codeMsg);
	}
	public static  <T> Result<T> error(){
		return new Result<T>(1,"操作失败");
	}

	public static  <T> Result<T> error(String msg){
		return new Result<T>(1,msg);
	}
	private Result(T data) {
		CodeMsg codeMsg= CodeMsg.SUCCESS;
		this.data = data;
		this.msg=codeMsg.getMsg();
		this.code=codeMsg.getCode();
	}
	
	private Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	private Result(CodeMsg codeMsg) {
		if(codeMsg != null) {
			this.code = codeMsg.getCode();
			this.msg = codeMsg.getMsg();
		}
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
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
}
