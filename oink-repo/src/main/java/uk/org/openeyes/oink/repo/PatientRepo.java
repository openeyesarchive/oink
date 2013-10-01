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
package uk.org.openeyes.oink.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import uk.org.openeyes.oink.entity.springdata.PatientEntity;

public interface PatientRepo extends JpaRepository<PatientEntity, Long> {

	@Query("select p from Patient p LEFT JOIN p.identifiers i WHERE i.identifier like ?")
	List<PatientEntity> findByIdentifer(String identifier);
}
