package er.excel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;

import er.extensions.ERXFileUtilities;

/**
 * EGWorkbook is the "page" for the excel generation. It instantiates the 
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Wed Mar 03 2004
 * @project ExcelGenerator
 */


public class EGWorkbook extends EGComponent {
    protected HSSFWorkbook _workbook;
    
    /**
     * Public constructor
     * @param context the context
     */
    public EGWorkbook(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	_workbook = null;
    	super.reset();
    }
    
    public HSSFWorkbook workbook() {
    	if(_workbook == null) {
    		
    		_workbook = new HSSFWorkbook();
    	}
    	return _workbook;
    }

    public void setWorkbook(HSSFWorkbook workbook) {
    	_workbook = workbook;
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
		WOResponse newResponse = new WOResponse();
		
		super.appendToResponse(newResponse, context);
		String contentString = newResponse.contentString();
		EGSimpleTableParser parser = new EGSimpleTableParser(new ByteArrayInputStream(contentString.getBytes()));
		NSData data = parser.data();
		response.appendContentData(data);
		try {
			ERXFileUtilities.writeInputStreamToFile(data.stream(), new File("/Users/ak/Desktop/test.xls"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
