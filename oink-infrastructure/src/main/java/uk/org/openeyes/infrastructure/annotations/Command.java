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
package uk.org.openeyes.infrastructure.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command {
	/**
	 * Hint to the command executor that this command should run asynchronously.
	 * <br>
	 * If true then {@link CommandHandler} must return void - otherwise the command executor will throw an exception.
	 * @return
	 */
    boolean asynchronous() default false;
    
    /**
     * Hint that this command should only be sent once.<br>
     * If true than command class must implement equals and hashCode.
     * @return
     */
    boolean unique() default false;

    /**
     * If unique is true than this property may specify maximum timeout in milliseconds before same command can be executed
     * @return
     */
    long uniqueStorageTimeout() default 0L;
}
