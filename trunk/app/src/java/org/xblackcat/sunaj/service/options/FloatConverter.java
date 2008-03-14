package org.xblackcat.sunaj.service.options;

/**
 * Date: 28 ��� 2008
 *
 * @author xBlackCat
 */

class FloatConverter extends AScalarConverter<Float> {
    public Float convert(String s) {
        try {
            if (s != null) {
                return Float.parseFloat(s);
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}