package com.pym.numb.web.interceptor;

import com.pym.numb.web.mapper.Mapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/8/23.
 */
public abstract class NumbInterceptor {
    private Set<String> exclude = new HashSet<String>();
    public boolean before(HttpServletRequest request, HttpServletResponse response, Mapper mapper) {
        return false;
    }

    public void after(HttpServletRequest request, HttpServletResponse response, Mapper mapper) {

    }

    public void complete(HttpServletRequest request, HttpServletResponse response, Mapper mapper) {

    }
}
