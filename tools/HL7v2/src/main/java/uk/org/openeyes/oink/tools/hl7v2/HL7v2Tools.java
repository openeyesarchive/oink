package uk.org.openeyes.oink.tools.hl7v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.datatype.CX;
import ca.uhn.hl7v2.model.v24.datatype.XPN;
import ca.uhn.hl7v2.model.v24.group.ADT_A39_PATIENT;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.PID;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.google.common.hash.Hashing;

public class HL7v2Tools {

	private static Logger logger = LoggerFactory.getLogger(HL7v2Tools.class);

	@SuppressWarnings("static-access")
	public static void main(String[] args) {

		CommandLineParser parser = new GnuParser();

		// create the Options
		Options options = new Options();

		options.addOption(OptionBuilder.hasArg().withArgName("input")
				.withType(String.class).withDescription("Input folder")
				.create("input"));
		options.addOption(OptionBuilder.hasArg().withArgName("output")
				.withType(String.class).withDescription("Output folder")
				.create("output"));
		options.addOption(OptionBuilder.hasArg().withArgName("maxfiles")
				.withType(Number.class)
				.withDescription("Maximum files to process").create("maxfiles"));
		options.addOption(OptionBuilder.hasArg().withArgName("type")
				.withType(String.class)
				.withDescription("HL7 message type filter").create("type"));
		options.addOption(OptionBuilder.hasArg().withArgName("trigger")
				.withType(String.class)
				.withDescription("HL7 message trigger type filter")
				.create("trigger"));
		options.addOption(OptionBuilder.hasArg().withArgName("outputfolders")
				.withType(String.class)
				.withDescription("Output into folders per message type")
				.create("outputfolders"));

		// Path to data
		String dir = null;
		String dirOut = null;
		String encoding = "ASCII";
		int fileMax = -1;
		String hl7Trigger = null;
		String hl7Type = null;
		boolean outputFolder = false;

		try {
			// parse the command line arguments
			CommandLine cmdLine = parser.parse(options, args);

			if (cmdLine.hasOption("input")) {
				dir = (String) cmdLine.getParsedOptionValue("input");
			}
			if (cmdLine.hasOption("output")) {
				dirOut = (String) cmdLine.getParsedOptionValue("output");
			}
			if (cmdLine.hasOption("outputfolders")) {
				outputFolder = Boolean.parseBoolean((String) cmdLine
						.getParsedOptionValue("outputfolders"));
			}
			if (cmdLine.hasOption("maxfiles")) {
				fileMax = ((Number) cmdLine.getParsedOptionValue("maxfiles"))
						.intValue();
			}
			if (cmdLine.hasOption("type")) {
				hl7Type = cmdLine.getOptionValue("type");
			}
			if (cmdLine.hasOption("trigger")) {
				hl7Trigger = cmdLine.getOptionValue("trigger");
			}

			if (!StringUtils.hasText(dir) || !StringUtils.hasText(dirOut)) {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("HL7v2Tools", options);
				return;
			}
		} catch (ParseException exp) {
			logger.error("Unexpected exception:" + exp.getMessage());
			return;
		}

		logger.info("----------------------------------------------------");
		logger.info(" OINK HL7v2 tools");
		logger.info("----------------------------------------------------");

		logger.info("Data folder = '{}'", dir);
		logger.info("Output Data folder = '{}'", dirOut);
		logger.info("Encoding = {}", encoding);
		logger.info("Max files = {}", fileMax);
		logger.info("HL7 Message Type Filter = '{}'", hl7Type);
		logger.info("HL7 Message Trigger Filter = '{}'", hl7Trigger);
		logger.info("Output to Folders = {}", outputFolder);

		File fileDirOut = new File(dirOut);
		fileDirOut.mkdirs();

		int errors = 0;

		OutputStream os = null;
		try {
			os = new FileOutputStream("output.csv");
		} catch (FileNotFoundException e1) {
			logger.error("Failed to open output file", e1);
		}
		PrintStream printStream = new PrintStream(os);

		@SuppressWarnings("resource")
		HapiContext context = new DefaultHapiContext();
		context.setValidationContext(new NoValidation());
		PipeParser p = context.getPipeParser();

		File dirF = new File(dir);
		int fileCount = 0;
		outerloop: for (File f : dirF.listFiles()) {
			Message message = null;
			try {
				String messageString = FileUtils.readFileToString(f, encoding);

				Pattern pattern = Pattern.compile("Z\\S+\\|");
				Matcher matcher = pattern.matcher(messageString);

				StringBuffer sb = new StringBuffer();
				int z = 0;
				while (matcher.find()) {
					z++;
					matcher.appendReplacement(sb,
							"Z" + String.format("%02d", z) + "|");
				}
				matcher.appendTail(sb);
				messageString = sb.toString();

				messageString = messageString.replace("\n", "\r");

				message = p.parse(messageString);

				//
				// Header
				//
				MSH msh = (MSH) message.get("MSH");

				String msgType = msh.getMessageType().getMessageType()
						.getValue();
				String msgTrigger = msh.getMessageType().getTriggerEvent()
						.getValue();

				DateTime msgDate = new DateTime(msh.getMsh7_DateTimeOfMessage()
						.getTimeOfAnEvent().getValueAsDate());

				if (StringUtils.hasText(hl7Type)) {
					if (!msgType.equalsIgnoreCase(hl7Type)) {
						continue;
					}
				}
				if (StringUtils.hasText(hl7Trigger)) {
					if (!msgTrigger.equalsIgnoreCase(hl7Trigger)) {
						continue;
					}
				}

				message = pseudonymise(message);

				String log = null;
				try {
					fileCount++;

					//
					// Identifiers
					//

					PID pid = null;

					try {
						pid = (PID) message.get("PID");
					} catch (HL7Exception e) {
						ADT_A39_PATIENT adtMessage = (ADT_A39_PATIENT) message
								.get("PATIENT");
						pid = (PID) adtMessage.getPID();
					}

					for (int i = 0; i < pid.getPid3_PatientIdentifierListReps(); i++) {
						CX cx = pid.getPid3_PatientIdentifierList(i);
						String value = cx.getCx1_ID().getValue();

						// SHA1 hash identifiers and replace clear text values
						value = Hashing.sha1()
								.hashString(value, Charsets.UTF_8).toString();

						cx.getCx1_ID().setValue(value);
					}

					Pattern patAnon = Pattern.compile("[^\\s\\^X]");

					for (int i = 0; i < pid.getPid5_PatientNameReps(); i++) {
						XPN xpn = pid.getPid5_PatientName(i);

						if (xpn != null) {
							Matcher m = patAnon.matcher(xpn.encode());
							if (m.find()) {
								errors++;
								logger.error(
										"Detaileds not anonymised!! '{}' - XPN '{}'",
										f.getName(), xpn.encode());
								continue outerloop;
							}
						}
					}

					for (int i = 0; i < pid.getPid4_AlternatePatientIDPIDReps(); i++) {
						CX cx = pid.getPid4_AlternatePatientIDPID(i);
						if (cx != null) {
							Matcher m = patAnon.matcher(cx.encode());
							if (m.find()) {
								errors++;
								logger.error("Detaileds not anonymised!! '{}' - CX '{}'",
										f.getName(), cx.encode());
								continue outerloop;
							}
						}
					}

					String outMessageString = message.encode();

					if (logger.isDebugEnabled()) {
						logger.debug("=================================================");
						logger.debug("{}", outMessageString.replace("\r", "\n"));
						logger.debug("=================================================");
					}

					File file = null;

					if (outputFolder) {
						File fileSubFolderOut = new File(fileDirOut.getPath()
								+ File.separator + msgType + File.separator
								+ msgTrigger);
						fileSubFolderOut.mkdirs();
						file = new File(fileSubFolderOut.getPath()
								+ File.separator + f.getName());
					} else {
						file = new File(fileDirOut.getPath() + File.separator
								+ f.getName());
					}
					FileUtils.writeStringToFile(file, outMessageString,
							encoding);

					log = String
							.format("%s, %s, %s, %s, %d", file.getPath(),
									msgType, msgTrigger,
									msgDate.toString("yyyy/MM/dd HH:mm:ss"),
									f.length());
				} catch (Exception e) {
					logger.warn("HL7 parse error: '{} - {} at {}'",
							f.getName(), e.getMessage(), e.getStackTrace());
					logger.warn(messageString.replace("\r", "\n"));
					log = String.format("%s, %s", f.getName(), "Parse Error");
				} finally {
					printStream.append(log);
					printStream.append("\n");
					logger.info("{}. Errors = {}", log, errors);
				}

			} catch (IOException e) {
				logger.warn("Unable to read file '{}'", f.getName());
				errors++;
			} catch (Exception e) {
				logger.warn("Unable to parse file '{}' - {} at {}",
						f.getName(), e.toString(),
						e.getStackTrace()[0].toString());
				errors++;
			}

			if (fileMax != -1 && fileCount >= fileMax) {
				break;
			}
		}

		logger.info("===========================================");
		logger.info("Complete. Errors = {}.", errors);

		/*
		 * try { context.close(); } catch (IOException e) {
		 * logger.error("Failed to close context", e); }
		 */

		printStream.close();
	}

	private static Message pseudonymise(Message message) throws HL7Exception {

		Message messageOut = null;

		XMLParser xmlParser = message.getParser().getHapiContext()
				.getXMLParser();
		Document doc = null;
		try {
			doc = xmlParser.encodeDocument(message);
			XPath xPath = XPathFactory.newInstance().newXPath();

			List<String> nodeNames = new ArrayList<String>();
			nodeNames.add("XPN");
			nodeNames.add("XAD");
			nodeNames.add("XTN");
			nodeNames.add("PID.4");

			List<String> excludeTags = new ArrayList<String>();
			excludeTags.add("XAD.7");
			// excludeTags.add("XPN.5"); // Title

			// logger.error(getStringFromDocument(doc));

			for (String nodeName : nodeNames) {
				NodeList nodes = (NodeList) xPath.evaluate(
						"//*[starts-with(local-name(), '" + nodeName + "')]",
						doc.getDocumentElement(), XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); ++i) {
					Element e = (Element) nodes.item(i);
					boolean skip = false;
					for (String excludeTag : excludeTags) {
						if (excludeTag.equalsIgnoreCase(e.getNodeName())) {
							skip = true;
						}
					}

					if (e.getChildNodes().item(0).getNodeName()
							.startsWith("CX")) {
						for (int j = 0; j < e.getChildNodes().getLength(); j++) {
							String beforeText = e.getChildNodes().item(j)
									.getTextContent();
							e.getChildNodes()
									.item(j)
									.setTextContent(
											beforeText.replaceAll("\\S", "X"));
							// logger.error(String.format("%s = '%s' -> '%s'", e
							// .getChildNodes().item(j), beforeText, e
							// .getChildNodes().item(j).getTextContent()));
						}
					}

					if (!skip) {
						String beforeText = e.getTextContent();
						e.setTextContent(beforeText.replaceAll("\\S", "X"));

						// logger.error(String.format("%s = '%s' -> '%s'",
						// e.getNodeName(), beforeText, e.getTextContent()));
					}
				}
			}

		} catch (HL7Exception e) {
			e.printStackTrace();
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
		}

		String xml = getStringFromDocument(doc);
		messageOut = xmlParser.parse(xml);
		String pipe = message.getParser().getHapiContext().getPipeParser()
				.encode(messageOut);
		messageOut = message.getParser().getHapiContext().getPipeParser()
				.parse(pipe);

		return messageOut;
	}

	public static String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
