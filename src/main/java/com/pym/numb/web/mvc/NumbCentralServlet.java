package com.pym.numb.web.mvc;

import com.alibaba.fastjson.JSON;
import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.util.GlobalUtil;
import com.pym.numb.util.StrUtils;
import com.pym.numb.web.annotation.JsonRes;
import com.pym.numb.web.handler.NumbExceptionHandler;
import com.pym.numb.web.handler.NumbViewHandler;
import com.pym.numb.web.interceptor.NumbInterceptor;
import com.pym.numb.web.mapper.Mapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/18.
 */
public final class NumbCentralServlet extends HttpServlet {
    private Map<String, Mapper> mappers = new HashMap<String, Mapper>();
    private List<NumbInterceptor> interceptors = new LinkedList<NumbInterceptor>();
    private NumbViewHandler viewHandler = new NumbViewHandler();
    private NumbExceptionHandler exceptionHandler;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String key = req.getServletPath().trim();
        if (StrUtils.isEmpty(key)) {
            key =  req.getRequestURI().replaceFirst(req.getContextPath(),"").split("\\?")[0].trim();
        }
        Mapper mapper = mappers.get(key);
        if (mapper == null) {
            resp.setStatus(404);
        } else {
            try {
                doAction(req,resp,mapper);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    @Override
    public void init() throws ServletException {
       NumbContainer container = GlobalUtil.getInstance().getNumbContainer();
       if (container != null) {
           this.viewHandler = container.getViewHandler();
           this.exceptionHandler = container.getExceptionHandler();
           this.mappers = container.getMappers();
           this.interceptors = container.getInterceptors();
       }
    }

    private void doAction(HttpServletRequest request, HttpServletResponse response, Mapper mapper) throws Exception {
        for (NumbInterceptor nIcp : interceptors) {
            if (!nIcp.before(request,response,mapper)) {
                return;
            }
        }
        //设置字符contentType
        String encoding = request.getCharacterEncoding();
        if (StrUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        Object result = "";
        try {
            result = mapper.invork(request,response);
            if (mapper.getControllerMethod().getAnnotation(JsonRes.class) != null) {
                response.setContentType("application/json; charset=" + encoding);
                OutputStream out = null;
                try {
                    out =  response.getOutputStream();
                    out.write(JSON.toJSONString(result).getBytes(encoding));
                    out.flush();
                } catch (Exception e) {

                } finally {
                    if (out!=null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return;
            }
        } catch (Exception e) {
            if (exceptionHandler == null) {
                throw e;
            }
            String errorPath = exceptionHandler.handler(request,response,e);
            if (!StrUtils.isEmpty(errorPath)) {
                viewHandler.handler(result.toString(),request,response);
            }
            return;
        }
        for (NumbInterceptor nIcp : interceptors) {
            nIcp.complete(request,response,mapper);
        }
        //处理视图
        viewHandler.handler(result.toString(),request,response);
    }
}
