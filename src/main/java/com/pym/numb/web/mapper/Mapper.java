package com.pym.numb.web.mapper;

import com.pym.numb.context.lifecycle.NumbLifecycle;
import com.pym.numb.util.ObjUtils;
import com.pym.numb.util.StrUtils;
import com.pym.numb.web.annotation.ReqParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 封装控制器和执行方法
 */
public class Mapper implements NumbLifecycle{
    private String path;
    private Object controllerObj;
    private Method controllerMethod;

    public Mapper(Object controllerObj, Method controllerMethod, String path) {
        this.controllerObj = controllerObj;
        this.controllerMethod = controllerMethod;
        this.path = path;
    }

    public Object getControllerObj() {
        return controllerObj;
    }

    public Method getControllerMethod() {
        return controllerMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 解析参数
      * @param request
     *
     */
    public List<Object> paseParamters(HttpServletRequest request, HttpServletResponse response) {
        List<Object> pl = new ArrayList<Object>();
        //int index = 0;
        //处理控制层方法参数
        Arrays.asList(controllerMethod.getParameters()).forEach(parameter -> {
            Class<?> cls = parameter.getType();
            ReqParam reqParam = parameter.getAnnotation(ReqParam.class);
            if (reqParam != null) {
                String pv = request.getParameter(reqParam.value());
                if (cls.equals(String.class)) {
                    pl.add(pv);
                } else if (cls.equals(Integer.class) || cls.equals(int.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Integer.valueOf(pv));
                    }
                } else if (cls.equals(Long.class) || cls.equals(long.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Long.parseLong(pv));
                    }
                } else if (cls.equals(Float.class) || cls.equals(float.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Float.parseFloat(pv));
                    }

                } else if (cls.equals(Double.class) || cls.equals(double.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Double.parseDouble(pv));
                    }
                } else if (cls.equals(Boolean.class) || cls.equals(boolean.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Boolean.parseBoolean(pv));
                    }

                } else if (cls.equals(Byte.class) || cls.equals(byte.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Byte.parseByte(pv));
                    }

                } else if (cls.equals(Short.class) || cls.equals(short.class)) {
                    if(StrUtils.isEmpty(pv)) {
                        pl.add(null);
                    } else {
                        pl.add(Short.parseShort(pv));
                    }

                } else {
                    Map<String, String> map = new HashMap<String, String>();
                    request.getParameterMap().forEach((key,value) -> {
                        map.put(key,value[0]);
                    });
                    if (parameter.getType().equals(Map.class)) {
                        if (parameter.getAnnotation(ReqParam.class) != null) {
                            pl.add(map);
                        }
                    } else {
                        pl.add(ObjUtils.mapToObjForReq(map,parameter.getType()));
                    }
                }
            } else {
                if (cls.equals(HttpServletRequest.class)) {
                    pl.add(request);
                } else if (cls.equals(HttpServletResponse.class)) {
                    pl.add(response);
                } else {
                    //没有注解的其他参数
                    pl.add(null);
                }
            }

        });
        return pl;
    }

    /**
     * 执行方法
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public Object invork(HttpServletRequest request, HttpServletResponse response) throws Exception {
         if (controllerObj == null || controllerMethod == null) {
             throw new RuntimeException("控制器对象或方法不能为空");
         }
         return controllerMethod.invoke(controllerObj,paseParamters(request,response).toArray());
     }


    @Override
    public void init() {

    }

    @Override
    public void destory() {
        this.path = null;
        this.controllerObj = null;
        this.controllerMethod = null;
    }
}
