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

package uk.org.openeyes.oink.datagen.mocks.mpi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;
import uk.org.openeyes.oink.datagen.generators.person.PersonGeneratorFactory;
import uk.org.openeyes.oink.datagen.mocks.mpi.MPIImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

public class TestMPI {

	@Test
	public void testOne() throws Exception {
		
		MPIImpl mpi = new MPIImpl();
		
		mpi.start();
		mpi.getRepo().deleteAll();
		
		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");
		Person patient = g.generate(1).get(0);
		mpi.getRepo().addPatient(patient);

		ObjectMapper m = new ObjectMapper();
		m.registerModule(new JodaModule());
		String json = m.writeValueAsString(patient);
		
		for(Identifier i : patient.getIdentifiers()) {
		
			Person patient2 = mpi.getRepo().getPatientByIdentifier(i);
			assertNotNull(patient2);
			
			String json2 = m.writeValueAsString(patient2);
			assertEquals(json, json2);
		}
		
		mpi.stop();
	}
	
	@Test
	public void testSize() throws Exception {
		
		MPIImpl mpi = new MPIImpl();
		
		mpi.start();
		mpi.getRepo().deleteAll();

		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");
		List<Person> patients = g.generate(100);
		mpi.getRepo().addPatients(patients);

		int size = mpi.getRepo().getSize();
		assertEquals(100, size);
		
		mpi.stop();
	}
	
	@Test
	public void testDeleteAll() throws Exception {
		
		MPIImpl mpi = new MPIImpl();
		
		int size = -1;
		int targetSize = 1234;
		
		mpi.start();
		mpi.getRepo().deleteAll();

		size = mpi.getRepo().getSize();
		assertEquals(0, size);

		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");
		List<Person> patients = g.generate(targetSize);
		mpi.getRepo().addPatients(patients);

		size = mpi.getRepo().getSize();
		assertEquals(targetSize, size);

		mpi.getRepo().deleteAll();
		
		size = mpi.getRepo().getSize();
		assertEquals(0, size);
		
		mpi.stop();
	}
}
