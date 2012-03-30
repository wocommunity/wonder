package er.pdf.builder;

import org.apache.fop.apps.MimeConstants;

public class FOPBuilderFactory {

	
	/**
	 * default to pdf builder. I don't know why, I just feel like it, ok? Get off my back man!
	 * 
	 * @return
	 */
	public static FOPBuilder newBuilder() {
		return new Fop2PdfImpl();
	}

	public static FOPBuilder newBuilder(String outputType) throws Exception {
		if (outputType == null || outputType == MimeConstants.MIME_PDF) {
			return new Fop2PdfImpl();
		}
		
		if ( outputType == MimeConstants.MIME_EPS) {
			return new Fop2EpsImpl();
		}
		
		return null;
	}
}
