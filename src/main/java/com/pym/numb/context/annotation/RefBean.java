package com.pym.numb.context.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/22.
 */
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RefBean {
    String value();
}
