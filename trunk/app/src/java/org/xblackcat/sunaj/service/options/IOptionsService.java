package org.xblackcat.sunaj.service.options;

/**
 * Date: 13 ��� 2007
 *
 * @author Alexey
 */

public interface IOptionsService {
	/**
	 * Returns the property value.
	 *
	 * @param name name of the property.
	 *
	 * @return property value.
	 *
	 * @throws NullPointerException if property name is <code>null</code>.
	 */
	<T> T getProperty(Property<T> name);

	/**
	 * Sets the new value of the property.
	 *
	 * @param name property to set.
	 * @param newValue new value to set.
	 *
	 * @return old value for the property.
	 *
	 * @throws NullPointerException if property name is <code>null</code>.
	 */
	<T> T setProperty(Property<T> name, T newValue);
}
