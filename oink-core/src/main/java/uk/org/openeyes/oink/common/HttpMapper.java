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
package uk.org.openeyes.oink.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import uk.org.openeyes.oink.domain.HttpMethod;

//TODO Make class for a resource path instead of a plain string

/**
 * A matcher that matches Resource paths {@link http://en.wikipedia.org/wiki/Uniform_resource_locator } and HTTP Method types to a value.
 * An entry's resource path can end in the * wildcard.
 * An entry's {@link HttpMethod}, if omitted or null, implies any HttpMethod.
 * When queried, will always return the most explicit match.
 * The query resource path may not have wildcards and the method type may not be omitted.
 * 
 * @author Oli
 *
 * @param <T>
 */
public class HttpMapper<T> {
	
	private final List<HttpMapperEntry<T>> list;
	
	public HttpMapper(List<HttpMapperEntry<T>> list) {
		this.list = list;
	}
	
	/**
	 * Finds the most explicit match from a list of wildcard matches.
	 * The most explicit match is the one with the longest path. 
	 * If two matches have the longest path then the first match to 
	 * have a non-null method is considered to be the most explicit. 
	 * @param matches
	 * @return
	 * 
	 * For example 
	 * *
	 * Pat*
	 * Patient ALL
	 * Patient GET
	 * Patient* ALL
	 * 
	 * Most explicit is Patient
	 * 
	 */
	private HttpMapperEntry<T> getMostExplicitMatch(List<HttpMapperEntry<T>> matches) {
		HttpMapperEntry<T> currBest = matches.get(0);
		
		// Iterate over list
		for (HttpMapperEntry<T> elem : matches) {
			String currResourcePath = elem.getUri();
			HttpMethod currMethodPath = elem.getMethod();
			
			String currBestResourcePath = currBest.getUri();
			
			if (isExplicit(currResourcePath)) {
				currBest = elem;
				if (currMethodPath != null) {
					// We've got as explicit as we can get so stop
					break;
				}
			} else {
				if (isWildcard(currBestResourcePath) && currResourcePath.length() > currBestResourcePath.length()) {
					currBest = elem;
				}
			}
		}
		return currBest;
	}
	
	private boolean isWildcard(String path) {
		return path.endsWith("*");
	}
	
	private boolean isExplicit(String path) {
		return !isWildcard(path);
	}
	
	private List<HttpMapperEntry<T>> getPossibleMatches(String resourcePath, HttpMethod method) {
		List<HttpMapperEntry<T>> result = new LinkedList<HttpMapperEntry<T>>();
		// Iterate over list
		for (HttpMapperEntry<T> elem : list) {
			String currResourcePath = elem.getUri();
			HttpMethod currMethodPath = elem.getMethod();
			
			// Check if there is a match
			if (checkMethodMath(currMethodPath, method) && checkResourceMatch(currResourcePath, resourcePath)) {
				result.add(elem);
			}
		}
		
		return result;
	}
	
	/**
	 * Check if a given resourcePath matches against a resourcePath in our matcher, 
	 * given that the resourcePath in the matcher could contain a wildcard.
	 * @param resourcePathInMap
	 * @param resourcePath
	 * @return
	 */
	private boolean checkResourceMatch(String resourcePathInMap, String resourcePath) {
		if (resourcePathInMap.endsWith("*")) {
			// Handle wildcard
			String prefix = resourcePathInMap.substring(0, resourcePathInMap.length()-1);
			String pattern = Pattern.quote(prefix) + "(.)*";
			// Check resource path matches
			return Pattern.matches(pattern, resourcePath);
		} else {
			// Treat as literal
			return resourcePathInMap.equals(resourcePath);
		}
	}
	
	private boolean checkMethodMath(HttpMethod methodInMap, HttpMethod method) {
		if (methodInMap == null || methodInMap == HttpMethod.ANY) {
			// Treat as wildcard
			return true;
		} else {
			// Treat as literal
			return methodInMap.equals(method);
		}
	}
	
	public T get(String resourcePath, HttpMethod method) {
		List<HttpMapperEntry<T>> matches = getPossibleMatches(resourcePath, method);
		if (matches.isEmpty()) {
			return null;
		} else {
			HttpMapperEntry<T> closestMatch = getMostExplicitMatch(matches);
			return closestMatch.getValue();
		}
	}
	
	public static class Builder<T> {
		
		ArrayList<HttpMapperEntry<T>> list;
		
		public Builder() {
			list = new ArrayList<HttpMapperEntry<T>>();
		}
		
		public Builder<T> addMapping(String resourcePath, HttpMethod status, T value) {
			list.add(new HttpMapperEntry<T>(resourcePath, status, value));
			return this;
		}
		
		public HttpMapper<T> build() {
			return new HttpMapper<T>(list);
		}
		
	}
}
