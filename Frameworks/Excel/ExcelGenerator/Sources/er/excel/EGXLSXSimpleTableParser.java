package er.excel;

import java.io.InputStream;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.webobjects.foundation.NSDictionary;

/**
 * Instances of EGXLSXSimpleTableParser are used to generate Excel spreadsheets
 * in XLSX format.
 * 
 * @author Michael Hast on Jun 6, 2016
 */
public class EGXLSXSimpleTableParser extends EGSimpleTableParser {
	
//	private static final Logger log = LoggerFactory.getLogger(EGXLSXSimpleTableParser.class);
//	
//	private InputStream _contentStream;
//	private XSSFWorkbook _workbook;
//	private NSMutableDictionary<String, NSDictionary<String, String>> _fontDicts;
//	private NSMutableDictionary<String, NSDictionary<String, String>> _styleDicts;
//	private NSMutableDictionary<String, XSSFFont> _fonts = new NSMutableDictionary<String, XSSFFont>();
//	private NSMutableDictionary<NSDictionary<String, ?>, XSSFCellStyle> _styles = new NSMutableDictionary<NSDictionary<String, ?>, XSSFCellStyle>();
//	
	public EGXLSXSimpleTableParser(InputStream contentStream) {
		this(contentStream, null, null);
 	}
	
	public EGXLSXSimpleTableParser(InputStream contentStream, NSDictionary<String, NSDictionary<String, String>> fontDicts, NSDictionary<String, NSDictionary<String, String>> styleDicts) {
		super(contentStream, fontDicts, styleDicts);
	}
//
// EGSimpleTableParser API
//
	@Override
	protected Workbook createWorkbook() {
		return new XSSFWorkbook();
	}
	
	@Override
	protected RichTextString createRichTextString(Object value) {
		return new XSSFRichTextString(value != null ? value.toString() : null);
	}
}
