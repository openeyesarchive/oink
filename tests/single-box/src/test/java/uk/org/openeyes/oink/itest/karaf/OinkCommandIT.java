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
package uk.org.openeyes.oink.itest.karaf;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import javax.inject.Inject;

import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OinkCommandIT extends OinkKarafITSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(OinkCommandIT.class);

	@Inject
	protected BundleContext bundleContext;

	@Inject
	private ConfigurationAdmin configurationAdmin;
	
	@Inject
	private FeaturesService featuresService;
	
	@Inject
	private CommandProcessor commandProcessor;
	
	@Test
	public void testOinkCommandsAreAvailable() throws Exception {
		Feature f = featuresService.getFeature("oink-commands");
		assertNotNull(f);
		assertTrue(featuresService.isInstalled(f));
		
	}
	
	@Test
	public void testOinkEnableCommandIsAvailableOnShellOnAFreshCopyOfCustomDistro() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream psout = new PrintStream(out);		
		PrintStream pser = new PrintStream(err);
		CommandSession cs = commandProcessor.createSession(System.in, psout, pser);
		cs.execute("help oink:enable");
		cs.close();
		psout.close();
		pser.close();
		assertTrue(err.toString().isEmpty());
		assertFalse(out.toString().contains("COMMANDS"));		
	}	
	
	@Test
	public void testOinkDisableCommandIsAvailableOnShellOnAFreshCopyOfCustomDistro() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream psout = new PrintStream(out);		
		PrintStream pser = new PrintStream(err);
		CommandSession cs = commandProcessor.createSession(System.in, psout, pser);
		cs.execute("help oink:disable");
		cs.close();
		psout.close();
		pser.close();
		assertTrue(err.toString().isEmpty());
		assertFalse(out.toString().contains("COMMANDS"));		
	}	
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
	    probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional,org.springframework.osgi.context.event.*;status=provisional");
	    return probe;
	}

	@Configuration
	public Option[] config() {
		return standardConfig();
	}
	
}
