package er.examples.ui;

import java.util.Arrays;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;

public class GoogleChartsPage extends ERXComponent {

    public GoogleChartsPage(WOContext context) {
        super(context);
    }

    public NSArray someArrayOfData = new NSArray(Arrays.asList(0, 40, 10, 70, 20));
    
    public NSArray someArrayOfColors = new NSArray(Arrays.asList("FF0000", "00FF00"));
    
    public NSArray mojoArray = new NSArray(Arrays.asList("Mike's Mojo", "Anjo's Mojo"));
         
    public NSArray monthsArray = new NSArray( Arrays.asList(
    		new NSArray(Arrays.asList("Jan","July","Jan","July","Jan")),
    		new NSArray(Arrays.asList(2005, 2006, 2007)) ) );
}