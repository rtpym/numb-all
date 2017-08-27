package com.pym.numb.context.core;

import com.pym.numb.context.lifecycle.NumbLifecycle;
import com.pym.numb.context.parse.FactoryParseHandler;
import com.pym.numb.util.StrUtils;
import com.pym.numb.web.handler.NumbExceptionHandler;
import com.pym.numb.web.handler.NumbViewHandler;
import com.pym.numb.web.interceptor.NumbInterceptor;
import com.pym.numb.web.mapper.Mapper;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 容器类，容器中对象命名策略：注解中有名字就用注解中的名字，没有名字的话：1.对应工厂类方法名 2.类名
 */
public final class NumbContainer implements NumbLifecycle {
   // private static NumbContainer numbContainer = null;
    //映射路径
    private Map<String, Mapper> mappers = new HashMap<String, Mapper>();
    //拦截器
    private List<NumbInterceptor> interceptors = new LinkedList<NumbInterceptor>();
    //全局异常处理
    private NumbExceptionHandler exceptionHandler = null;
    //视图解析器
    private NumbViewHandler viewHandler = new NumbViewHandler();
    //对象池
    private Map<String, BeanMeta> beanContainer = new ConcurrentHashMap<String,BeanMeta>();
    private String[] factoryClassNames;
    private FactoryParseHandler factoryParseHandler;
    //对象池 name-object
  //  private Map<String, Object> namePool = new HashMap<String, Object>();
//    //对象池 class-object
  // public Map<Class<?>, Object> typePool = new HashMap<Class<?>, Object>();
    public NumbContainer(String... factoryClassNames) {
        this.factoryClassNames = factoryClassNames;
    }

    private void  doInit() {
        this.factoryParseHandler = new FactoryParseHandler(this,this.factoryClassNames);
        this.factoryParseHandler.parse();
    }

    public List<NumbInterceptor> getInterceptors() {
        return interceptors;
    }

    public  void addIcp(int i, NumbInterceptor numbInterceptor) {
        if (i <= 0) {
            interceptors.add(numbInterceptor);
        } else {
            interceptors.add(i,numbInterceptor);
        }
    }

    public NumbExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(NumbExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public NumbViewHandler getViewHandler() {
        return viewHandler;
    }

    public void setViewHandler(NumbViewHandler viewHandler) {
        this.viewHandler = viewHandler;
    }

    /**
     * 添加被管理对象
     * @param beanName
     * @param bean
     */
    public void addBeanMeta(String beanName, Object bean) {
       if (StrUtils.isEmpty(beanName) || bean == null) {
           throw new RuntimeException("name 和 实体类不能为空");
       }
       if (beanContainer.get(beanName) != null) {
           throw new RuntimeException("名字称为" + beanName + "的实体类已经存在");
       }
       beanContainer.put(beanName,new BeanMeta(beanName,bean));
    }

    public Map<String, Mapper> getMappers() {
        return mappers;
    }
    public boolean hasBean(String name) {
        return beanContainer.get(name) != null;
    }
    public <T> T getBean(String name, Class<T> type) {
        return (T) (beanContainer.get(name).getSubObj());
    }
    public <T> List<T> getBean(Class<T> type) {
       List<T> list = new ArrayList<T>();
       beanContainer.forEach((name, meta) -> {
           if (meta.isThisType(type)) {
               list.add((T)(meta.getSubObj()));
           }
       });
       return list;
    }
    public BeanMeta getBeanMeta(String name) {
        return beanContainer.get(name);
    }
    public List<BeanMeta> getBeanMeta(Type type) {
        List<BeanMeta> list = new ArrayList<BeanMeta>();
        beanContainer.forEach((name, beanMeta) -> {
            if (beanMeta.isThisType(type)) {
                list.add(beanMeta);
            }
        });
        return list;
    }
    public List<BeanMeta> getCompMeta(Class<?> cls) {
        List<BeanMeta> list = new ArrayList<BeanMeta>();
        beanContainer.forEach((name, beanMeta) -> {
            if (beanMeta.isThisType(cls)) {
                list.add(beanMeta);
            }
        });
        return list;
    }
    public void addMapper(String path, Mapper mapper) {
        this.mappers.put(path,mapper);
    }
    public boolean hasThisMapper(String path) {
        return this.mappers.get(path) != null;
    }


    @Override
    public void init() {
        doInit();
    }

    @Override
    public void destory() {
        this.factoryParseHandler.destory();
        this.factoryParseHandler = null;
        this.mappers.forEach((key,val) ->{
            if (val != null) {
                val.destory();
            }
        });
        this.mappers = null;
        this.exceptionHandler = null;
        this.viewHandler = null;
        this.interceptors = null;
        this.factoryClassNames = null;
        this.beanContainer.forEach((key,val) ->{
            if (val != null) {
                val.destory();
            }
        });
        this.beanContainer = null;
    }
}
