package org.xblackcat.rojac.service.options.converter;

/**
 * Date: 28 ��� 2008
 *
 * @author xBlackCat
 */

public class ByteConverter extends AScalarConverter<Byte> {
    public Byte convert(String s) {
        try {
            if (s != null) {
                return Byte.decode(s);
            } else {
                return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
}