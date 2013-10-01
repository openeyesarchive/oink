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
package uk.org.openeyes.infrastructure.commands.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uk.org.openeyes.infrastructure.annotations.Command;

/**
 * Manages command execution history based on {@link Command} annotation attributes<br>
 * Commands that are annotated as unique=true are stored in this history.<br>
 * History checks if the same command (equals) is called again.<br>
 * <br>
 * Each command class has it's own entries in history - history length can be parameterized via constructor parameter. 
 * 
 * @author Slawek
 * 
 */
public class GateHistory {

	@SuppressWarnings("serial")
	private class CommandExecutionsMap extends LinkedHashMap<Object, Date> {
		protected boolean removeEldestEntry(Map.Entry<Object, Date> eldest) {
			return this.size() > maxHistoryCapacity;
		}
	};

	private static final int DEFAULT_MAX_HISTORY_CAPACITY = 3;

	/**
	 * History model. Each command class has map of executions (command instance
	 * and time)
	 */
	@SuppressWarnings("rawtypes")
	private Map<Class, CommandExecutionsMap> history = new ConcurrentHashMap<Class, CommandExecutionsMap>();

	private int maxHistoryCapacity;

	public GateHistory(int maxHistoryCapacity) {
		this.maxHistoryCapacity = maxHistoryCapacity;
	}

	public GateHistory() {
		this(DEFAULT_MAX_HISTORY_CAPACITY);
	}

	/**
	 * 
	 * @param command
	 * @return true if command is not a repetition, false if command is
	 *         repetition and should not be executed now
	 */
	public boolean register(Object command) {
		if (!isUnique(command))
			return true;

		Date lastRun = getFromHistory(command);

		// update history
		Date now = new Date();
		addToHistory(command, now);

		// first run, so go
		if (lastRun == null)
			return true;

		long uniqueStorageTimeout = getUniqueStorageTimeout(command);
		// no timeout so by default it is duplicated
		if (uniqueStorageTimeout == 0)
			return false;

		long milisFromLastRun = now.getTime() - lastRun.getTime();
		return milisFromLastRun > uniqueStorageTimeout;
	}

	private boolean isUnique(Object command) {
		if (!command.getClass().isAnnotationPresent(Command.class))
			return false;

		Command commandAnnotation = command.getClass().getAnnotation(
				Command.class);

		return commandAnnotation.unique();
	}

	private Long getUniqueStorageTimeout(Object command) {
		Command commandAnnotation = command.getClass().getAnnotation(
				Command.class);
		return commandAnnotation.uniqueStorageTimeout();
	}

	private Date getFromHistory(Object command) {
		Map<Object, Date> executions = history.get(command.getClass());
		if (executions == null)
			return null;
		return executions.get(command);
	}

	private void addToHistory(Object command, Date executeDate) {
		CommandExecutionsMap executions = history.get(command.getClass());
		if (executions == null) {
			executions = new CommandExecutionsMap();
			history.put(command.getClass(), executions);
		}
		executions.put(command, executeDate);
	}
}
