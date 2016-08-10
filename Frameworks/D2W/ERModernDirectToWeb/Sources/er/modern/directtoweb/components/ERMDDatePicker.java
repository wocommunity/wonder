package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WUtils;

import er.coolcomponents.CCDatePicker;
import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.foundation.ERXValueUtilities;

/**
 * D2WEditComponent based on CCDatePicker. To use localized date formats, define the 
 * patterns in your Localizable.strings, e.g. "defaultDateFormat" = "%d.%m.%Y". Then
 * add one or more sets of rules like these:
 * <pre>
 * 60 : true => defaultDateFormat = "D2W.defaultDateFormat" [er.directtoweb.ERDDelayedLocalizedAssignment]
 * 60 : smartAttribute.prototypeName = 'dateTime' => formatter = "defaultDateFormat" [er.directtoweb.assignments.delayed.ERDDelayedKeyValueAssignment]
 * </pre>
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
	
    private static final long serialVersionUID = 1L;

    private String _formatter;
    private String _dateReadableDescription;
    
	public ERMDDatePicker(WOContext context) {
        super(context);
    }
	
    /**
     * Format string for the date text fields
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
     */
	public String dateReadableDescription() {
		if (_dateReadableDescription == null) {
			_dateReadableDescription = D2WUtils.readableDateFormatDescription(formatter());
		}
		return _dateReadableDescription;
	}
	
	// DatePicker options

	/**
	 * {@link CCDatePicker} option: is drag disabled
	 */
	public Boolean dragDisabled() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerDragDisabled"), null);
	}

	/**
	 * {@link CCDatePicker} option: name of the custom css file
	 */
	public String cssFile() {
		return (String)valueForBinding("datePickerCssFile");
	}

	/**
	 * {@link CCDatePicker} option: name of the custom css file framework
	 */
	public String cssFramework() {
		return (String)valueForBinding("datePickerCssFramework");
	}

	/**
	 * {@link CCDatePicker} option: should selection be constrained to current month (if fill grid is true)
	 */
	public Boolean constrainSelection() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerConstrainSelection"), null);
	}

	/**
	 * {@link CCDatePicker} option: fill the entire grid, not just the current month
	 */
	public Boolean fillGrid() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerFillGrid"), null);
	}

	/**
	 * {@link CCDatePicker} option: sets the final opacity
	 */
	public String finalOpacity() {
		return (String)valueForBinding("datePickerFinalOpacity");
	}

	/**
	 * {@link CCDatePicker} option: array (string in js format) of disabled days of the week (i.e: [0,0,0,0,0,1,1])
	 */
	public String disabledDays() {
		return (String)valueForBinding("datePickerDisabledDays");
	}
	
	/**
	 * {@link CCDatePicker} option: date range of disabled dates
	 */
	public String disabledDates() {
		return (String)valueForBinding("datePickerDisabledDates");
	}


	/**
	 * {@link CCDatePicker} option: date range of enabled dates
	 */
	public String enabledDates() {
		return (String)valueForBinding("datePickerEnabledDates");
	}

	/**
	 * {@link CCDatePicker} option: array (string in js format) of highlighted days of the week
	 */
	public String highlightDays() {
		return (String)valueForBinding("datePickerHighlightDays");
	}

	/**
	 * {@link CCDatePicker} option: disable fade effect
	 */
	public Boolean noFadeEffect() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerNoFadeEffect"), null);
	}

	/**
	 * {@link CCDatePicker} option: don't show today button
	 */
	public Boolean noTodayButton() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerNoTodayButton"), null);
	}

	/**
	 * {@link CCDatePicker} option: lowest selectable date
	 */
	public String rangeLow() {
		return (String)valueForBinding("datePickerRangeLow");
	}

	/**
	 * {@link CCDatePicker} option: highest selectable date
	 */
	public String rangeHigh() {
		return (String)valueForBinding("datePickerRangeHigh");
	}
	
	/**
	 * {@link CCDatePicker} option: hide the input text field
	 */
	public Boolean hideInput() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerHideInput"), null);
	}
	
	/**
	 * {@link CCDatePicker} option: hide the calendar control
	 */
	public Boolean hideControl() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding("datePickerHideControl"), null);
	}
}
