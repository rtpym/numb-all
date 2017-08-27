package com.pym.numb.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/18.
 */
public class ObjUtils {

    public static Map<String, Object> objToMap(Object obj) {
        if (obj == null) {
            throw new NullPointerException("对象不能null");
        }
       // Method[] methods = obj.getClass().getMethods();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Method> setters = new HashMap<String, Method>();
        Map<String, Method> getters = new HashMap<String, Method>();
        Arrays.asList(obj.getClass().getMethods()).forEach(
                method -> {
                    String name = method.getName();
                    int pc = method.getParameterCount();
                    if (name.startsWith("get") && pc == 0) {
                        getters.put(StrUtils.lowerFirstChar(
                                name.replaceFirst("get","")),method);
                    } else if (name.startsWith("set") && pc == 1) {
                        setters.put(StrUtils.lowerFirstChar(name.replaceFirst("set","")), method);
                    }
                });
        getters.forEach((key,method) -> {
            try {
                if (setters.containsKey(key)) {
                    map.put(key, method.invoke(obj));
                }
            } catch (Exception e) {
                throw new RuntimeException("obj转map出错");
            }

        });
        return map;
    }
    public  static  <T> T mapToObj(Map<String, Object> map, Class<T> cls) {
        if (map == null) {
            throw new NullPointerException("map 不能为空");
        }
        if (cls == null) {
            throw new NullPointerException("必须指明对象类型");
        }
        try {
            Object obj = cls.newInstance();
            Arrays.asList(cls.getMethods()).forEach(method -> {
                String name = method.getName();
                int pc = method.getParameterCount();
                if (name.startsWith("set") && pc == 1) {
                    name = StrUtils.lowerFirstChar(name.replaceFirst("set", ""));
                    if (map.containsKey(name)) {
                       try {
                           method.invoke(obj,method.getParameterTypes()[0].cast(map.get(name)));
                       } catch (Exception e) {
                           throw  new RuntimeException("map 转对象异常");
                       }
                    }
                }
            });
            return  (T)obj;
        } catch (Exception e) {
            throw new RuntimeException("map转对象异常", e);
        }
    }
    public static <T> T mapToObjForReq(Map<String, String> map, Class<T> cls) {
        if (map == null) {
            throw new NullPointerException("map 不能为空");
        }
        if (cls == null) {
            throw new NullPointerException("必须指明对象类型");
        }
        try {
            Object obj = cls.newInstance();
            Arrays.asList(cls.getMethods()).forEach(method -> {
                String name = method.getName();
                int pc = method.getParameterCount();
                if (name.startsWith("set") && pc == 1) {
                    name = StrUtils.lowerFirstChar(name.replaceFirst("set", ""));
                    if (map.containsKey(name)) {
                        try {
                            String pv_str = map.get(name);
                            Class p_cls = method.getParameterTypes()[0];
                            Object pv_obj =  pv_str;;
                            if (p_cls.equals(Integer.class)) {
                                pv_obj = Integer.parseInt(pv_str);
                            } else if (p_cls.equals(Long.class)) {
                                pv_obj = Long.parseLong(pv_str);
                            } else if (p_cls.equals(Double.class)) {
                                pv_obj = Double.parseDouble(pv_str);
                            } else if (p_cls.equals(Float.class)) {
                                pv_obj = Float.parseFloat(pv_str);
                            }
                            method.invoke(obj,p_cls.cast(pv_obj));
                        } catch (Exception e) {
                            throw  new RuntimeException("map 转对象异常");
                        }
                    }
                }
            });
            return  (T)obj;
        } catch (Exception e) {
            throw new RuntimeException("map转对象异常", e);
        }
    }
}
