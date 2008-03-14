package org.xblackcat.sunaj.service.options;

/**
 * Date: 28 ��� 2008
 *
 * @author xBlackCat
 */

class DoubleConverter extends AScalarConverter<Double> {
    public Double convert(String s) {
        try {
            if (s != null) {
                return Double.parseDouble(s);
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}