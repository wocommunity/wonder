package er.directtoweb.components.dates;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryDateRange;

/**
 * <span class="ja">このプロパティ・レベル・コンポーネントは date のクエリを 開始日と終了日のパラメータでビルドします。</span>
 */
public class ERD2WQueryDateRange extends D2WQueryDateRange {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WQueryDateRange(WOContext context) {
        super(context);
    }
/*
    protected static final NSTimestampFormatter DATE_FORMAT =
    new NSTimestampFormatter("%m/%d/%Y");
    protected static final NSTimestampFormatter DATE_FORMAT_YEAR_TWO_DIGITS =
        new NSTimestampFormatter("%m/%d/%y") ;

    public String propertyKey() {  return (String)localContext.valueForKey("propertyKey"); }

    public WODisplayGroup _displayGroup;
    public WODisplayGroup displayGroup() { return _displayGroup; }
    public D2WContext localContext;

    public boolean isStateless() { return false; }
    public boolean synchronizesVariablesWithBindings() { return true; }

    public void setMinValue(String min) {
        _minValue=min;
        displayGroup().queryMin().takeValueForKey(dateForString(min), propertyKey());
    }

    public void setMaxValue(String max) {
        _maxValue=max;
        displayGroup().queryMax().takeValueForKey(dateForString(max), propertyKey());
    }

    private String _minValue;
    public String minValue() {
        if(_minValue == null){
            _minValue=stringForDate((NSTimestamp)displayGroup().queryMin().valueForKey(propertyKey()));
        }
        return _minValue;
    }

    private String _maxValue;
    public String maxValue() {
        if (_maxValue == null) {
            _maxValue=stringForDate((NSTimestamp)displayGroup().queryMax().valueForKey(propertyKey()));
        }
        return _maxValue;
    }

    private String stringForDate(NSTimestamp d) {
        String result=null;
        if(d != null) {
            try {
                result = DATE_FORMAT.format(d);
            } catch(IllegalArgumentException nsfe) {}
        }
        return result;
    }

    public NSTimestamp dateForString(String dateString) {
        NSTimestamp date = null;
        try {
            if(dateString!=null) {
                boolean dateIsValid = false;
                NSMutableArray components = new NSMutableArray(NSArray.componentsSeparatedByString(dateString, "/"));
                if (components.count() == 3) {
                    String monthString = (String)components.objectAtIndex(0);
                    if (monthString.length() == 1)
                        components.replaceObjectAtIndex("0" + monthString, 0);
                    String dayString = (String)components.objectAtIndex(1);
                    if (dayString.length() == 1)
                        components.replaceObjectAtIndex("0" +dayString, 1);
                    String yearString = (String)components.objectAtIndex(2);
                    //String yearString = dateString.substring(dateString.lastIndexOf("/")+1, dateString.length());
                    String modifiedDateString = components.componentsJoinedByString("/");
                    java.text.Format formatter=yearString.length()==2 ? DATE_FORMAT_YEAR_TWO_DIGITS : DATE_FORMAT;
                    date = (NSTimestamp) formatter.parseObject(modifiedDateString);
                    String reformattedDate=formatter.format(date);
                    dateIsValid = reformattedDate.equals(modifiedDateString);
                }
                if (!dateIsValid)
                    throw ERXValidationFactory.defaultFactory().createException(null, propertyKey(), dateString, "InvalidDateFormatException");

            }
        } catch (java.text.ParseException nspe) {
            NSValidation.ValidationException v =
            ERXValidationFactory.defaultFactory().createException(null, propertyKey(), dateString, "InvalidDateFormatException");
            parent().validationFailedWithException( v, date, propertyKey());
        } catch (NSValidation.ValidationException v) {
            parent().validationFailedWithException(v,date,propertyKey());
        } catch(Exception e) {
            parent().validationFailedWithException(e,date,propertyKey());
        }

        return date;
    }*/
}
