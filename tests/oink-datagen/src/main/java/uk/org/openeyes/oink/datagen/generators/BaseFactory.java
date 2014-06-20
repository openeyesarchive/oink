/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.datagen.generators;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;

/**
 * A simple factory class which maintains a String keyed list of instances.
 *
 * @param <T> The instance type.
 */
public class BaseFactory<T> {

	private Object lock = new Object();
	private Map<String, T> instances = new HashMap<String, T>();

	/**
	 * Get an instance with a particular key.
	 * 
	 * @param key The key of the instance.
	 * @param errorMessage The error message to throw if not found.
	 * @return The instance singleton.
	 * @throws Exception An exception is thrown if the key does not exist.
	 */
	public T getInstanceReference(String key, String errorMessage) throws Exception {
		key = key.toLowerCase();
		synchronized (lock) {
			if(!instances.containsKey(key)) {
				T instance = createInstance(key);
				instances.put(key, instance);
				if(instance == null) {
					throw new Exception(errorMessage);
				}
			}
			return (T)instances.get(key);
		}
	}

	/**
	 * Override this message to return a new instance for a given key. This will only be
	 * called when the key does not exist, i.e. there is no instance for the key.
	 * @param key The key of the instance.
	 * @return A new instance which will become the singleton. Null if not created.
	 */
	protected T createInstance(String key) {
		return null;
	}

	/**
	 * Replaces the instance for a given key.
	 * @param key The key of the instance.
	 * @param instance The instance to replace the current singleton with.
	 */
	public void setInstanceReference(String key, T instance) {
		synchronized (lock) {
			if(instance != null) {
				instances.put(key, instance);
			} else {
				throw new NullArgumentException("instance");
			}
		}
	}
}
