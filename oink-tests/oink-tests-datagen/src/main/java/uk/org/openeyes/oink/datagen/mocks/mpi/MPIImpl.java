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

package uk.org.openeyes.oink.datagen.mocks.mpi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPIImpl {
	
	private static Logger logger = LoggerFactory.getLogger(MPIImpl.class);
	
	private MPIRepo repo;
	
	public void start() {
		
		logger.info("Starting...");
		
		try {
			initData();
		} catch (Exception e) {
			logger.error("Failed to initialise. {}", e);
		}

		logger.info("Starting complete.");
	}
	
	public void stop() {
		logger.info("Stopping...");
		try {
			repo.close();
			logger.info("Stopping complete.");
		} catch (IOException e) {
			logger.info("Stopping failed: {}", e);
		}
	}

	public void clearData() {
		logger.info("Clearing data...");
		logger.info("Clearing data complete.");
	}
	
	public void initData() throws Exception {
		
		logger.info("Initialising data...");
		repo = new MPIRepo();
		repo.init();
		
		logger.info("Initialising data complete.");
	}
	
	public MPIRepo getRepo() {
		return repo;
	}
}
