package er.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Based upon http://java-x.blogspot.com/2006/11/merge-pdf-files-with-itext.html
 * ERPDFMerge contains the method concatPDFs to concatenate multiple PDF documents
 * into one.
 * 
 * @author mhast
 */
public class ERPDFMerge {
	
	/**<p>
	 * Concatenates the list of streamOfPDFFiles into one PDF document that can
	 * be accessed via outputStream. If paginate is true it adds page numbering
	 * to the bottom right of each PDF page.</p>
	 * <p>
	 * This method creates a default iText Document object and passes it into concatPDFs(List, OutputStream, Document, boolean) method
	 * </p>
	 * 
	 * @param streamOfPDFFiles
	 * @param outputStream
	 * @param paginate
	 */
	public static void concatPDFs(List<InputStream> streamOfPDFFiles, OutputStream outputStream, boolean paginate) {	
		ERPDFMerge.concatPDFs(streamOfPDFFiles, outputStream, new Document(), paginate);		
	}
	
	/**
	 * <p>
	 * Concatenates a list of streamOfPDFFiles into one PDF document that can be
	 * accessed through the outputStream.
	 * </p>
	 * <p>
	 * The document arg is intended to give you control over the page size and
	 * margins by allowing you to construct a document of your own parameters.
	 * Other than that, it behaves the same as the concatPDFs method without the
	 * document arg
	 * </p>
	 * <p>To use this version of the method, create a new Document and pass it in like this:
	 * 
	 * <pre>
	com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.LETTER, 0.0F, 0.0F,0.0F,0.0F);
	ERPDFMerge.concatPDFs(pdfsToCombine, output, doc, true);</pre>
	
	 * That creates a letter sized (8.5 in x 11 in) page with no margin (suitable for concatinating 8.5x11 pdfs that already have
	 * their margins set.
	 * 
	 * 
	 * @param streamOfPDFFiles
	 * @param outputStream
	 * @param document
	 * @param paginate
	 * 
	 * @see com.lowagie.text.Document iText Document class
	 * @see com.lowagie.text.PageSize Page Size class in iText
	 * 
	 * @author Larry Mills-Gahl &lt;lmg@webfarm.com&gt;
	 */

	public static void concatPDFs(List<InputStream> streamOfPDFFiles, OutputStream outputStream, Document document, boolean paginate) {
		
		
		try {
			List<InputStream> pdfs = streamOfPDFFiles;
			List<PdfReader> readers = new ArrayList<>();
			int totalPages = 0;
			Iterator<InputStream> iteratorPDFs = pdfs.iterator();
			
			// Create Readers for the pdfs.
			while (iteratorPDFs.hasNext()) {
				InputStream pdf = iteratorPDFs.next();
				PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
				totalPages += pdfReader.getNumberOfPages();
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);
			
			document.open();
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
			// data
			
			PdfImportedPage page;
			int currentPageNumber = 0;
			int pageOfCurrentReaderPDF = 0;
			Iterator<PdfReader> iteratorPDFReader = readers.iterator();
			
			// Loop through the PDF files and add to the output.
			while (iteratorPDFReader.hasNext()) {
				PdfReader pdfReader = iteratorPDFReader.next();
				
				// Create a new page in the target for each source page.
				while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
					document.newPage();
					pageOfCurrentReaderPDF++;
					currentPageNumber++;
					page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
					cb.addTemplate(page, 0, 0);
					
					// Code for pagination.
					if (paginate) {
						cb.beginText();
						cb.setFontAndSize(bf, 9);
						cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "" + currentPageNumber + " of " + totalPages, 520, 5, 0);
						cb.endText();
					}
				}
				pageOfCurrentReaderPDF = 0;
			}
			outputStream.flush();
			document.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen()) document.close();
			try {
				if (outputStream != null) outputStream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
