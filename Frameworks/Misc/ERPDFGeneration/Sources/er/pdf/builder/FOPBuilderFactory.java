package er.pdf.builder;

import org.apache.fop.apps.MimeConstants;

public class FOPBuilderFactory {

	
	/**
	 * default to pdf builder. I don't know why, I just feel like it, ok? Get off my back man!
	 * 
	 * @return a new instance of FOPBuilder
	 */
	public static FOPBuilder newBuilder() {
		return new Fop2PdfImpl();
	}

	public static FOPBuilder newBuilder(String outputType) throws Exception {
		if (MimeConstants.MIME_PDF.equals(outputType)) {
			return new Fop2PdfImpl();
		}
		
		if (MimeConstants.MIME_EPS.equals(outputType)) {
			return new Fop2EpsImpl();
		}
		
		return null;
	}
}
