package er.grouping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimestamp;

/**
 * Converts values. The main reason for this class is to provide a
 * point where you can patch into the internals of the grouping framework
 * without needing to override many classes.
 * You'd set an instance of your subclass if you want to deal with
 * simple Timestamps, Dates or the like.
 */
public class DRValueConverter {
    private static final Logger log = LoggerFactory.getLogger(DRValueConverter.class);
    
    public NSTimestamp timestampForValue(Object v) {
        if(v instanceof NSTimestamp) {
            return (NSTimestamp)v;
        }
        return null;
    }
    public double doubleForValue(Object v) {
        double scr = 0.0;
        if(v == null) {
            return 0.0;
        } else if (v instanceof String) {
            try {
                scr = Double.parseDouble((String)v);
            } catch(NumberFormatException e) {
                log.error("Not a number: {}", v);
                scr = 0.0;
            }
        } else if(v instanceof Number){
            Number vv = (Number)v;
            scr = vv.doubleValue();
        } else if (v instanceof NSTimestamp) {
            NSTimestamp vv = (NSTimestamp)v;
            scr = vv.getTime() / 1000.0;
        } else if(v == NSKeyValueCoding.NullValue) {
            scr = 0.0;
        } else {
            try {
                scr = Double.parseDouble(v.toString());
            } catch(NumberFormatException ex) {
                log.error("Not a number: {}", v);
                scr = 0.0;
            }
        }
        return scr;
    }
   
    public Number numberForValue(Object v) {
        double vv = doubleForValue(v);
        Number scr = Double.valueOf(vv);
        return scr;
    }
    
    private static DRValueConverter _converter = new DRValueConverter();

    public static DRValueConverter converter() {
        return _converter;
    }
    public static void setConverter(DRValueConverter value) {
        _converter = value;
    }
}
