package er.examples.textsearchdemo.components.sections;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class AutoSuggest extends SimpleSearch {
	
    public AutoSuggest(WOContext context) {
        super(context);
    }
    
    public NSArray<String> currentValues() {
    	NSArray<String> result = new NSArray<String>();
    	if (searchString != null) {
    		try {
    			result = moviesIndex().findTermStringsForPrefix("content", searchString);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	return result;
    }
    
}