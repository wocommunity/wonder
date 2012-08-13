package er.pdf.builder;

import java.io.OutputStream;

import org.apache.fop.apps.MimeConstants;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * right now this is theoretical, but you should be able to add multiple output
 * formats through the FOPBuilderFactory
 * 
 * @author lmg42
 * 
 */
public class Fop2EpsImpl implements FOPBuilder {

	private String _outputType = MimeConstants.MIME_EPS;

	public Fop2EpsImpl() throws Exception {
		throw new Exception("Not implemented yet");
	}

	public void setXSL(String fopxsl) {
		// TODO Auto-generated method stub

	}

	public void setXML(String xmlToTransform) {
		// TODO Auto-generated method stub

	}

	public void createDocument(OutputStream os) {
		// TODO Auto-generated method stub

	}

	public void setConfiguration(NSMutableDictionary<String, Object> config) {
		// TODO Auto-generated method stub

	}

	public void createDocument(OutputStream os, NSDictionary<String, Object> agentAttributes) throws Throwable {
		// TODO Auto-generated method stub
		
	}

}
