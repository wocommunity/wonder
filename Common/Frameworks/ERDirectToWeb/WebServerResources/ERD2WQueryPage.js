var ERD2WQueryPage = {

    /**
     * When the 'null' query checkbox for a query field is checked, this method toggles the 'disabled' attribute of the
     * corresponding form inputs.
     * @param evt to handle
     */
	toggleFormFieldsCorrespondingToNullQueryCheckbox: function(evt) {
		// Get the target element.
		var targEl;
		if (!evt) { var evt = window.event; }
		if (evt.target) { targEl = evt.target; }
		else if (evt.srcElement) { targEl = evt.srcElement; } // Stupid IE
		if (targEl.nodeType == 3) { targEl = targEl.parentNode; } // Defeat Safari bug

		// Get the table cell containing the target element.  Must ignore any text nodes.
		var targetCell = targEl;
		do { targetCell = targetCell.parentNode; } while(targetCell.nodeType != 1 || targetCell.tagName === undefined || targetCell.tagName.toLowerCase() != 'td');

		// Get the previous table cell, containing the form inputs.  Must ignore any text nodes.
		var formInputsCell = targetCell;
		do { formInputsCell = formInputsCell.previousSibling; } while(formInputsCell.nodeType != 1 || formInputsCell.tagName === undefined || formInputsCell.tagName.toLowerCase() != 'td');

		// Get the form inputs and selects in the table cell.
		var inputs = formInputsCell.getElementsByTagName('input');
		var selects = formInputsCell.getElementsByTagName('select');

		// Process the form elements.
		for (var i = 0; i < inputs.length; i++) {
			var inputEl = inputs[i];
			inputEl.disabled = targEl.checked;
		}

		for (i = 0; i < selects.length; i++) {
			var selectEl = selects[i];
			selectEl.disabled = targEl.checked;
		}
	} // end function
	
};
