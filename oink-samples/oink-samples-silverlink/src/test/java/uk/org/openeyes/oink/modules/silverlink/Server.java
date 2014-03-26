package uk.org.openeyes.oink.modules.silverlink;
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


import org.junit.After;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Server application than can be run as an app or unit test.
 * 
 * @author Oliver Wilkie
 */
public class Server {

	private ClassPathXmlApplicationContext context;

	public static void main(String[] args) {
		new Server().run();
	}

	@After
	public void close() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void run() {
		context = new ClassPathXmlApplicationContext("root-context.xml");
	}

}
