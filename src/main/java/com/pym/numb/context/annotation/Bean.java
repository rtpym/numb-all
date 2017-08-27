package com.pym.numb.context.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/22.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Bean {
    String value() default "";
}
