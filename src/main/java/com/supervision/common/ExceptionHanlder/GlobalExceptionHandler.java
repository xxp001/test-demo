package com.supervision.common.ExceptionHanlder;

import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.ResponseMessage;
import com.supervision.common.util.StaticProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.text.ParseException;

/*
 * @Project:SupervisionSystem
 * @Description:
 * @Author:TjSanshao
 * @Create:2019-02-19 02:41
 *
 **/
@Slf4j
@ControllerAdvice(basePackages = "com.supervision")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 登录验证异常处理器，密码错误
    @ExceptionHandler({IncorrectCredentialsException.class})
    @ResponseBody
    public String incorrectCredentialsExceptionHandler(ShiroException e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "密码错误！如忘记密码请联系管理员！", "密码错误！如忘记密码请联系管理员！");
    }

    // 登录验证异常处理器，账号不存在
    @ExceptionHandler({UnknownAccountException.class})
    @ResponseBody
    public String unknownAccountExceptionHandler(ShiroException e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "账号不存在！如需分配账号请联系管理员！", "用户名不存在！如需分配账号请联系管理员！");
    }

    // 未登录异常处理器
    @ExceptionHandler({UnauthenticatedException.class, AuthenticationException.class})
    @ResponseBody
    public String unauthenticatedExceptionHandler(ShiroException e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.LOGIN_FAIL, "请先登录！");
    }

    // 未授权异常处理器
    @ExceptionHandler({UnauthorizedException.class, AuthorizationException.class})
    @ResponseBody
    public String unauthorizationExceptionHandler(ShiroException e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, "没有权限！");
    }

    // shiro异常处理器
    @ExceptionHandler(ShiroException.class)
    @ResponseBody
    public String shiroExceptionHandler(ShiroException e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, "授权过程出错，请稍后重试！");
    }

    // 日期格式异常处理器
    @ExceptionHandler(ParseException.class)
    @ResponseBody
    public String parseExceptionHandler(ParseException e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, "日期参数格式错误！请核实！");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String exceptionHandler(Exception e) {
        log.error(e.getMessage());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, "系统内部错误！请联系管理员或者稍后重试！");
    }

}
