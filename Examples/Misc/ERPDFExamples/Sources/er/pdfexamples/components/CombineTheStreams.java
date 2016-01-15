package er.pdfexamples.components;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXComponent;
import er.pdf.ERPDFMerge;

public class CombineTheStreams extends ERXComponent {
	public String filename = "combined_result.pdf";
	public NSMutableArray<InputStream> pdfsToCombine;
	public NSData data;




	public CombineTheStreams(WOContext context) {
		super(context);
		pdfsToCombine = new NSMutableArray<InputStream>();
	}


	

	public void combinedResponseAsPdf(WOResponse response, WOContext context) {
		
		
		
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ERPDFMerge.concatPDFs(pdfsToCombine, output, false);
		data = new NSData(output.toByteArray());
		
		
		response.setHeader("inline; filename=\"" + filename + "\"", "content-disposition");
		response.setHeader("application/pdf", "Content-Type");
		response.setHeader(String.valueOf(data.length()), "Content-Length");
		response.setContent(data);
	}
	
	/**
	 * combine the NSData elements to one pdf file
	 */
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		combinedResponseAsPdf(response, context);
	}


}
