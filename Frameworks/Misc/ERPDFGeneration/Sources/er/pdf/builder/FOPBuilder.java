package er.pdf.builder;

import java.io.OutputStream;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public interface FOPBuilder {

	/**
	 * set the location of the transform file. This is passed to
	 * getResourceAsStream so it's intended to be loaded from the classpath
	 * somewhere. It should work with jars or anything that can be found using
	 * getResource(asStream)
	 * 
	 * @param fopxslLocation
	 */
	public void setXSL(String fopxslLocation);

	/**
	 * set the xml string to be transformed. 
	 * @param xmlToTransform
	 */
	public void setXML(String xmlToTransform);

	
	/**
	 * create the output file pdf.
	 * @param os
	 * @throws Throwable
	 */
	public void createDocument(OutputStream os) throws Throwable;
	
	public void createDocument (OutputStream os, NSDictionary<String, Object> agentAttributes) throws Throwable;

	
	/**
	 * this is here for extension in order to pass configuration of the fop
	 * engine from the program. For now, I'm relying on environment configuration
	 * and some specific inline configuration. We'll see if this is important. 
	 * @param config
	 */
	public void setConfiguration(NSMutableDictionary<String, Object> config);

}
