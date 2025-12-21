package er.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openpdf.text.Document;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfImportedPage;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

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
	public static void concatPDFs(final List<InputStream> streamOfPDFFiles, final OutputStream outputStream, final boolean paginate) {
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
	org.openpdf.text.Document doc = new org.openpdf.text.Document(org.openpdf.text.PageSize.LETTER, 0.0F, 0.0F,0.0F,0.0F);
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
	 * @see org.openpdf.text.Document iText Document class
	 * @see org.openpdf.text.PageSize Page Size class in iText
	 *
	 * @author Larry Mills-Gahl &lt;lmg@webfarm.com&gt;
	 */

	public static void concatPDFs(final List<InputStream> streamOfPDFFiles, final OutputStream outputStream, final Document document, final boolean paginate) {


		try {
			final List<InputStream> pdfs = streamOfPDFFiles;
			final List<PdfReader> readers = new ArrayList<>();
			int totalPages = 0;


			// Create Readers for the pdfs.
			for (final InputStream pdf : pdfs) {
				final PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
				totalPages += pdfReader.getNumberOfPages();
			}
			// Create a writer for the outputstream
			final PdfWriter writer = PdfWriter.getInstance(document, outputStream);

			document.open();
			final BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			final PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
			// data

			PdfImportedPage page;
			int currentPageNumber = 0;
			int pageOfCurrentReaderPDF = 0;


			// Loop through the PDF files and add to the output.
			for (final PdfReader pdfReader : readers) {
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
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen()) {
				document.close();
			}
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (final IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
