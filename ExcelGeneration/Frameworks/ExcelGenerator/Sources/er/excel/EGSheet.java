package er.excel;

import org.apache.poi.hssf.usermodel.*;

import com.webobjects.appserver.*;

/**
 * Class for Excel Component EGSheet.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Wed Mar 03 2004
 * @project ExcelGenerator
 */

public class EGSheet extends EGComponent {
    protected HSSFSheet _sheet;
    protected HSSFWorkbook _workbook;

	/**
     * Public constructor
     * @param context the context
     */
    public EGSheet(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	_workbook = null;
    	_sheet = null;
    	super.reset();
    }
    
    public HSSFWorkbook workbook() {
    	if(_workbook == null) {
    		_workbook = (HSSFWorkbook)valueForBinding("workbook");
    		if(_workbook == null) {
    			EGComponent parent = parentExcelComponent();
    			if(!(parent instanceof EGWorkbook)) {
    				throw new IllegalStateException("EGSheet must live in a EGWorkbook!");
    			}
    			_workbook = ((EGWorkbook)parent).workbook();
    		}
    	}
    	return _workbook;
    }

    public HSSFSheet sheet() {
    	if(_sheet == null) {
    		_sheet = workbook().createSheet();
    	}
    	return _sheet;
    }

    public void setSheet(HSSFSheet sheet) {
    	_sheet = sheet;
    }

    public void setWorkbook(HSSFWorkbook workbook) {
    	_workbook = workbook;
    }
}
