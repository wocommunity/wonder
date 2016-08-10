package er.extensions.components._private;

import java.text.Format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.appserver.ERXSession;
import er.extensions.formatters.ERXNumberFormatter;
import er.extensions.formatters.ERXTimestampFormatter;

/**
 * Reimplementation of WOString that can resolve localized format strings. This
 * is very useful as most of the components in DirectToWeb use a "format"-String
 * binding - so to use localized patterns, you'd need to re-implement all these
 * components.
 * 
 * @author ak
 */
public class ERXWOString extends WODynamicElement {
    private static final Logger log = LoggerFactory.getLogger(ERXWOString.class);

    protected WOAssociation _dateFormat;
    protected WOAssociation _numberFormat;
    protected WOAssociation _formatter;
    protected WOAssociation _value;
    protected WOAssociation _escapeHTML;
    protected WOAssociation _valueWhenEmpty;

    boolean                 _shouldFormat;

    public ERXWOString(String s, NSDictionary nsdictionary, WOElement woelement) {
        super(null, null, null);
        _value = (WOAssociation) nsdictionary.objectForKey("value");
        if (_value == null) { throw new WODynamicElementCreationException("<" + getClass().getName()
                + "> ( no 'value' attribute specified."); }
        _valueWhenEmpty = (WOAssociation) nsdictionary.objectForKey("valueWhenEmpty");
        _escapeHTML = (WOAssociation) nsdictionary.objectForKey("escapeHTML");
        _dateFormat = (WOAssociation) nsdictionary.objectForKey("dateformat");
        _numberFormat = (WOAssociation) nsdictionary.objectForKey("numberformat");
        _formatter = (WOAssociation) nsdictionary.objectForKey("formatter");

        if (_dateFormat != null || _numberFormat != null || _formatter != null)
            _shouldFormat = true;
        else
            _shouldFormat = false;

        if ((_dateFormat != null && _numberFormat != null) || (_formatter != null && _dateFormat != null)
                || (_formatter != null && _numberFormat != null)) { throw new WODynamicElementCreationException("<" + getClass().getName()
                + "> ( cannot have 'dateFormat' and 'numberFormat' or 'formatter' attributes at the same time."); }
    }

    @Override
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        WOComponent component = wocontext.component();
        Object valueInComponent = null;

        if (_value != null) {
            valueInComponent = _value.valueInComponent(component);
            if (_shouldFormat) {
                Format format = null;
                boolean hasFormatter = false;
                if (_formatter != null) {
                    format = (Format) _formatter.valueInComponent(component);
                }
                if (format == null) {
                    if (_dateFormat != null) {
                        String formatString = (String) _dateFormat.valueInComponent(component);
                        if (formatString == null) {
                            format = ERXTimestampFormatter.defaultDateFormatterForObject(formatString);
                        } else {
                            format = ERXTimestampFormatter.dateFormatterForPattern(formatString);
                        }
                    } else if (_numberFormat != null) {
                        String formatString = (String) _numberFormat.valueInComponent(component);
                        if (formatString == null) {
                            format = ERXNumberFormatter.defaultNumberFormatterForObject(valueInComponent);
                        } else {
                            format = ERXNumberFormatter.numberFormatterForPattern(formatString);
                        }
                    }
                } else {
                	hasFormatter = true;
                }
                if(valueInComponent == NSKeyValueCoding.NullValue) {
                	valueInComponent = null;
                }
                if (format != null) {
                	if (valueInComponent == null) {
                		// do nothing;
                	} else {
						if(ERXSession.autoAdjustTimeZone() &&
								!hasFormatter && 
        						format instanceof NSTimestampFormatter && 
        						wocontext.hasSession() && 
        						ERXSession.class.isAssignableFrom(wocontext.session().getClass())
                				) {
								
							synchronized(format) {
								ERXSession session = (ERXSession)wocontext.session();
								NSTimeZone zone = NSTimeZone._nstimeZoneWithTimeZone(session.timeZone());
								NSTimestampFormatter tsFormat = (NSTimestampFormatter)format;
								NSTimeZone parseZone = tsFormat.defaultParseTimeZone();
								NSTimeZone formatZone = tsFormat.defaultFormatTimeZone();
								tsFormat.setDefaultFormatTimeZone(zone);
								tsFormat.setDefaultParseTimeZone(zone);
		                		try {
		                            valueInComponent = format.format(valueInComponent);
		                        } catch (IllegalArgumentException ex) {
		                            log.info("Exception while formatting", ex);
		                            valueInComponent = null;
		                        } finally {
		                        	tsFormat.setDefaultFormatTimeZone(formatZone);
		                        	tsFormat.setDefaultParseTimeZone(parseZone);
		                        }
							}
						} else {
	                		try {
	                            valueInComponent = format.format(valueInComponent);
	                        } catch (IllegalArgumentException ex) {
	                            log.info("Exception while formatting", ex);
	                            valueInComponent = null;
	                        }
						}
                    }

                } else {
                    if (valueInComponent != null) {
                        log.debug("no formatter found! {}", valueInComponent);
                    }
                }
            }
        } else {
            log.warn("value binding is null !");
        }
        String stringValue = null;

        if (valueInComponent != null) stringValue = valueInComponent.toString();
        if ((stringValue == null || stringValue.length() == 0) && _valueWhenEmpty != null) {
            stringValue = (String) _valueWhenEmpty.valueInComponent(component);
            woresponse.appendContentString(stringValue);
        } else if (stringValue != null) {
            boolean escapeHTML = true;
            if (_escapeHTML != null) escapeHTML = _escapeHTML.booleanValueInComponent(component);
            if (escapeHTML)
                woresponse.appendContentHTMLString(stringValue);
            else
                woresponse.appendContentString(stringValue);
        }
    }
}