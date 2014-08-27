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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

/**
 * NHS Data Dictionary representation of a GP Practice, see:
 * http://systems.hscic.gov.uk/data/ods/supportinginfo/filedescriptions#_Toc350757591
 *
 */
public class NHSPractice {

	public NHSPractice() {

	}

	public NHSPractice(String csvLine) {

		String[] columns = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for(int i = 0; i < columns.length; i++) {
			if(StringUtils.hasText(columns[i])) {
				columns[i] = columns[i].replaceAll("\"", ""); 
			} else {
				columns[i] = null;
			}
		}
		organisationCode = columns[0];
		name = columns[1];
		nationalGrouping = columns[2];
		highLevelHealthAuthority = columns[3];
		addressLine1 = columns[4];
		addressLine2 = columns[5];
		addressLine3 = columns[6];
		addressLine4 = columns[7];
		addressLine5 = columns[8];
		postcode = columns[9];
		openDate = NHSDateFormatter.parse(columns[10]);
		closeDate = NHSDateFormatter.parse(columns[11]);
		statusCode = columns[12];
		organisationSubTypeCode = columns[13];
		parentOrganisationCode = columns[14];
		joinParentDate = NHSDateFormatter.parse(columns[15]);
		leftParentDate = NHSDateFormatter.parse(columns[16]);
		contactTelephoneNumber = columns[17];
		amendedRecordIndicator = columns[21];
	}

	public static Map<String, NHSPractice> convert(List<String> rows) {

		Map<String, NHSPractice> items = new HashMap<String, NHSPractice>();

		for (String row : rows) {
			try {
				NHSPractice item = new NHSPractice(row);
				items.put(item.getOrganisationCode(), item);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return items;
	}

	private String organisationCode;
	private String name;
	private String nationalGrouping;
	private String highLevelHealthAuthority;
	private String addressLine1;
	private String addressLine2;
	private String addressLine3;
	private String addressLine4;
	private String addressLine5;
	private String postcode;
	private DateTime openDate;
	private DateTime closeDate;
	private String statusCode;
	private String organisationSubTypeCode;
	private String parentOrganisationCode;
	private DateTime joinParentDate;
	private DateTime leftParentDate;
	private String contactTelephoneNumber;
	private String amendedRecordIndicator;

	private List<NHSPractitioner> practioners = new ArrayList<NHSPractitioner>();

	public String getOrganisationCode() {
		return organisationCode;
	}

	public void setOrganisationCode(String organisationCode) {
		this.organisationCode = organisationCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNationalGrouping() {
		return nationalGrouping;
	}

	public void setNationalGrouping(String nationalGrouping) {
		this.nationalGrouping = nationalGrouping;
	}

	public String getHighLevelHealthAuthority() {
		return highLevelHealthAuthority;
	}

	public void setHighLevelHealthAuthority(String highLevelHealthAuthority) {
		this.highLevelHealthAuthority = highLevelHealthAuthority;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getAddressLine3() {
		return addressLine3;
	}

	public void setAddressLine3(String addressLine3) {
		this.addressLine3 = addressLine3;
	}

	public String getAddressLine4() {
		return addressLine4;
	}

	public void setAddressLine4(String addressLine4) {
		this.addressLine4 = addressLine4;
	}

	public String getAddressLine5() {
		return addressLine5;
	}

	public void setAddressLine5(String addressLine5) {
		this.addressLine5 = addressLine5;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public DateTime getOpenDate() {
		return openDate;
	}

	public void setOpenDate(DateTime openDate) {
		this.openDate = openDate;
	}

	public DateTime getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(DateTime closeDate) {
		this.closeDate = closeDate;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getOrganisationSubTypeCode() {
		return organisationSubTypeCode;
	}

	public void setOrganisationSubTypeCode(String organisationSubTypeCode) {
		this.organisationSubTypeCode = organisationSubTypeCode;
	}

	public String getParentOrganisationCode() {
		return parentOrganisationCode;
	}

	public void setParentOrganisationCode(String parentOrganisationCode) {
		this.parentOrganisationCode = parentOrganisationCode;
	}

	public DateTime getJoinParentDate() {
		return joinParentDate;
	}

	public void setJoinParentDate(DateTime joinParentDate) {
		this.joinParentDate = joinParentDate;
	}

	public DateTime getLeftParentDate() {
		return leftParentDate;
	}

	public void setLeftParentDate(DateTime leftParentDate) {
		this.leftParentDate = leftParentDate;
	}

	public String getContactTelephoneNumber() {
		return contactTelephoneNumber;
	}

	public void setContactTelephoneNumber(String contactTelephoneNumber) {
		this.contactTelephoneNumber = contactTelephoneNumber;
	}

	public String getAmendedRecordIndicator() {
		return amendedRecordIndicator;
	}

	public void setAmendedRecordIndicator(String amendedRecordIndicator) {
		this.amendedRecordIndicator = amendedRecordIndicator;
	}

	public List<NHSPractitioner> getPractioners() {
		return practioners;
	}

	public void setPractioners(List<NHSPractitioner> practioners) {
		this.practioners = practioners;
	}
}
