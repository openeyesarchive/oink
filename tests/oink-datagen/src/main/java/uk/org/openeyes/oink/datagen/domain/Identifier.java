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

package uk.org.openeyes.oink.datagen.domain;

import org.springframework.util.StringUtils;

/**
 * This class represents a patient identifier from a particular system and with a defined usage.
 *
 */
public class Identifier extends CodedValue {
	
	public Identifier() {
		
	}
	
	public Identifier(String usage, String codeSystem, String value) {
		this.setUsage(usage);
		this.setValue(value);
		this.setCodeSystem(codeSystem);
	}
	
	public Identifier(String codeSystem, String value) {
		this.setValue(value);
		this.setCodeSystem(codeSystem);
	}
	
	@Override
	public String toString() {
		String value = "(";
		
		if(StringUtils.hasText(getUsage())) {
			value += getUsage();
		}
		if(StringUtils.hasText(getCodeSystem())) {
			if(value.length() > 1) {
				value += ",";
			}
			value += getCodeSystem();
		}
		if(StringUtils.hasText(getValue())) {
			if(value.length() > 1) {
				value += ",";
			}
			value += getValue();
		}
		
		value += ")";
		
		return value;
	}
}
