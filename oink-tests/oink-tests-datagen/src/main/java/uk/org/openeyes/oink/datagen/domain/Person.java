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

import java.util.List;

import org.joda.time.DateTime;

/**
 * This class represents a person.
 *
 */
public class Person {

	public List<Identifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<Identifier> identifiers) {
		this.identifiers = identifiers;
	}

	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	public List<Telecom> getTelecoms() {
		return telecoms;
	}

	public void setTelecoms(List<Telecom> telecoms) {
		this.telecoms = telecoms;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public DateTime getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(DateTime dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public DateTime getDateOfDeath() {
		return dateOfDeath;
	}

	public void setDateOfDeath(DateTime dateOfDeath) {
		this.dateOfDeath = dateOfDeath;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Organisation getManagingOrganisation() {
		return managingOrganisation;
	}

	public void setManagingOrganisation(Organisation managingOrganisation) {
		this.managingOrganisation = managingOrganisation;
	}

	public Practitioner getManagingPractioner() {
		return managingPractioner;
	}

	public void setManagingPractioner(Practitioner managingPractioner) {
		this.managingPractioner = managingPractioner;
	}

	private List<Identifier> identifiers;
	private List<Address> addresses;
	private List<Telecom> telecoms;

	private String prefix;
	private String suffix;
	private String firstName;
	private String lastName;
	private String gender;

	private DateTime dateOfBirth;
	private DateTime dateOfDeath;

	private Boolean active;

	private Organisation managingOrganisation;
	private Practitioner managingPractioner;

	@Override
	public String toString() {
		return String.format("%s, %s %s, %s, %s, %s, %s, %s, %s, %s, ManagingPractioner = %s, ManagingOrg = %s", 
				prefix, firstName, lastName, suffix,
				gender,
				dateOfBirth.toString("dd MMM YYYY"),
				dateOfDeath != null ? dateOfDeath.toString("dd MMM YYYY") : "alive",
				identifiers.toString(),
				telecoms.toString(),
				addresses.toString(),
				managingPractioner != null ? managingPractioner.toString() : "none",
				managingOrganisation != null ? managingOrganisation.toString() : "none"
				);
	}

	public Identifier getIdentifiersByUsage(String usage) {
		for(Identifier i : identifiers) {
			if(i.getUsage().equalsIgnoreCase(usage)) {
				return i;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result
				+ ((addresses == null) ? 0 : addresses.hashCode());
		result = prime * result
				+ ((dateOfBirth == null) ? 0 : dateOfBirth.hashCode());
		result = prime * result
				+ ((dateOfDeath == null) ? 0 : dateOfDeath.hashCode());
		result = prime * result
				+ ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result
				+ ((identifiers == null) ? 0 : identifiers.hashCode());
		result = prime * result
				+ ((lastName == null) ? 0 : lastName.hashCode());
		result = prime
				* result
				+ ((managingOrganisation == null) ? 0 : managingOrganisation
						.hashCode());
		result = prime
				* result
				+ ((managingPractioner == null) ? 0 : managingPractioner
						.hashCode());
		result = prime * result
				+ ((telecoms == null) ? 0 : telecoms.hashCode());
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
		Person other = (Person) obj;
		if (active == null) {
			if (other.active != null)
				return false;
		} else if (!active.equals(other.active))
			return false;
		if (addresses == null) {
			if (other.addresses != null)
				return false;
		} else if (!addresses.equals(other.addresses))
			return false;
		if (dateOfBirth == null) {
			if (other.dateOfBirth != null)
				return false;
		} else if (!dateOfBirth.equals(other.dateOfBirth))
			return false;
		if (dateOfDeath == null) {
			if (other.dateOfDeath != null)
				return false;
		} else if (!dateOfDeath.equals(other.dateOfDeath))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (gender == null) {
			if (other.gender != null)
				return false;
		} else if (!gender.equals(other.gender))
			return false;
		if (identifiers == null) {
			if (other.identifiers != null)
				return false;
		} else if (!identifiers.equals(other.identifiers))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (managingOrganisation == null) {
			if (other.managingOrganisation != null)
				return false;
		} else if (!managingOrganisation.equals(other.managingOrganisation))
			return false;
		if (managingPractioner == null) {
			if (other.managingPractioner != null)
				return false;
		} else if (!managingPractioner.equals(other.managingPractioner))
			return false;
		if (telecoms == null) {
			if (other.telecoms != null)
				return false;
		} else if (!telecoms.equals(other.telecoms))
			return false;
		return true;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}
