import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {
  public DirectAction(WORequest aRequest) {
    super(aRequest);
  }

  @Override
public WOActionResults defaultAction() {
    return pageWithName(Main.class.getName());
  }
  
  public WOActionResults exampleReplacementAction() {
	  WOResponse response = WOApplication.application().createResponseInContext(context());
	  response.appendContentString("Example Replacement " + System.currentTimeMillis());
	  return response;
  }
}
