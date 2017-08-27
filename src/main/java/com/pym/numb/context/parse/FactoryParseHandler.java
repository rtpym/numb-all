package com.pym.numb.context.parse;

import com.pym.numb.context.annotation.*;
import com.pym.numb.context.core.BeanMeta;
import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.context.lifecycle.NumbLifecycle;
import com.pym.numb.util.StrUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 解析策略：包扫描全部解析成BeanMeta对象，工厂类方法全解析成FactoryMethodParseHandler对象
 */
public class FactoryParseHandler implements NumbLifecycle{

    private NumbContainer numbContainer;
    private String[] factoryClass;
    private Set<FactoryMethodParseHandler> methodPaseHandlers = new HashSet<FactoryMethodParseHandler>();
    private Set<BeanMeta> beanMetas = new HashSet<BeanMeta>();

    public FactoryParseHandler(NumbContainer numbContainer,String...factoryClass) {
        this.numbContainer = numbContainer;
       this.factoryClass = factoryClass;
    }

    public void parse() {
        if (factoryClass == null || factoryClass.length == 0) {
            throw new RuntimeException("缺少工厂类");
        }
        List<CompScan> compScanList = new ArrayList<CompScan>();
        List<Class<?>> factorys = new ArrayList<Class<?>>();
        for (String str : factoryClass) {
            Class<?> cls;
            try {
                cls = Thread.currentThread().getContextClassLoader().loadClass(str);
            } catch (ClassNotFoundException e) {
                throw  new RuntimeException("找不到工厂类"+str);
            }
            CompScan cs = cls.getAnnotation(CompScan.class);
            if (cs != null) {
                compScanList.add(cs);
            }
            factorys.add(cls);
        }
        parseCompSacn(compScanList);
        parseFactorys(factorys);
        //----最多解析10次
        for (int i=0; i < 10; i++) {
            if (this.methodPaseHandlers.isEmpty() && this.beanMetas.isEmpty()) {
                return;
            }
            doInstanceObj();
        }
        if (!methodPaseHandlers.isEmpty()) {
            FactoryMethodParseHandler fm = (FactoryMethodParseHandler) this.methodPaseHandlers.toArray()[0];
            throw new RuntimeException(fm.getSubObj().getClass().getName() + "的"+ fm.getMethod().getName() + "找不到引用对象");
        }
        if (!beanMetas.isEmpty()) {
            BeanMeta bem = (BeanMeta) beanMetas.toArray()[0];
            throw new RuntimeException(bem.getSubCls().getName() + "找不到引用对象");
        }
    }

    private void parseCompSacn(List<CompScan> compScanList) {
        compScanList.forEach(compScan -> {
            String bpk = compScan.bpk();
            if (!StrUtils.isEmpty(bpk)) {
                beanMetas.addAll(CompScanPaseHandler.handler(bpk,compScan.compType()));
            }
        });
    }
    private void parseFactorys(List<Class<?>> factorys) {
        factorys.forEach(cls ->{
            Object obj;
            try {
               obj = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("实例化工厂类"+ cls.getName() + "失败", e);
            }
            for (Method method : cls.getMethods()) {
                if (method.getAnnotation(Bean.class) != null
                        || method.getAnnotation(Controller.class) != null
                        || method.getAnnotation(Service.class) != null
                        || method.getAnnotation(ViewHandler.class) != null
                        || method.getAnnotation(ExceptionHandler.class) != null
                        || method.getAnnotation(Icp.class) != null) {
                    methodPaseHandlers.add(new FactoryMethodParseHandler(obj,method));
                }
            }
        });
    }
    private void doInstanceObj() {
        Set<Object> set = new HashSet<Object>();
        set.addAll(this.beanMetas);
        set.addAll(this.methodPaseHandlers);
        Set<BeanMeta> noRef_bm = new HashSet<BeanMeta>();
        Set<FactoryMethodParseHandler> noRef_fmph = new HashSet<FactoryMethodParseHandler>();
        for (Object obj : set) {
            if (obj instanceof BeanMeta) {
                BeanMeta bm = (BeanMeta)obj;
                if (!bm.instanceSubObj(this.numbContainer)) {
                    noRef_bm.add(bm);
                }
            } else {
                FactoryMethodParseHandler fmph = (FactoryMethodParseHandler)obj;
                fmph.handler(this.numbContainer,noRef_fmph);
            }
        }
        this.beanMetas = noRef_bm;
        this.methodPaseHandlers = noRef_fmph;
    }


    @Override
    public void init() {

    }

    @Override
    public void destory() {
        this.numbContainer = null;
        this.beanMetas.forEach(nm ->{
            nm.destory();
        });
        this.beanMetas = null;
        this.factoryClass = null;
        this.methodPaseHandlers.forEach(mph ->{
            mph.destory();
        });
        this.methodPaseHandlers = null;
    }
}
