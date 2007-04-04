/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

/**
 * Enhanced table component that adds the ability to have the
 * table layed out in a vertical orientation and adds the
 * ability to specify an array of header images that appear
 * in the header cells of the table. Corrects a bug intorduced
 * in WO 5.1 where OutOfBounds exceptions are thrown. Note that
 * this component subclasses WOTable from this framework, not
 * the WOTable in com.webobjects.woextensions. The reason for
 * this is that all of the instance variables are private in
 * JavaWOExtensions WOTable.<br/>
 * <br/>
 * Synopsis:<br/>
 * list=<i>anArray</i>;item=<i>aSettableObject</i>;[col=<i>aSettableNumber</i>;][index=<i>aSettableNumber</i>;][row=<i>aSettableNumber</i>;]
 * [maxColumns=<i>aNumber</i>;][tableBackgroundColor=<i>aString</i>;][border=<i>aNumber</i>;][cellpadding=<i>aNumber</i>;][cellspacing=<i>aNumber</i>;]
 * [rowBackgroundColor=<i>aString</i>;][cellBackgroundColor=<i>aString</i>;][cellAlign=<i>aNumber</i>;][cellVAlign=<i>aNumber</i>;]
 * [cellWidth=<i>aNumber</i>;][tableWidth=<i>aNumber</i>;]
 * [goingVertically=<i>aBoolean</i>;][headerImages=<i>anArray</i>;][headerRowBackgroundColor=<i>aColor</i>;]
 *
 * @binding col pushed to the parent with the current
 *		column number
 * @binding index pushed to the parent indicating
 *		the current index
 * @binding list of objects to construct the table for
 * @binding maxColumns maximum number of columns
 * @binding fillColumns when true, loops until the last row is filled even when no more elements are left in the list
 * @binding row pushed to the parent with the current
 *		row number
 * @binding item pushed to the parent with the
 *		current object from the list
 * @binding tableClass CSS class for the table
 * @binding tableBackgroundColor background color for table
 * @binding border table border
 * @binding cellpadding cell padding
 * @binding cellspacing cell spacing
 * @binding rowBackgroundColor background color to be
 *		used for the rows of the table
 * @binding rowClass CSS class for the row
 * @binding cellBackgroundColor background color for the cell
 * @binding cellAlign cell's alignment
 * @binding cellVAlign cell's vertical alignment
 * @binding cellWidth cell's width
 * @binding cellClass CSS class for the cell
 * @binding tableWidth table width
 * @binding goingVertically boolean if the list should be
 *		layed out horizontally or vertically.
 * @binding headerImages array of images to be displayed
 *		in the header cells of the table
 * @binding headerRowBackgroundColor background color for the 
 *		header row
 */
public class ERXTable extends WOTable {

	/** used in the repetition for header images */
	protected String header;
	/** caches the value from the binding goingVertical */
	protected Boolean _goingVertically;
	protected Boolean _showIndex;
	protected int index = 0;

	/**
	 * Public constructor
	 * @param context the context
     */
    public ERXTable(WOContext context) {
        super(context);
    }
    
    public String header() {
      return header;
    }

    public int colCount() {
    	if(_colCount == -1) {
    		if(ERXValueUtilities.booleanValue(valueForBinding("fillColumns"))) {
                _colCount = maxColumns();
    		} else {
    			_colCount = super.colCount();
    		}
    	}
    	return _colCount;
    }
    
    /**
     * Component is stateless.
     * @return true
     */
    // CHECKME: This shouldn't be needed, although for some strange reason it was needed at one point.
    public boolean isStateless() { return true; }

    /**
     * resets the cached variables
     */
    protected void _resetInternalCaches() {
        super._resetInternalCaches();
        _goingVertically = null;
		  _showIndex = null;
    }

    /**
     * Denotes if the list should be layed out vertically
     * or horizontally. This is the boolean value from the
     * binding: <b>goingVertically</b>
     * @return if the list of items should be layed out
     *		vertically.
     */
    public boolean goingVertically() {
        if (_goingVertically == null) {
            _goingVertically=ERXValueUtilities.booleanValue(valueForBinding("goingVertically")) ?
            Boolean.TRUE : Boolean.FALSE;
        }
        return _goingVertically.booleanValue();
    }

    /**
     * Overridden to account for when goingVertical is
     * enabled. Also corrects a bug from the WO 5.1
     * release that would throw OutOfBoundsExceptions.
     * This method pushs the current item up to the
     * parent component.
     */
    public void pushItem() {
        NSArray aList = list();
        //int index;
        if (goingVertically()) {
            int c=aList.count() % maxColumns();
            index = currentRow+rowCount()*currentCol;
            if (c!=0 && currentCol>c) index-=(currentCol-c);
        } else {
            index = currentCol+maxColumns()*currentRow;
        }
        // WO 5.1 guarding against OOB index
        // WORepetition count=x seems to go to x+1 in 5.1, even though it is not displayed
        Object item = index < aList.count() ? aList.objectAtIndex(index) :  null;
        setValueForBinding(item, "item");
        if (canSetValueForBinding("row"))
            setValueForBinding(ERXConstant.integerForInt(currentRow), "row");
        if (canSetValueForBinding("col"))
            setValueForBinding(ERXConstant.integerForInt(currentCol), "col");
        if (canSetValueForBinding("index"))
            setValueForBinding(ERXConstant.integerForInt(index), "index");
        currentItemIndex++;
    }

    /**
     * Conditional to determine if the binding: <b>headerImages</b>
     * is present.
     * @return if the component has the binding headerImages.
     */
    public boolean hasHeaders() { return hasBinding("headerImages"); }

	 /**
		 * Conditional to determine if the index should be shown
     * @return if the index should be shown from the bindings
     */
    public boolean showIndex() {
		 if (_showIndex == null) {
			 _showIndex=ERXValueUtilities.booleanValue(valueForBinding("showIndex")) ?
			 Boolean.TRUE : Boolean.FALSE;
		 }
		 return _showIndex.booleanValue();
	 }

	 	 /**
		 * Returns a displayable value for the index starting from 1 instead of 0.
		  * @return a displayable value for the index starting from 1 instead of 0.
		  */
	 public int displayInt(){
		 return index+1;
	 }
}
