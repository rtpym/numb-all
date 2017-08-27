package com.pym.numb.web.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/18.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ReqParam {
    String value() default "";
}
