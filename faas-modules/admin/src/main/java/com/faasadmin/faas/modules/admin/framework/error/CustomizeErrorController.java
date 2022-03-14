package com.faasadmin.faas.modules.admin.framework.error;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @version: V1.0
 * @author: faasadmin
 * @description: 自定义错误处理
 * @data: 2021-08-28 22:47
 **/
//@Controller
public class CustomizeErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request){
        //获取statusCode:401,404,500
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if(statusCode == 401){
            return "/error/401";
        }else if(statusCode == 404){
            return "/one.html";
        }else if(statusCode == 403){
            return "/error/403";
        }else{
            return "/error/500";
        }

    }

    @Override
    public String getErrorPath() {
        return "/";
    }

}
