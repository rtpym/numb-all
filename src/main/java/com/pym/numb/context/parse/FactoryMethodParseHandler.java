package com.pym.numb.context.parse;

import com.pym.numb.context.annotation.*;
import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.context.lifecycle.NumbLifecycle;
import com.pym.numb.context.parse.util.BeanParseUtils;
import com.pym.numb.util.StrUtils;
import com.pym.numb.web.handler.NumbExceptionHandler;
import com.pym.numb.web.handler.NumbViewHandler;
import com.pym.numb.web.interceptor.NumbInterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 一个工厂类封装成多个MethodHandler的目的是为了多个工厂类之间混合交叉注入，提升注入效率，
 * 考虑没有引用对象的优先
 */
public class FactoryMethodParseHandler implements NumbLifecycle{
    private Object subObj;
    private Method method;

    //参数对象数组
    private Parameter[] parameters;
    //具体参数
    private List<Object> params = new ArrayList<Object>();
   // private List<Method> methods = new ArrayList<Method>();

    public FactoryMethodParseHandler(Object subObj, Method method) {
        this.subObj = subObj;
        this.method = method;
        this.parameters = this.method.getParameters();
    }

    public Object getSubObj() {
        return subObj;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * 从容器中查找对象，对象生成后添加到容器，没有创建成功的话重新添加到noRef中
     * @param container
     * @param noRef
     */
    public void handler(NumbContainer container, Set<FactoryMethodParseHandler> noRef ) {

        //多个注解存在按优先级最高的解析，以下是优先级关系
        Bean bean = method.getAnnotation(Bean.class);
        Icp icp = method.getAnnotation(Icp.class);
        ExceptionHandler eh = method.getAnnotation(ExceptionHandler.class);
        ViewHandler viewHandler = method.getAnnotation(ViewHandler.class);
        Controller controller = method.getAnnotation(Controller.class);
        Service service = method.getAnnotation(Service.class);
        String beanName = "";
        if (bean != null) {
            if (checkParamter()) {
                if (!BeanParseUtils.paseParams(Arrays.asList(this.parameters),container,this.params)) {
                    noRef.add(this);
                } else {
                    beanName = StrUtils.isEmpty(bean.value())? method.getName() : bean.value();
                    try {
                        if (params.isEmpty()) {
                            container.addBeanMeta(beanName, method.invoke(subObj));
                        } else {
                            container.addBeanMeta(beanName, method.invoke(subObj,params.toArray()));
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("添加bean"+ beanName + "失败", e );
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("添加bean"+ beanName + "失败", e );
                    }
                    ;
                }
            }
        } else if (icp != null) {
            if (checkParamter()) {
                if (!BeanParseUtils.paseParams(Arrays.asList(this.parameters),container,this.params)) {
                    noRef.add(this);
                }
                try {
                    container.addIcp(icp.value(),(NumbInterceptor) method.invoke(subObj, params.toArray()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("创建拦截器"+ method.getName() + "失败", e );
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("创建拦截器"+ method.getName() + "失败", e );
                }
            }
        } else if (eh != null) {
            if (checkParamter()) {
                if (!BeanParseUtils.paseParams(Arrays.asList(this.parameters),container,this.params)) {
                    noRef.add(this);
                }
                try {
                    container.setExceptionHandler((NumbExceptionHandler) method.invoke(subObj, params.toArray()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("创建全局异常处理"+ method.getName() + "失败", e );
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("创建全局异常处理"+ method.getName() + "失败", e );
                }
            }
        } else if (viewHandler != null) {
            if (checkParamter()) {
                if (!BeanParseUtils.paseParams(Arrays.asList(this.parameters),container,this.params)) {
                    noRef.add(this);
                }
                try {
                    container.setViewHandler((NumbViewHandler) method.invoke(subObj, params.toArray()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("创建视图处理器"+ method.getName() + "失败", e );
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("创建视图处理器"+ method.getName() + "失败", e );
                }
            }
        } else if (controller != null) {
            if (checkParamter()) {
                if (!BeanParseUtils.paseParams(Arrays.asList(this.parameters),container,this.params)) {
                    noRef.add(this);
                } else {
                    beanName = StrUtils.isEmpty(controller.value())? method.getName() : controller.value();
                    Object controllerObj = null;
                    try {
                        controllerObj = method.invoke(subObj,params.toArray());
                        container.addBeanMeta(beanName, controllerObj);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("创建控制器"+ beanName + "失败", e );
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("创建控制器"+ beanName + "失败", e );
                    }
                   BeanParseUtils.paseMapper(controllerObj,container);
                }
            }
        } else if (service != null) {
            if (checkParamter()) {
                if (!BeanParseUtils.paseParams(Arrays.asList(this.parameters),container,this.params)) {
                    noRef.add(this);
                } else {
                    beanName = StrUtils.isEmpty(service.value())? method.getName() : service.value();
                    try {
                        container.addBeanMeta(beanName, method.invoke(subObj,params.toArray()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("创建service"+ beanName + "失败", e );
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException("创建service"+ beanName + "失败", e );
                    }
                }
            }
        }

    }

    /**
     * 检查有没有refBean注解
     * @return
     */
    public boolean checkParamter() {
        if (parameters.length == 0) {
            return true;
        }
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotation(RefBean.class) == null) {
                throw new RuntimeException(subObj.getClass().getName() + "的" + method.getName() + "方法参数缺少注解");
            }
        }
        return true;
    }

    @Override
    public void init() {

    }

    @Override
    public void destory() {
        this.method = null;
        this.parameters = null;
        this.params = null;
        this.subObj = null;
    }





}
