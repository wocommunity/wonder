package er.modern.directtoweb.components.query;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WUtils;

import er.coolcomponents.CCDatePicker;
import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Date range query component that uses {@link CCDatePicker}
 * 
 * @d2wKey formatter
 * @d2wKey propertyKey
 * @d2wKey datePickerDragDisabled
 * @d2wKey datePickerCssFile
 * @d2wKey datePickerCssFramework
 * @d2wKey datePickerConstrainSelection
 * @d2wKey datePickerFillGrid
 * @d2wKey datePickerFinalOpacity
 * @d2wKey datePickerDisabledDays
 * @d2wKey datePickerDisabledDates
 * @d2wKey datePickerEnabledDates
 * @d2wKey datePickerHighlightDays
 * @d2wKey datePickerNoFadeEffect
 * @d2wKey datePickerNoTodayButton
 * @d2wKey datePickerRangeLow
 * @d2wKey datePickerRangeHigh
 * @d2wKey datePickerHideInput
 * @d2wKey datePickerHideControl
 * 
 * @author davidleber
 *
 */
public class ERMD2WQueryDateRange extends ERDCustomQueryComponent {
	
    public static interface Keys extends ERDCustomQueryComponent.Keys {

        public static final String formatter = "formatter";
        public static final String propertyKey = "propertyKey";
        public static final String datePickerDragDisabled = "datePickerDragDisabled";
        public static final String datePickerCssFile = "datePickerCssFile";
        public static final String datePickerCssFramework = "datePickerCssFramework";
        public static final String datePickerConstrainSelection = "datePickerConstrainSelection";
        public static final String datePickerFillGrid = "datePickerFillGrid";
        public static final String datePickerFinalOpacity = "datePickerFinalOpacity";
        public static final String datePickerDisabledDays = "datePickerDisabledDays";
        public static final String datePickerDisabledDates = "datePickerDisabledDates";
        public static final String datePickerEnabledDates = "datePickerEnabledDates";
        public static final String datePickerHighlightDays = "datePickerHighlightDays";
        public static final String datePickerNoFadeEffect = "datePickerNoFadeEffect";
        public static final String datePickerNoTodayButton = "datePickerNoTodayButton";
        public static final String datePickerRangeLow = "datePickerRangeLow";
        public static final String datePickerRangeHigh = "datePickerRangeHigh";
        public static final String datePickerHideInput = "datePickerHideInput";
        public static final String datePickerHideControl = "datePickerHideControl";
        
    }
    
    protected String _formatter;
	private String _dateReadableDescription;
	private String _key;
	
    public ERMD2WQueryDateRange(WOContext context) {
        super(context);
    }
    
    /**
     * Format string for the date text fields
     */
	public String formatter() {
		if(_formatter == null) {
			_formatter = (String)valueForBinding(Keys.formatter);
		}
		if(_formatter == null || _formatter.length() == 0) {
			_formatter = ERXTimestampFormatter.DEFAULT_PATTERN;
		}
		return _formatter;
	}

	/**
	 * Convenience getter for the displayGroups queryMin value
	 */
	public Object minValue() {
		return displayGroup().queryMin().valueForKey(propertyKey());
	}

	/**
	 * Convenience setter for the displayGroup's qeryMin value
	 * 
	 * @param obj
	 */
	public void setMinValue(Object obj) {
		if (obj != null) {
			displayGroup().queryMin().takeValueForKey(obj, propertyKey());
		} else {
			displayGroup().queryMin().removeObjectForKey(propertyKey());
		}
	}

	/**
	 * Convenience getter for the displayGroups queryMax value
	 */
	public Object maxValue() {
		return displayGroup().queryMax().valueForKey(propertyKey());
	}

	/**
	 * Convenience setter for the displayGroup's qeryMax value
	 * 
	 * @param obj
	 */
	public void setMaxValue(Object obj) {
		if (obj != null) {
			displayGroup().queryMax().takeValueForKey(obj, propertyKey());
		} else {
			displayGroup().queryMax().removeObjectForKey(propertyKey());
		}
	}
	
	/**
	 * Convenience accessor for the current propertyKey
	 */
    public String propertyKey() {
        if(_key == null)
            _key = (String)valueForBinding(Keys.propertyKey);
        return _key;
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
	
	// DatePicker Options
	
	/**
	 * {@link CCDatePicker} option: is drag disabled
	 */
	public Boolean dragDisabled() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerDragDisabled), null);
	}

	/**
	 * {@link CCDatePicker} option: name of the custom css file
	 */
	public String cssFile() {
		return (String)valueForBinding(Keys.datePickerCssFile);
	}

	/**
	 * {@link CCDatePicker} option: name of the custom css file framework
	 */
	public String cssFramework() {
		return (String)valueForBinding(Keys.datePickerCssFramework);
	}

	/**
	 * {@link CCDatePicker} option: should selection be constrained to current month (if fill grid is true)
	 */
	public Boolean constrainSelection() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerConstrainSelection), null);
	}

	/**
	 * {@link CCDatePicker} option: fill the entire grid, not just the current month
	 */
	public Boolean fillGrid() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerFillGrid), null);
	}

	/**
	 * {@link CCDatePicker} option: sets the final opacity
	 */
	public String finalOpacity() {
		return (String)valueForBinding(Keys.datePickerFinalOpacity);
	}

	/**
	 * {@link CCDatePicker} option: array (string in js format) of disabled days of the week (i.e: [0,0,0,0,0,1,1])
	 */
	public String disabledDays() {
		return (String)valueForBinding(Keys.datePickerDisabledDays);
	}
	
	/**
	 * {@link CCDatePicker} option: date range of disabled dates
	 */
	public String disabledDates() {
		return (String)valueForBinding(Keys.datePickerDisabledDates);
	}

	/**
	 * {@link CCDatePicker} option: date range of enabled dates
	 */
	public String enabledDates() {
		return (String)valueForBinding(Keys.datePickerEnabledDates);
	}

	/**
	 * {@link CCDatePicker} option: array (string in js format) of highlighted days of the week
	 */
	public String highlightDays() {
		return (String)valueForBinding(Keys.datePickerHighlightDays);
	}

	/**
	 * {@link CCDatePicker} option: disable fade effect
	 */
	public Boolean noFadeEffect() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerNoFadeEffect), null);
	}

	/**
	 * {@link CCDatePicker} option: don't show today button
	 */
	public Boolean noTodayButton() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerNoTodayButton), null);
	}

	/**
	 * {@link CCDatePicker} option: lowest selectable date
	 */
	public String rangeLow() {
		return (String)valueForBinding(Keys.datePickerRangeLow);
	}

	/**
	 * {@link CCDatePicker} option: highest selectable date
	 */
	public String rangeHigh() {
		return (String)valueForBinding(Keys.datePickerRangeHigh);
	}
	
	/**
	 * {@link CCDatePicker} option: hide the input text field
	 */
	public Boolean hideInput() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerHideInput), null);
	}
	
	/**
	 * {@link CCDatePicker} option: hide the calendar control
	 */
	public Boolean hideControl() {
		return ERXValueUtilities.BooleanValueWithDefault(valueForBinding(Keys.datePickerHideControl), null);
	}
}
