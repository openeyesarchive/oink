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

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import java.io.File;
import javax.inject.Inject;

import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Oliver Wilkie
 */
@RunWith(PaxExam.class)
public class OinkHl7v2IT extends OinkKarafITSupport{

	private static final Logger logger = LoggerFactory
			.getLogger(OinkHl7v2IT.class);

	@Inject
	protected BundleContext bundleContext;
	
	@Test
	public void checkFacadeHasASingleConfigPidAssociatedInTheFeaturesRepo() throws Exception {
		checkAdapterHasASingleConfigPidAssociatedInTheFeaturesRepo("hl7v2");
	}
	
	@Test
	public void checkFacadeContextFailsWithoutCfg() throws Exception {
		checkAdapterContextFailsWithoutCfg("hl7v2");
	}
	
	@Test
	public void checkFacadeContextDoesntFailWithCfg() throws Exception {
		checkAdapterContextDoesntFailWithCfg("hl7v2");
	}	
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
	    probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*,org.springframework.osgi.context.event.*;status=provisional");
	    return probe;
	}

	@Configuration
	public Option[] config() {
		MavenArtifactUrlReference karafUrl = maven()
				.groupId("uk.org.openeyes.oink.karaf").artifactId("distro")
				.version(asInProject()).type("tar.gz");

		return new Option[] {
				// Provision and launch a container based on a distribution of
				// Karaf (Apache ServiceMix).
				karafDistributionConfiguration().frameworkUrl(karafUrl)
						.unpackDirectory(new File("target/pax"))
						.useDeployFolder(false),
				// It is really nice if the container sticks around after the
				// test so you can check the contents
				// of the data directory when things go wrong.
				keepRuntimeFolder(),
				// Don't bother with local console output as it just ends up
				// cluttering the logs
				configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				logLevel(LogLevel.INFO),
				features("mvn:org.apache.karaf.features/spring/3.0.1/xml/features", "spring-dm"),
				// Provision the example feature exercised by this test
				//features(oinkFeaturesRepo, "oink-example-facade"),
				//replaceConfigurationFile("etc/uk.org.openeyes.oink.facade.cfg", new File("src/test/resources/facade.properties")),
		// Remember that the test executes in another process. If you want to
		// debug it, you need
		// to tell Pax Exam to launch that process with debugging enabled.
		// Launching the test class itself with
		// debugging enabled (for example in Eclipse) will not get you the
		// desired results.
		//debugConfiguration("5005", true),
		};
	}
	
}
