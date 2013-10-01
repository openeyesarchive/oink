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
package uk.org.openeyes.oink.infrastructure.commands.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.infrastructure.annotations.Command;
import uk.org.openeyes.oink.infrastructure.commands.Gate;

@Component
public class StandardGate implements Gate {
	
	@Inject
	private RunEnvironment runEnvironment;
	
	private GateHistory gateHistory = new GateHistory();

	@Override
	public Object dispatch(Object command){
		if (! gateHistory.register(command)){
			//TODO - log.info(duplicate)
			return null; // skip duplicate
		}
			
		if (isAsynchronous(command)){
			// TODO - add to the queue. Queue should send this command to the RunEnvironment.
			return null;
		}
		
		return runEnvironment.run(command);
	}

	/**
	 * @param command
	 * @return
	 */
	private boolean isAsynchronous(Object command) {
		if (!command.getClass().isAnnotationPresent(Command.class))
			return false;
		
		Command commandAnnotation = command.getClass().getAnnotation(Command.class);		
		return commandAnnotation.asynchronous();		
	}
}
