package er.pdfexamples.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSTimestamp;


/**
 * Subclassing one of the other components here is just lazy, but 
 * I didn't want to copy the getXsl() method to read the property 
 * for the transform path... what can I say, it's late.
 * @author lmg42
 *
 */
public class SimpleXML2FOP2PDF1 extends SimpleHTML2FOP2PDF {
    public SimpleXML2FOP2PDF1(WOContext context) {
        super(context);
    }

	public NSTimestamp now() {
		return new NSTimestamp();
	}
}