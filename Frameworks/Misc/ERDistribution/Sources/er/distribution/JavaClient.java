package er.distribution;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eodistribution.EODistributionContext;

public class JavaClient extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public EODistributionContext distributionContext;
	
    public JavaClient(WOContext context) {
        super(context);
        distributionContext = new ERDistributionContext(session());
        distributionContext.setDelegate(session());
    }
    
}
