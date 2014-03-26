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
package uk.org.openeyes.oink.modules.silverlink;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.ErrorHandler;

public class LoggingErrorHandler implements ErrorHandler {
		
	private static Logger log = Logger.getLogger(LoggingErrorHandler.class.getName());

	@Override
	public void handleError(Throwable arg0) {
		log.log(Level.SEVERE, "Could not handle incoming message");
	}

}
