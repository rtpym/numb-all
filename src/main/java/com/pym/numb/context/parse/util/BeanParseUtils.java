package com.pym.numb.context.parse.util;

import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.context.annotation.RefBean;
import com.pym.numb.context.core.BeanMeta;
import com.pym.numb.util.StrUtils;
import com.pym.numb.web.annotation.ReqMapping;
import com.pym.numb.web.mapper.Mapper;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/8/26.
 */
public class BeanParseUtils {
    public  static boolean checkParameters(List<Parameter> parameters, Class cls) {
        if (parameters.isEmpty()) {
            return true;
        }
        for (Parameter parameter : parameters) {
            if (parameter.getAnnotation(cls) == null) {
                return false;
            }
        }
        return true;
    }
    
    public static void paseMapper(Object controllerObj, NumbContainer container) {
        String cpath = "";
        ReqMapping clsRm = controllerObj.getClass().getAnnotation(ReqMapping.class);
        if (clsRm != null) {
            cpath = clsRm.value().trim();
            if (StrUtils.isEmpty(cpath)) {
                cpath = "";
            } else {
                cpath = cpath.startsWith("/") ? cpath : ("/" + cpath);
            }
        }
        final String c_path = cpath;
        Arrays.asList(controllerObj.getClass().getMethods()).forEach(method -> {
            ReqMapping mRm = method.getAnnotation(ReqMapping.class);
            if (mRm != null) {
                String mpath = mRm.value().trim();
                if (StrUtils.isEmpty(mpath)) {
                    mpath = "";
                } else {
                    mpath = mpath.startsWith("/") ? mpath : ("/" + mpath);
                }
                String mkey = c_path + mpath;
                if (container.hasThisMapper(mkey)) {
                    throw new RuntimeException(mkey + "路径重复！");
                }
                container.addMapper(mkey, new Mapper(controllerObj,method,mkey));
            }
        });
    }
    public static boolean paseParams(List<Parameter> parameters, NumbContainer container, List<Object> params) {
        params.clear();
        if (parameters.isEmpty()) {
            return true;
        }
        for (Parameter parameter : parameters) {
            RefBean refBean = parameter.getAnnotation(RefBean.class);
            String refName = refBean.value();
            if (StrUtils.isEmpty(refName)) {
                //没有名称按照自动注入
                //1.将类名首字母小写作为name查找
                Class<?> pCls = parameter.getType();
                BeanMeta bm = container.getBeanMeta(StrUtils.lowerFirstChar(pCls.getSimpleName()));
                if (bm != null && bm.isThisType(pCls) && bm.isThisType(parameter.getParameterizedType())) {
                    params.add(bm.getSubObj());
                } else {
                    List<BeanMeta> bmsByCls = container.getBeanMeta(pCls);
                    boolean ok = false;
                    for (BeanMeta beanMeta : bmsByCls) {
                        if (beanMeta.isThisType(parameter.getParameterizedType())) {
                            params.add(pCls.cast(bm.getSubObj()));
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        return false;
                    }

                }
            } else {
                //有名称 按照名称注入
                if (container.hasBean(refName)) {
                    params.add(container.getBean(refName, parameter.getType()));
                } else {
                    return false;
                }
            }
        }
        return true;
    }

}
