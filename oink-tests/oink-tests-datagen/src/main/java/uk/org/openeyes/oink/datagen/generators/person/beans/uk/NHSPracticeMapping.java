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
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

/**
 * Mapping between GP and GP practices, see:
 * http://systems.hscic.gov.uk/data/ods/supportinginfo/filedescriptions#_Toc350757592
 *
 */
public class NHSPracticeMapping {
	
	public NHSPracticeMapping() {
		
	}
	
	public NHSPracticeMapping(String csvLine) {
		String[] columns = csvLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
		for(int i = 0; i < columns.length; i++) {
			if(StringUtils.hasText(columns[i])) {
				columns[i] = columns[i].replaceAll("\"", ""); 
			} else {
				columns[i] = null;
			}
		}
		practitionerCode = columns[0];
		parentOrganisationCode = columns[1];
		parentOrganisationType = columns[2];
		joinParentDate = NHSDateFormatter.parse(columns[3]);
		leftParentDate = NHSDateFormatter.parse(columns[4]);
		amendedRecordIndicator = columns[5];
	}	
	
	public static List<NHSPracticeMapping> convert(List<String> rows) {
		
		List<NHSPracticeMapping> items = new ArrayList<NHSPracticeMapping>();
		
		for(String row : rows) {
			try {
			items.add(new NHSPracticeMapping(row));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		return items;
	}	
	
	private String practitionerCode;
	private String parentOrganisationCode;
	private String parentOrganisationType;
	private DateTime joinParentDate;
	private DateTime leftParentDate;
	private String amendedRecordIndicator;

	public String getPractitionerCode() {
		return practitionerCode;
	}

	public void setPractitionerCode(String practitionerCode) {
		this.practitionerCode = practitionerCode;
	}

	public String getParentOrganisationCode() {
		return parentOrganisationCode;
	}

	public void setParentOrganisationCode(String parentOrganisationCode) {
		this.parentOrganisationCode = parentOrganisationCode;
	}

	public String getParentOrganisationType() {
		return parentOrganisationType;
	}

	public void setParentOrganisationType(String parentOrganisationType) {
		this.parentOrganisationType = parentOrganisationType;
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

	public String getAmendedRecordIndicator() {
		return amendedRecordIndicator;
	}

	public void setAmendedRecordIndicator(String amendedRecordIndicator) {
		this.amendedRecordIndicator = amendedRecordIndicator;
	}

}
