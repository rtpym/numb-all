package com.pym.numb.context.core;

import com.pym.numb.context.annotation.Icp;
import com.pym.numb.context.annotation.RefBean;
import com.pym.numb.context.lifecycle.NumbLifecycle;
import com.pym.numb.context.parse.util.BeanParseUtils;
import com.pym.numb.context.support.CompType;
import com.pym.numb.util.StrUtils;
import com.pym.numb.web.handler.NumbExceptionHandler;
import com.pym.numb.web.handler.NumbViewHandler;
import com.pym.numb.web.interceptor.NumbInterceptor;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Administrator on 2017/8/26.
 */
public class BeanMeta implements NumbLifecycle{
    /**
     * 名称
     */
    private String subName;
    /**
     * 目标对象
     */
    private Object subObj;
    /**
     * 目标类
     */
    private Class<?> subCls;

    private CompType compType;
    private List<Field> fieldList;
    private List<Method> methodList;
    private Set<Type> genericIntefaces;
    private Set<Type> genericSupperClass;
    private Set<Class<?>> allSupAndInf;
    public BeanMeta(String subName, Object subObj) {
        this(subName,subObj,subObj.getClass());
    }

    public BeanMeta(String subName, Class<?> subCls) {
        this(subName,null,subCls);
    }

    public BeanMeta(String subName, Object subObj, Class<?> subCls) {
        this.subObj = subObj;
        this.subName = subName;
        this.subCls = subCls;
        this.genericIntefaces = new HashSet<Type>(Arrays.asList(this.subCls.getGenericInterfaces()));
        this.methodList = Arrays.asList(this.subCls.getMethods());
        this.fieldList = getAllFiled(this.subCls);
        this.genericSupperClass = getAllGSCs(this.subCls);
        this.genericIntefaces = getAllGIs(this.subCls);
        this.allSupAndInf = getAllSupAndInf(this.subCls);
    }
    private List<Field> getAllFiled(Class<?> clszz) {
        List<Field> list = new ArrayList<Field>();
        if (clszz == null) {
            return list;
        }
        list.addAll(Arrays.asList(clszz.getDeclaredFields()));
        Class<?> sup = clszz.getSuperclass();
        if (sup != null && !sup.equals(Object.class)) {
            list.addAll(getAllFiled(sup));
        }
        return list;
    }
    private Set<Type> getAllGIs(Class<?> clszz) {
        Set<Type> set = new HashSet<Type>();
        if (clszz == null) {
            return set;
        }
        Type[] infTypes = clszz.getGenericInterfaces();
        if (infTypes !=null && infTypes.length != 0) {
            set.addAll(Arrays.asList(infTypes));
        }
        Class<?>[] infs = clszz.getInterfaces();
        if (infs != null && infs.length != 0) {
            Arrays.asList(infs).forEach(inf -> {
                set.addAll(getAllGIs(inf));
            });
        }
        Class<?> sup = clszz.getSuperclass();
        if (sup != null) {
            set.addAll(getAllGIs(sup));
        }
        return set;
    }
    private Set<Type> getAllGSCs(Class<?> clszz) {
        Set<Type> set = new HashSet<Type>();
        if (clszz == null) {
            return set;
        }
        Type t = clszz.getGenericSuperclass();
        if (t != null) {
            set.add(t);
        }
        Class<?> sup = clszz.getSuperclass();
        if (sup != null) {
            set.addAll(getAllGSCs(sup));
        }
        return set;
    }
    private Set<Class<?>> getAllSupAndInf(Class<?> clsszz) {
        Set<Class<?>> set = new HashSet<Class<?>>();
        if (clsszz == null) {
            return set;
        }
        set.add(clsszz);
        Class<?>[] infs = clsszz.getInterfaces();
        if (infs != null && infs.length != 0) {
            for (Class<?> inf : infs) {
                set.addAll(getAllSupAndInf(inf));
            }
        }
        Class<?> sup = clsszz.getSuperclass();
        if (sup != null) {
            set.addAll(getAllSupAndInf(sup));
        }
        return set;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeanMeta beanMeta = (BeanMeta) o;

        if (subName != null ? !subName.equals(beanMeta.subName) : beanMeta.subName != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = subName != null ? subName.hashCode() : 0;
        //result = 31 * result + (subCls != null ? subCls.hashCode() : 0);
        return result;
    }

    public String getSubName() {
        return subName;
    }

    public Object getSubObj() {
        return subObj;
    }

    public Class<?> getSubCls() {
        return subCls;
    }

    public List<Field> getFieldList() {
        return fieldList;
    }

    public List<Method> getMethodList() {
        return methodList;
    }

    public Set<Type> getGenericIntefaces() {
        return genericIntefaces;
    }

    public Set<Type> getGenericSupperClass() {
        return genericSupperClass;
    }

    public boolean isThisType(Class<?> cls) {
        return this.allSupAndInf.contains(cls);
    }
    public boolean isThisType(Type type) {
        if (type == this.subCls) {
            return true;
        }
        return this.genericIntefaces.contains(type)
                || this.genericSupperClass.contains(type);
    }

    public CompType getCompType() {
        return compType;
    }

    public BeanMeta compType(CompType compType) {
        this.compType = compType;
        return this;
    }

    public boolean instanceSubObj(NumbContainer container) {
        //修复普通对象注入不成功的bug，普通对象没有泛型父类
        if (this.subObj != null) {
            return true;
        }

        if (container == null) {
            throw new RuntimeException("请传入容器");
        }
        Constructor<?> constructor = this.subCls.getConstructors()[0];
        Object obj = null;
        if (constructor.getParameterCount() == 0) {
            try {
                obj = this.subCls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("实例化失败", e);
            }
        } else {
            Parameter[] parameters = constructor.getParameters();
            List<Parameter> parameterList = Arrays.asList(parameters);
            if (!BeanParseUtils.checkParameters(parameterList,RefBean.class)) {
                throw new RuntimeException(constructor.getName() + "缺少RefBean注解");
            }
            List<Object> params = new ArrayList<Object>();
           if (!BeanParseUtils.paseParams(parameterList,container,params)) {
               return false;
           }
            try {
                obj = constructor.newInstance(params.toArray());
            } catch (Exception e) {
               throw new RuntimeException("实例化失败", e);
            }
        }
        for (Field field : fieldList) {
            field.setAccessible(true);
            RefBean rb = field.getAnnotation(RefBean.class);
            if (rb != null) {
                String refName = rb.value();
                if (StrUtils.isEmpty(refName)) {
                    Class<?> cls = field.getType();
                    List<BeanMeta> bmsByCls = container.getBeanMeta(cls);
                    boolean ok = false;
                    for (BeanMeta beanMeta : bmsByCls) {

                        if (beanMeta.isThisType(field.getGenericType())) {
                            try {
                                field.set(obj,cls.cast(beanMeta.getSubObj()));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("参数注入失败", e);
                            }
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        return false;
                    }
                } else {
                    try {
                        if (!container.hasBean(refName)) {
                            return false;
                        }
                        field.set(obj,container.getBean(refName,field.getType()));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException( "参数注入失败",e);
                    }
                }
            }
        }
        this.subObj = obj;
        save(container);
        return true;
    }
    private void save (NumbContainer container) {
        if (this.compType == null) {
            return;
        }
        if (this.compType.equals(CompType.BEAN)) {
            container.addBeanMeta(this.subName,this);
        } else if(this.compType.equals(CompType.CONTROLLER)) {
            container.addBeanMeta(this.subName, this);
            BeanParseUtils.paseMapper(this.subObj,container);
        } else if (this.compType.equals(CompType.EXCEPTION_HANDLER)) {
            container.setExceptionHandler((NumbExceptionHandler)(this.subObj));
        } else if (this.compType.equals(CompType.VIEW_HANDLER)) {
            container.setViewHandler((NumbViewHandler)(this.subObj));
        } else if (this.compType.equals(CompType.SERVICE)) {
            container.addBeanMeta(subName,this);
        } else if (this.compType.equals(CompType.ICP)) {
            container.addIcp(this.subCls.getAnnotation(Icp.class).value(),(NumbInterceptor)(this.subObj));
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void destory() {
        this.allSupAndInf = null;
        this.compType = null;
        this.fieldList = null;
        this.genericIntefaces = null;
        this.genericSupperClass = null;
        this.methodList = null;
        this.subCls = null;
        this.subName = null;
        this.subObj = null;
    }
}
