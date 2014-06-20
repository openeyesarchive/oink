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
package uk.org.openeyes.oink.datagen.generators.address.uk;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import uk.org.openeyes.oink.datagen.domain.Address;
import uk.org.openeyes.oink.datagen.generators.address.beans.uk.Town;

/**
 * This class generates UK addresses.
 *
 */
public class AddressGenerator {
	public List<Address> generate(int quantity) {

		List<Address> addresses = new ArrayList<Address>();

		if (towns == null) {
			try {
				init();
			} catch (IOException e) {
				e.printStackTrace();
				return addresses;
			}
		}

		Random rng = new Random();

		for (int i = 0; i < quantity; i++) {
			Address a = new Address();

			Town town = towns.get(rng.nextInt(towns.size()));

			switch(rng.nextInt() % 5) {
				case 0:
					a.setLine1(String.format("Flat %d", rng.nextInt(30)));
					a.setLine2(String.format("%d %s", rng.nextInt(350),
							streetNames.get(rng.nextInt(streetNames.size()))));
					break;
				default:
					a.setLine1(String.format("%d %s", rng.nextInt(350),
						streetNames.get(rng.nextInt(streetNames.size()))));
			}
			a.setLine3(town.getCounty());
			a.setLine4(town.getTown());
			a.setCountry(town.getCountry());
			
			final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVW";
		    final int N = alphabet.length();
		    
			String postcode = String.format("%s%d %d%s%s", town.getPostcode(), rng.nextInt(20), rng.nextInt(10), alphabet.charAt(rng.nextInt(N)), alphabet.charAt(rng.nextInt(N)));  
			a.setZipCode(postcode);

			addresses.add(a);
		}

		return addresses;
	}

	private List<Town> towns = null;

	public void init() throws IOException {
		ICsvBeanReader beanReader = null;
		InputStream is = null;
		InputStreamReader isr = null;
		try {
			is = this.getClass().getClassLoader()
					.getResourceAsStream("generator/uk/uk_towns_list.csv");
			isr = new InputStreamReader(is);
			beanReader = new CsvBeanReader(isr,
					CsvPreference.STANDARD_PREFERENCE);

			// the header elements are used to map the values to the bean (names
			// must match)
			final String[] header = beanReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();

			towns = new ArrayList<Town>();
			Town town;
			while ((town = beanReader.read(Town.class, header, processors)) != null) {
				towns.add(town);
			}

		} finally {
			if (beanReader != null) {
				beanReader.close();
			}
			if (isr != null) {
				isr.close();
			}
		}

		streetNames = IOUtils.readLines(getClass().getResourceAsStream(
				"/generator/uk/uk_street_names.txt"));
	}

	private List<String> streetNames = null;

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] { new NotNull(), new NotNull(),
				new NotNull(), new NotNull() };

		return processors;
	}
}
