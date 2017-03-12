package er.excel;

import java.io.InputStream;

import com.webobjects.appserver.WOContext;

/**
 * Class for XLSX Excel Component EGXLSXWrapper.
 *
 * @binding sample sample binding explanation
 *
 * @author Michael Hast on Jun 6, 2016 9:22:46 AM
 */
public class EGXLSXWrapper extends EGWrapper {
	
	public EGXLSXWrapper(WOContext context) {
		super(context);
	}
//
// EGWrapper API
//
	@Override
	protected String defaultFilename() {
		return "results.xlsx";
	}
	
	@Override
	protected String contentType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}
	
	@Override
	protected EGSimpleTableParser parser(InputStream stream) {
		return new EGXLSXSimpleTableParser(stream, fonts(), styles());
	}
}
