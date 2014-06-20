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
package uk.org.openeyes.oink.datagen.domain.identifier;

/**
 * This class represents a UK NHS number.
 *
 */
public class NHSNumber {
	
	public static final String URI = "urn:nhs-uk:identity:nhsno";
	
	private int checkNumber;
    private String value;
    private int[] multiplers;
    private Boolean isValid = null;

    public NHSNumber() {

    	// Create the multipler array
        this.multiplers = new int[9];

        this.multiplers[0] = 10;
        this.multiplers[1] = 9;
        this.multiplers[2] = 8;
        this.multiplers[3] = 7;
        this.multiplers[4] = 6;
        this.multiplers[5] = 5;
        this.multiplers[6] = 4;
        this.multiplers[7] = 3;
        this.multiplers[8] = 2;	
    }
    
    public NHSNumber(String value) {
    	this();
    	setValue(value);
    }
    
    private void validate() {
    	
    	this.checkNumber = -1;
    	this.isValid = false;
    	
    	if(this.value == null) {
    		return;
    	}
    	
    	if(this.value.length() < 9) {
    		return;
    	}
    	
    	String checkDigit = this.value.substring(this.value.length() - 1, this.value.length());
        this.checkNumber = Integer.parseInt(checkDigit);
        
        int remainder = 0;
        /// The total to be checked against the check number
        int total = 0;

        // Loop over each number in the string and calculate the current sum
        int currentSum = 0;
        for(int i = 0; i <= 8; i++)
        {
            String currentString = this.value.substring(i, i + 1);
            
            int currentNumber = Integer.parseInt(currentString);
            int currentMultipler = this.multiplers[i];
            currentSum = currentSum + (currentNumber * currentMultipler);
        }

        /// Calculate the remainder and get the total
        remainder = currentSum % 11;
        total = 11 - remainder;

        /// Now we have our total we can validate it against the check number
        if (total == 11)
        {
            total = 0;
        }

        if (total == 10)
        {
            this.isValid = false;
        } else {
        	this.isValid = (total == this.checkNumber);
        }
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
    	// trim and remove all non-digit chars
    	this.value = value.trim().replaceAll("[\\D]", "");
		validate();
	}

    public Boolean isValid() {
    	if(this.isValid == null) {
    		validate();
    	}
    	return this.isValid;
    }
    
    @Override
    public String toString() {
    	if(isValid()) {
    		return this.value.substring(0, 3) + "-" + this.value.substring(3, 6) + "-" + this.value.substring(6, 10);
    	}
    	
    	return "Invalid value '" + this.value + "'";
    }
}
