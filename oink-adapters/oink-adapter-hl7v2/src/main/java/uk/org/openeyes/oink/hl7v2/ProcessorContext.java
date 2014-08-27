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
package uk.org.openeyes.oink.hl7v2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows tests to track the progress of a process.
 *  
 */
public class ProcessorContext {
	
	private List<Object> contextHistory = new ArrayList<Object>();

	public List<Object> getContextHistory() {
		return contextHistory;
	}
	
	public void addToContextHistory(Object o) {
		contextHistory.add(o);
	}
}
