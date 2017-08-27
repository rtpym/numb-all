package com.pym.numb.web.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Administrator on 2017/8/24.
 */
public interface NumbExceptionHandler {
    String handler(HttpServletRequest request, HttpServletResponse response, Exception ex) ;
}
