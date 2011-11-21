package er.distribution;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eodistribution.EODistributionContext;

public class JavaClient extends WOComponent {

	public EODistributionContext distributionContext;
	
    public JavaClient(WOContext context) {
        super(context);
        distributionContext = new ERDistributionContext(session());
        distributionContext.setDelegate(session());
    }
    
}
