package com.pym.numb.web.annotation;

import java.lang.annotation.*;

/**
 * Created by Administrator on 2017/8/18.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JsonRes {

}
