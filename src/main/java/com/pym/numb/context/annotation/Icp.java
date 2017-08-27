package com.pym.numb.context.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/22.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Icp {
    //0表示没有优先级 会按照先后顺序来排位置
    int value() default 0;
}
