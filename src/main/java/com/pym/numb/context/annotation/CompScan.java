package com.pym.numb.context.annotation;

import com.pym.numb.context.support.CompType;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/22.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CompScan {
    //组件类型
    CompType[] compType();
    //包前缀
    String bpk();

}
