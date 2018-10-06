package com.lee.blogdemo1.exceptionHandler;


import com.lee.blogdemo1.VO.Response;
import com.lee.blogdemo1.controller.CategoryController;
import com.lee.blogdemo1.controller.VoteController;
import com.lee.blogdemo1.controller.admin.UserController;
import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * UserController和VoteController全局异常处理器.
 */
@ControllerAdvice(basePackageClasses = {UserController.class,VoteController.class,CategoryController.class})  //表示这是一个全局的异常处理器,只捕获这些类抛出的异常
public class ControllerExceptionHandler {

    /**
     * 拦截并处理数据格式校验异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity handlerBindException(BindException e) {
        //每个异常信息之间以";"分隔,转换为String
        String errorMsg = StringUtils.join(e.getAllErrors().stream().map(err->err.getDefaultMessage()).toArray(), ";");
        System.out.println("异常已被拦截："+errorMsg);
        return ResponseEntity.ok().body(new Response(false,errorMsg));
    }

    /**
     * 拦截并处理唯一键约束异常
     * Duplicate entry '123@qq.com' for key 'UK_ob8kqyqqgmefl0aco34akdtpe'
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity handlerDataIntegrityViolationException(DataIntegrityViolationException e){
        System.out.println("异常已被拦截："+e.getRootCause().getMessage());
        String msg = e.getRootCause().getMessage();
        if (msg.startsWith("Duplicate entry")) {
            msg = msg.substring(msg.indexOf("'"), msg.indexOf("for"))+"已被注册!";
        }
        return ResponseEntity.ok().body(new Response(false,msg));
    }

    @ExceptionHandler(AccessDeniedException.class) //拦截访问@PreAuthorize注解的方法时权限被拒绝抛出的异常,此异常信息不处理好像会有默认配置("不允许访问")
    public ResponseEntity handlerAccessDeniedException(AccessDeniedException e){
        System.out.println("异常已被拦截："+e);
        //返回权限拒绝页
        return ResponseEntity.ok().body(new Response(false,"没有权限哦~"));
    }

    @ExceptionHandler //拦截并处理其他异常
    public ResponseEntity handlerOthers(Exception e){
        System.out.println("异常已被拦截："+e);
        return ResponseEntity.ok().body(new Response(false,e.getMessage()));
    }



}
