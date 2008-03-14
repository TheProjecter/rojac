package org.xblackcat.sunaj.service.options;

/**
 * Date: 28 ��� 2008
 *
 * @author xBlackCat
 */

class IntegerConverter extends AScalarConverter<Integer> {
    public Integer convert(String s) {
        try {
            if (s != null) {
                return Integer.decode(s);
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}