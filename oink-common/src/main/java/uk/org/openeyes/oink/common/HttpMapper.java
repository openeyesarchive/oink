package uk.org.openeyes.oink.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Tuple;

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
	
	private final List<Triplet<String, HttpMethod, T>> list;
	
	public HttpMapper(List<Triplet<String, HttpMethod, T>> list) {
		this.list = list;
	}
	
	public List<Pair<String, HttpMethod>> getHttpKey() {
		List<Pair<String,HttpMethod>> result = new LinkedList<Pair<String,HttpMethod>>();
		for (Triplet<String, HttpMethod, T> entry: list) {
			result.add(new Pair<String, HttpMethod>(entry.getValue0(), entry.getValue1()));
		}
		return result;
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
	private Triplet<String, HttpMethod, T> getMostExplicitMatch(List<Triplet<String, HttpMethod, T>> matches) {
		Triplet<String, HttpMethod, T> currBest = matches.get(0);
		
		// Iterate over list
		for (Triplet<String, HttpMethod, T> elem : matches) {
			String currResourcePath = elem.getValue0();
			HttpMethod currMethodPath = elem.getValue1();
			
			String currBestResourcePath = currBest.getValue0();
			
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
	
	private List<Triplet<String, HttpMethod, T>> getPossibleMatches(String resourcePath, HttpMethod method) {
		List<Triplet<String, HttpMethod, T>> result = new LinkedList<Triplet<String,HttpMethod,T>>();
		// Iterate over list
		for (Triplet<String, HttpMethod, T> elem : list) {
			String currResourcePath = elem.getValue0();
			HttpMethod currMethodPath = elem.getValue1();
			
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
		List<Triplet<String, HttpMethod, T>> matches = getPossibleMatches(resourcePath, method);
		if (matches.isEmpty()) {
			return null;
		} else {
			Triplet<String, HttpMethod, T> closestMatch = getMostExplicitMatch(matches);
			return closestMatch.getValue2();
		}
	}
	
	public static class Builder<T> {
		
		ArrayList<Triplet<String,HttpMethod,T>> list;
		
		public Builder() {
			list = new ArrayList<Triplet<String,HttpMethod,T>>();
		}
		
		public Builder<T> addMapping(String resourcePath, HttpMethod status, T value) {
			list.add(new Triplet<String, HttpMethod, T>(resourcePath, status, value));
			return this;
		}
		
		public HttpMapper<T> build() {
			return new HttpMapper<T>(list);
		}
		
	}
}
