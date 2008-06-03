//----------------------------------------------//
//	Created by: Romulo do Nascimento Ferreira	//
//	Email: romulo.nf@gmail.com					//
//----------------------------------------------//

// NOTICE: This code may be used to any purpose without further
// permission from the author. You may remove this notice from the
// final code, however its appreciated if you keep the author name/email.
// Email me if theres something needing improvement

// Chuck Hill: Translated to English, made many improvements, error condition handling
//             Moved functions into AjaxGrid object

var AjaxGrid = {
	isDragging : false,

	/*
	 * Adds drag and drop support to the table
	 */
	ajaxGrid_init : function(table) {
		table.onselectstart = function () { return false; } 
		table.onmousedown = function () { return false; }
		table.onmouseup = AjaxGrid.drop;
		
		numberOfColumns = table.rows[0].cells.length;
		tableRows = table.getElementsByTagName("TR");
	
		for (row=0; row < tableRows.length; row++) {
			tds = tableRows[row].cells;
			for (col=1; col<tds.length; col++) {
				AjaxGrid.drag(tds[col]);
				tds[col].onmouseover = AjaxGrid.paint;
				tds[col].onmouseout = AjaxGrid.paint;
				tds[col].originalClassName=tds[col].className;  // Save this to restore after a select and drag
			}
		}
	},


	/**
	 * Adds the dynamically generated drag handler to the passed object.
	 * The drag handler sets the style of the column being dragged and captures its index. It rejects 
	 * the drag if started over an <a> tag (this caused problems with the sort column links).
	 */
	drag : function(obj){
		if(!obj) return;
		obj.onmousedown = function(ev) {
			if (!ev) ev=window.event
		    if (ev.target) target = ev.target
		    else if (ev.srcElement) target=ev.srcElement
		    if ( ! AjaxGrid.isIgnoredElement(target))
		    {
			    columnAtual = AjaxGrid.cellIndex(this)
				for (x=0; x<tableRows.length; x++) {
					Element.addClassName(tableRows[x].cells[columnAtual], "ajaxGridSelected");
				}
				AjaxGrid.isDragging = true
				AjaxGrid.recordColumn(this);
			}
			return false;
		}
	},
		
		
	/**
	 * Returns true if the passed element is one that mouse click should be ignored for.  This
	 * allows the element to be accessed and prevents clicks on it from being used to re-order rows.
	 */
	isIgnoredElement : function(element) {
		nodeName = element.nodeName.toLowerCase();
		return nodeName == 'a' ||
		       nodeName == 'input' ||
		       nodeName == 'button' ||
		       nodeName == 'select' ||
		       nodeName == 'textarea';
	},


	/*
	 * Captures the index of the column being dragged
	 */
	recordColumn : function(obj) {
		columnIndex =  AjaxGrid.cellIndex(obj) 
		return columnIndex
	},


	/*
	 * Performs the actual re-ordering of the table in the browser and notifies the server of the change
	 */
	orderTd : function(obj) {
		destinationIndex =  AjaxGrid.cellIndex(obj);
		
		// Error and no-op handling
		// A destinationIndex of -1 means not a table cell and 0 means the left most column which is not dropable
		if (destinationIndex < 1 || columnIndex == destinationIndex) return
	  updateServerColumnOrder('sourceColumn=' + columnIndex + '&destinationColumn=' + destinationIndex);
	
		for (x=0; x<tableRows.length; x++) {
			tds = tableRows[x].cells
			var cell = tableRows[x].removeChild(tds[columnIndex])
			if (destinationIndex >= numberOfColumns || destinationIndex + 1 >= numberOfColumns) {
				tableRows[x].appendChild(cell)
			}
			else {
				tableRows[x].insertBefore(cell, tds[destinationIndex])
			}
		}
	},


	/*
	 * Drop handler.  Calls orderTd() to perform the acutal re-order and then resets the style
	 */
	drop : function(e) {
	    if ( ! AjaxGrid.isDragging) return;
		if (!e) e=window.event
		if (e.target) target = e.target
		else if (e.srcElement) target=e.srcElement
		AjaxGrid.orderTd(target)
		AjaxGrid.isDragging = false
		
		for(x=0; x<tableRows.length; x++) {
			for (y=1; y<tableRows[x].cells.length; y++) {
			  Element.removeClassName(tableRows[x].cells[y], "ajaxGridSelected");
			}
		}
	},


	/*
	 * Sets and resets the style as the drag operation moves over other columns
	 */
	paint : function(e) {
		if (!e) e=window.event
		ev = e.type
		if (ev == "mouseover") {
			if (AjaxGrid.isDragging) {
				for (x=0; x<tableRows.length; x++) {
					if (!Element.hasClassName(this, "ajaxGridSelected")) {
						Element.addClassName(tableRows[x].cells[AjaxGrid.cellIndex(this)], "ajaxGridHover");
					}
				}
			}
		}
		
		else if (ev == "mouseout") {
			for (x=0; x<tableRows.length; x++) {
				if (!Element.hasClassName(this, "ajaxGridSelected") && tableRows[x].cells[AjaxGrid.cellIndex(this)]) {
					Element.removeClassName(tableRows[x].cells[AjaxGrid.cellIndex(this)], "ajaxGridHover");
				}
			}
		}
	},


	/*
	 * Returns the cell index (index in the row, zero based) of el if it is a <td> or <th> tag. If el is not one of those tags,
	 * returns the cell index of the closest containing <td> or <th> tag.
	 */
	cellIndex : function(el) {
	    var ci = -1;
	    td = el;
	    if (el.nodeName.toLowerCase() !='td' && el.nodeName.toLowerCase() !='th') {
	    	td = AjaxGrid.ascendDOM(el, 'th');
	    	if (td == null) td = AjaxGrid.ascendDOM(el, 'td');
	    }
	    parent_row = AjaxGrid.ascendDOM(td, 'tr');
	    for (var i = 0; i < parent_row.cells.length; i++) {
	        if (td === parent_row.cells[i]) {
	            ci = i;
	        }
	    }
	    return ci;
	},
	

	/*
	 * Walks up the DOM tree from e and returns the closest element with a nodeName of target.
	 * Returns null if no such node is found.
	 */
	ascendDOM : function(e,target) {
	    while (e.nodeName.toLowerCase() !=target &&
	           e.nodeName.toLowerCase() !='html')
	        e=e.parentNode;
	    return (e.nodeName.toLowerCase() == 'html')? null : e;
	}

}