package com.ry.cbms.decision.server.Exeption;


import com.ry.cbms.decision.server.Msg.CodeMsg;

/**
 * @Author maoYang
 * @Date 2019/4/25
 * @Description 全局异常类
 */
public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm) {
		super(cm.toString());
		this.cm = cm;
	}
	public GlobalException(String cm) {
		super(cm);
	}
	public CodeMsg getCm() {
		return cm;
	}

}
