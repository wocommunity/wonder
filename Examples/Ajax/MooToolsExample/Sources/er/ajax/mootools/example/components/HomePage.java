package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOContext;

public class HomePage extends Main {
    public HomePage(WOContext context) {
        super(context);
    }
    
    public String pageTitle() {
    	return "MooTools meets Project Wonder's Ajax Framework version 3"; 
    }
}