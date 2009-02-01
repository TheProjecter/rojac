package org.xblackcat.rojac.util;

import org.flexdock.util.SwingUtility;
import org.xblackcat.rojac.data.IRSDNable;

import javax.swing.*;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Date: 27 ���� 2008
 *
 * @author xBlackCat
 */

public final class RojacUtils {
    private RojacUtils() {
    }

    public static <T extends Serializable> T[] getRSDNObject(IRSDNable<T>[] ar) {
        Class<T> c = (Class<T>) ((ParameterizedType) ar.getClass().getComponentType().getGenericInterfaces()[0]).getActualTypeArguments()[0];

        List<T> res = new ArrayList<T>(ar.length);

        for (IRSDNable<T> o : ar) {
            res.add(o.getRSDNObject());
        }

        T[] a = (T[]) Array.newInstance(c, ar.length);
        return res.toArray(a);
    }

    public static String construstDebugSQL(String sql, Object... params) {
        String query = sql;

        for (Object o : params) {
            String str;
            if (o instanceof String) {
                str = "'" + Matcher.quoteReplacement(o.toString()) + "'";
            } else {
                str = Matcher.quoteReplacement(o.toString());
            }
            query = query.replaceFirst("\\?", str);
        }

        return query;
    }

    /*
    * Util methods for converting values.
    */
    public static <T extends Enum<T>> T convertToEnum(Class<T> enumClass, String val) {
        if (val != null) {
            return Enum.valueOf(enumClass, val);
        } else {
            return null;
        }
    }

    public static void setLookAndFeel(LookAndFeel laf) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(laf);
        SwingUtility.setPlaf(laf.getClass());
    }

//    public static <T, E> T[] extract(E[] arr, IExtractor<T, E> e) {
//
//    }
}
