package com.pym.numb.web.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/18.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ReqMapping {
    String value() default "";
}
