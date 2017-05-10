package com.jhj.demo.material.util;

/**
 * @author HuaJian Jiang.
 *         Date 2017/1/6.
 */
public class ViewUtil {
    private static final String TAG = ViewUtil.class.getSimpleName();

    public static boolean objectEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
