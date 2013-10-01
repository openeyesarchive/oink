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
package uk.org.openeyes.infrastructure.commands.handler;

/**
 * @param <C> The command type.
 * @param <R> Result type - for asynchronous {@link Command}commands (asynchronous=true) should be {@link Void}.
 */
public interface CommandHandler<C, R> {

    public R handle(C command);
}
