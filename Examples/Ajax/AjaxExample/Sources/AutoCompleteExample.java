import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.example.ExampleDataFactory;
import er.ajax.example.Word;


public class AutoCompleteExample extends WOComponent {
    
	static Logger log = Logger.getLogger(AutoCompleteExample.class);

	public String value;
    public Word currentValue;
    public Word selectedValue;
    
    public NSArray allValues;
    
	public String value2;
    public Word selectedValue2;

    public AutoCompleteExample(WOContext context) {
    	super(context);
    	allValues = ExampleDataFactory.allWords();
    }

    /**
     * This method gets called after every keystroke, we check the value variable and return the 10 entries
     * in allValues that contain this value.
     */
    public NSArray currentValues(String v) {
        NSMutableArray<Word> result = new NSMutableArray<Word>();
        for(Enumeration e = allValues.objectEnumerator(); e.hasMoreElements() && result.count() < 10;) {
        	Word c = (Word) e.nextElement();
            if(v == null || c.name.toLowerCase().indexOf(v.toLowerCase()) >= 0) {
                result.addObject(c);
            }
        }
        return result;
    }
    
    public NSArray currentValues() {
    	return currentValues(value);
    }
    
    public NSArray currentValues2() {
    	return currentValues(value2);
    }
    
    public WOActionResults submitted() {
    	System.out.println("AutoCompleteExample.submitted: " + value + ", " + selectedValue);
    	System.out.println("AutoCompleteExample.submitted: " + value2 + ", " + selectedValue2);
    	return null;
    }
}
