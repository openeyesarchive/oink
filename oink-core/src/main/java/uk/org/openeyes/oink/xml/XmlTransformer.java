package uk.org.openeyes.oink.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;

import uk.org.openeyes.oink.exception.OinkException;

public class XmlTransformer {
	
	TransformerFactory factory;
	
	public XmlTransformer() {
		factory = TransformerFactory.newInstance();
	}

	public String transform(String inputXml, InputStream xslIs) throws TransformerFactoryConfigurationError, TransformerException, UnsupportedEncodingException, OinkException {

		Source xslSource = new StreamSource(xslIs);

		InputStream inputXmlIs = new ByteArrayInputStream(inputXml.getBytes());
		Source inputXmlSource = new StreamSource(inputXmlIs);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Result outputXmlResult = new StreamResult(os);

		Transformer transformer = factory.newTransformer(xslSource);
		
		if (transformer == null) {
			throw new OinkException("Failed to transform XML using XSL");
		}
		
		transformer.transform(inputXmlSource, outputXmlResult);
		return os.toString("UTF-8");
	}

}
