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
 * This class represents a patient address.
 */
public class Address {

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getLine3() {
		return line3;
	}

	public void setLine3(String line3) {
		this.line3 = line3;
	}

	public String getLine4() {
		return line4;
	}

	public void setLine4(String line4) {
		this.line4 = line4;
	}

	public String getLine5() {
		return line5;
	}

	public void setLine5(String line5) {
		this.line5 = line5;
	}
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	private String usage;

	private String line1;
	private String line2;
	private String line3;
	private String line4;
	private String line5;
	private String country;

	private String zipCode;
	
	@Override
	public String toString() {
		String value = "(";
		
		if(StringUtils.hasText(usage)) {
			value += usage;
		}
		if(StringUtils.hasText(line1)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += line1;
		}
		if(StringUtils.hasText(line2)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += line2;
		}
		if(StringUtils.hasText(line3)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += line3;
		}
		if(StringUtils.hasText(line4)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += line4;
		}
		if(StringUtils.hasText(line5)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += line4;
		}
		if(StringUtils.hasText(country)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += country;
		}
		if(StringUtils.hasText(zipCode)) {
			if(value.length() > 1) {
				value += ",";
			}
			value += zipCode;
		}
		
		value += ")";
		
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((line1 == null) ? 0 : line1.hashCode());
		result = prime * result + ((line2 == null) ? 0 : line2.hashCode());
		result = prime * result + ((line3 == null) ? 0 : line3.hashCode());
		result = prime * result + ((line4 == null) ? 0 : line4.hashCode());
		result = prime * result + ((line5 == null) ? 0 : line5.hashCode());
		result = prime * result + ((usage == null) ? 0 : usage.hashCode());
		result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (line1 == null) {
			if (other.line1 != null)
				return false;
		} else if (!line1.equals(other.line1))
			return false;
		if (line2 == null) {
			if (other.line2 != null)
				return false;
		} else if (!line2.equals(other.line2))
			return false;
		if (line3 == null) {
			if (other.line3 != null)
				return false;
		} else if (!line3.equals(other.line3))
			return false;
		if (line4 == null) {
			if (other.line4 != null)
				return false;
		} else if (!line4.equals(other.line4))
			return false;
		if (line5 == null) {
			if (other.line5 != null)
				return false;
		} else if (!line5.equals(other.line5))
			return false;
		if (usage == null) {
			if (other.usage != null)
				return false;
		} else if (!usage.equals(other.usage))
			return false;
		if (zipCode == null) {
			if (other.zipCode != null)
				return false;
		} else if (!zipCode.equals(other.zipCode))
			return false;
		return true;
	}
}
