package com.pym.numb.util;

/**
 * Created by Administrator on 2017/8/18.
 */
public class StrUtils {

    public static boolean isEmpty(String str) {
       return str==null || str.trim().length() == 0;
    }
    public static String lowerFirstChar(String str) {
        return upperOrLowerChar(str,0,false);
    }
    public static String upperFirstChar(String str) {
        return upperOrLowerChar(str,0,true);
    }
    public static String upperOrLowerChar(String str, int index, boolean upper) {
        if (str == null) {
            throw new NullPointerException("字符串不能为空");
        }
        char[] chars = str.toCharArray();
        char c = chars[index];
        if (upper) {
            if ('a' <= c && c <= 'z') {
                c = (char)(c - 32);
            }
        } else {
            if ('A' <= c && c <= 'Z') {
                c = (char)(c + 32);
            }
        }
        chars[index] = c;
        return new String(chars);
    }
}
