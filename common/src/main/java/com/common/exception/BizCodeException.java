package com.common.exception;

/**
 * user:lufei
 * DATE:2021/10/13
 **/

/**
 *  - 10:通用
 *      001:参数格式校验
 *  - 11:商品
 *  - 12:订单
 *  - 13:购物车
 *  - 14:物流
 */
public enum BizCodeException {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    FAILTOUPDATE(14000,"采购单不可被修改");

    private int code;
    private String msg;

    BizCodeException(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
