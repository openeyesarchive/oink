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
package uk.org.openeyes.oink.datagen.generators.person.beans.uk;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.springframework.util.StringUtils;

/**
 * Joda time formatter to parse dates in monthly update files from the HSCIC.
 *
 */
public class NHSDateFormatter {

	public static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.appendYear(4, 4).appendMonthOfYear(2)
			.appendDayOfMonth(2).toFormatter();
	
	public static DateTime parse(String value) {
		if(StringUtils.hasText(value)) {
			return DateTime.parse(value, formatter);
		}
		return null;
	}
}
