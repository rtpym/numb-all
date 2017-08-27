package com.pym.numb.context.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/22.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Service {
    String value() default "";
}
