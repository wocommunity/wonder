package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WUtils;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.foundation.ERXValueUtilities;
import er.modern.components.ERMDatePicker;

/**
 * D2WEditComponent based on ERMDatePicker.
 * 
 * @d2wKey formatter - string - date format string
 * @d2wKey datePickerDragDisabled - boolean - disable dragging on date picker
 * @d2wKey datePickerCssFile - string - file for alternate css file (default is datepicker.css)
 * @d2wKey datePickerCssFramework - string - framework for alternate css file (default is ERModernDirectToWeb)
 * @d2wKey datePickerConstrainSelection - boolean - when fillGrid is enalbed, constrain selection to current month.
 * @d2wKey datePickerFillGrid - boolean - fill the complete grid not just current month.
 * @d2wKey datePickerFinalOpacity - number - Final calendar overlay opacity (20 - 100)
 * @d2wKey datePickerDisabledDays - string - Identifies any days on calendar that are disabled (format: [0,0,0,0,0,1,1])
 * @d2wKey datePickerDisabledDates - string - Range of disabled dates (YYYYMMDD:YYYYMMDD or YYYYMMDD if single date)
 * @d2wKey datePickerEnabledDates - string - Range of enabled dates (YYYYMMDD:YYYYMMDD or YYYYMMDD if single date)
 * @d2wKey datePickerHighlightDays - string - Identifies what days are highlighted on calendar (format: [1,1,0,0,0,0,0])
 * @d2wKey datePickerNoFadeEffect - boolean - disables fade in/out animation
 * @d2wKey datePickerNoTodayButton - boolean - hides 'Today' button
 * @d2wKey datePickerRangeLow - string - specifies earliest selectable date (format: YYYYMMDD)
 * @d2wKey datePickerRangeHigh - string - specifies latest selectable date (format:YYYYMMDD)
 * @d2wKey datePickerHideInput - boolean - hide the input field (don't use if hideControl is true)
 * @d2wKey datePickerHideControl - boolean - hide the calendar button (don't use if hideInput is true)
 * 
 * @author david
 *
 */
public class ERMDDatePicker extends ERDCustomEditComponent {
	
    private String _formatter;
    private String _dateReadableDescription;
    
	public ERMDDatePicker(WOContext context) {
        super(context);
    }
	
    /**
     * Format string for the date text fields
     * 
     * @return
     */
	public String formatter() {
		if(_formatter == null) {
			_formatter = (String)valueForBinding("formatter");
		}
		if(_formatter == null || _formatter.length() == 0) {
			_formatter = ERXTimestampFormatter.DEFAULT_PATTERN;
		}
		return _formatter;
	}

	public void setFormatter(String formatter) {
		_formatter = formatter;
	}
	
    /**
     * Convenience accessor for the readable date format description
     * 
     * @return
     */
	public String dateReadableDescription() {
		if (_dateReadableDescription == null) {
			_dateReadableDescription = D2WUtils.readableDateFormatDescription(formatter());
		}
		return _dateReadableDescription;
	}
	
	// DatePicker options

	/**
	 * {@link ERMDatePicker} option: is drag disabled
	 * 
	 * @return
	 */
	public Boolean dragDisabled() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerDragDisabled"), null);
	}

	/**
	 * {@link ERMDatePicker} option: name of the custom css file
	 * 
	 * @return
	 */
	public String cssFile() {
		return (String)valueForBinding("datePickerCssFile");
	}

	/**
	 * {@link ERMDatePicker} option: name of the custom css file framework
	 * 
	 * @return
	 */
	public String cssFramework() {
		return (String)valueForBinding("datePickerCssFramework");
	}

	/**
	 * {@link ERMDatePicker} option: should selection be constrained to current month (if fill grid is true)
	 * 
	 * @return
	 */
	public Boolean constrainSelection() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerConstrainSelection"), null);
	}

	/**
	 * {@link ERMDatePicker} option: fill the entire grid, not just the current month
	 * 
	 * @return
	 */
	public Boolean fillGrid() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerFillGrid"), null);
	}

	/**
	 * {@link ERMDatePicker} option: sets the final opacity
	 * 
	 * @return
	 */
	public String finalOpacity() {
		return (String)valueForBinding("datePickerFinalOpacity");
	}

	/**
	 * {@link ERMDatePicker} option: array (string in js format) of disabled days of the week (i.e: [0,0,0,0,0,1,1])
	 * 
	 * @return
	 */
	public String disabledDays() {
		return (String)valueForBinding("datePickerDisabledDays");
	}
	
	/**
	 * {@link ERMDatePicker} option: date range of disabled dates
	 * 
	 * @return
	 */
	public String disabledDates() {
		return (String)valueForBinding("datePickerDisabledDates");
	}


	/**
	 * {@link ERMDatePicker} option: date range of enabled dates
	 * 
	 * @return
	 */
	public String enabledDates() {
		return (String)valueForBinding("datePickerEnabledDates");
	}

	/**
	 * {@link ERMDatePicker} option: array (string in js format) of highlighted days of the week
	 * 
	 * @return
	 */
	public String highlightDays() {
		return (String)valueForBinding("datePickerHighlightDays");
	}

	/**
	 * {@link ERMDatePicker} option: disable fade effect
	 * 
	 * @return
	 */
	public Boolean noFadeEffect() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerNoFadeEffect"), null);
	}

	/**
	 * {@link ERMDatePicker} option: don't show today button
	 * 
	 * @return
	 */
	public Boolean noTodayButton() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerNoTodayButton"), null);
	}

	/**
	 * {@link ERMDatePicker} option: lowest selectable date
	 * 
	 * @return
	 */
	public String rangeLow() {
		return (String)valueForBinding("datePickerRangeLow");
	}

	/**
	 * {@link ERMDatePicker} option: highest selectable date
	 * 
	 * @return
	 */
	public String rangeHigh() {
		return (String)valueForBinding("datePickerRangeHigh");
	}
	
	/**
	 * {@link ERMDatePicker} option: hide the input text field
	 * 
	 * @return
	 */
	public Boolean hideInput() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerHideInput"), null);
	}
	
	/**
	 * {@link ERMDatePicker} option: hide the calendar control
	 * 
	 * @return
	 */
	public Boolean hideControl() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerHideControl"), null);
	}
}