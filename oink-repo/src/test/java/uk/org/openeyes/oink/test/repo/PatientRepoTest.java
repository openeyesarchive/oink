/*******************************************************************************
 * OpenEyes Interop Toolkit
 * Copyright (C) 2013  OpenEyes Foundation (http://www.openeyes.org.uk)
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
 ******************************************************************************/
package uk.org.openeyes.oink.test.repo;

import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import uk.org.openeyes.oink.entity.springdata.PatientEntity;
import uk.org.openeyes.oink.entity.springdata.PatientIdentiferEntity;
import uk.org.openeyes.oink.repo.PatientRepo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/oink-context-test-repo.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
@Transactional
public class PatientRepoTest {
	
	private static Logger LOGGER = LoggerFactory.getLogger(PatientRepoTest.class);

	@Inject
	private PatientRepo repo;

	@Inject
	private TestData data;

	private boolean init = false;

	@Before
	@Rollback(false)
	public void setup() throws Exception {

		if (!init) {
			repo.deleteAll();
			for (int i = 0; i < data.getPatients().size(); i++) {
				repo.save(data.getPatients().get(i));
			}
			init = true;
		}
	}

	@After
	public void teardown() throws Exception {
	}

	@Test
	public void testGetAll() {
		List<PatientEntity> patients = repo.findAll();

		Assert.assertEquals(3, patients.size());
		Assert.assertTrue(patients.get(0).getFamilyName()
				.equalsIgnoreCase("bloggs"));
		Assert.assertTrue(patients.get(1).getFamilyName()
				.equalsIgnoreCase("doe"));
	}
	
	@Test
	public void findById() {
		List<PatientEntity> patients = repo.findByIdentifer("11111-ABCDEF");
		
		printPatients(patients);
		
		Assert.assertEquals(1, patients.size());
	}

	@Test
	public void findByIdWithLike() {
		List<PatientEntity> patients = repo.findByIdentifer("11111%");
		
		printPatients(patients);
		
		Assert.assertEquals(2, patients.size());
	}

	private void printPatients(List<PatientEntity> patients) {
		for(PatientEntity p : patients) {
			LOGGER.info(">>>----------------------------------------------------------------------------");
			LOGGER.info("'{}','{}'", p.getFamilyName(), p.getGivenNames());
			for(PatientIdentiferEntity i : p.getIdentifiers()) {
				LOGGER.info("  '{}'", i.getIdentifier());
			}
			LOGGER.info("----------------------------------------------------------------------------<<<");
		}
	}
}
