package er.excel;

import org.apache.poi.hssf.usermodel.*;

import com.webobjects.appserver.*;

/**
 * Class for Excel Component EGRow.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Wed Mar 03 2004
 * @project ExcelGenerator
 */

public class EGRow extends EGComponent {
    protected HSSFRow _row;
    protected HSSFSheet _sheet;

	
    /**
     * Public constructor
     * @param context the context
     */
    public EGRow(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	_row = null;
    	_sheet = null;
    	super.reset();
    }
    
    public HSSFSheet sheet() {
    	if(_sheet == null) {
    		_sheet = (HSSFSheet)valueForBinding("sheet");
    		if(_sheet == null) {
    			EGComponent parent = parentExcelComponent();
    			if(!(parent instanceof EGSheet)) {
    				throw new IllegalStateException("EGRow must live in a EGSheet!");
    			}
    			_sheet = ((EGSheet)parent).sheet();
    		}
    	}
    	return _sheet;
    }

    public HSSFRow row() {
    	if(_row == null) {
    		_row = sheet().createRow(sheet().getLastRowNum());
    	}
    	return _row;
    }
    
    public void setRow(HSSFRow row) {
    	_row = row;
    }

    public void setSheet(HSSFSheet sheet) {
    	_sheet = sheet;
    }

}
