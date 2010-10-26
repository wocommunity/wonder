package er.movies;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.webobjects.foundation.NSTimestamp;

import er.chronic.Chronic;
import er.chronic.Options;
import er.chronic.utils.Span;

public class ChronicFormatter extends SimpleDateFormat {
    private Options options;
    private boolean isGuessingEarly = true;
    
    public ChronicFormatter(String pattern) {
        this(pattern, null, true);
    }
    
    public ChronicFormatter(String pattern, Options options) {
        this(pattern, options, true);
    }
    
    public ChronicFormatter(String pattern, Options options, boolean isGuessingEarly) {
        super(pattern);
        this.options = options;
        this.isGuessingEarly = isGuessingEarly;
    }
    
    public Options options() {
        if (options == null)
            options = new Options();
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
    
    public boolean isGuessingEarly() {
        return isGuessingEarly;
    }

    public void setIsGuessingEarly(boolean isGuessingEarly) {
        this.isGuessingEarly = isGuessingEarly;
    }

    @Override
    public NSTimestamp parseObject(String text) throws ParseException {
        NSTimestamp parsedTimestamp = null;
        
        try {
            // Attempt to parse the string with the given pattern. 
            Date parsedDate = super.parse(text);
            parsedTimestamp = new NSTimestamp(parsedDate);
        }
        catch (ParseException e) {
            
            // If the input doesn't match the pattern, use Chronic to parse the input.
            Span span = Chronic.parse(text, options());
            if (span == null) {
                throw e;
            }
            else {
                if (span.isSingularity() || isGuessingEarly()) {
                    parsedTimestamp = new NSTimestamp(span.getBeginCalendar().getTime());
                }
                else {
                    parsedTimestamp = new NSTimestamp(span.getEndCalendar().getTime());
                }
            }
        }
        
        return parsedTimestamp;
    }
    
}
