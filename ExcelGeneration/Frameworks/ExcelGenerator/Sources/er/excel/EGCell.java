package er.excel;

import org.apache.poi.hssf.usermodel.*;

import com.webobjects.appserver.*;

/**
 * Class for Excel Component EGCell.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Wed Mar 03 2004
 * @project ExcelGenerator
 */

public class EGCell extends EGComponent {
    protected HSSFCell _cell;
    protected HSSFRow _row;
    
    /**
     * Public constructor
     * @param context the context
     */
    public EGCell(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	_row = null;
    	_cell = null;
    	super.reset();
    }
    
    public HSSFRow row() {
    	if(_row == null) {
    		_row = (HSSFRow)valueForBinding("row");
    		if(_row == null) {
    			EGComponent parent = parentExcelComponent();
    			if(!(parent instanceof EGRow)) {
    				throw new IllegalStateException("EGCell must live in a EGRow!");
    			}
    			_row = ((EGRow)parent).row();
    		}
    	}
    	return _row;
    }
    
    public HSSFCell cell() {
    	if(_cell == null) {
    		_cell = row().createCell(row().getLastCellNum());
    	}
    	return _cell;
    }
    
    public void setRow(HSSFRow row) {
    	_row = row;
    }

    public void setCell(HSSFCell cell) {
    	_cell = cell;
    }
}
