package er.pdfexamples.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class SimpleHTML2FOP2PDF extends ERXComponent {
    public SimpleHTML2FOP2PDF(WOContext context) {
        super(context);
    }
    
    
	/**
	 * get the location (on the classpath) of the xsl to fo transform file
	 * @return
	 * @throws Exception
	 */
	public String getXsl() throws Exception {
		String k = "xsl.xml2fop." + this.getClass().getName();
		if (System.getProperties().containsKey(k)) {
			return System.getProperty(k);
		}
		// return "er/pdfexamples/xsl/xhtml-to-xslfo.xsl";
		throw new Exception("XSL resource not found. Looking for property \"" + k + "\" as address of resource in classpath");
	}
}