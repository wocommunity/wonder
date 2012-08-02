package er.erxtest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WODirectAction;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXProperties;

public class DirectAction extends WODirectAction {
  private static NSMutableArray<String> _sessionIDs = new NSMutableArray<String>();

  public DirectAction(WORequest aRequest) {
    super(aRequest);
  }

  @Override
  public WOActionResults defaultAction() {
    return pageWithName(Main.class.getName());
  }

  protected String urlForDirectActionNamed(String directActionName, String sessionID, NSDictionary parameters) {
    context().generateCompleteURLs();
    String url = context().directActionURLForActionNamed(directActionName, parameters);
    context().generateRelativeURLs();
    String sessionIdKey = WOApplication.application().sessionIdKey();
    if (sessionID != null) {
      int sessionIdIndex = url.indexOf(sessionIdKey + "=");
      if (sessionIdIndex == -1) {
        if (url.indexOf("?") == -1) {
          url += "?";
        }
        else {
          url += "&";
        }
        url += sessionIdKey + "=" + sessionID;
      }
      else {
        url = url.replace(sessionIdKey + "=[^&]+", sessionIdKey + "=" + sessionID);
      }
    }
    return url;
  }

  public WOActionResults testInitializeAction() throws MalformedURLException, IOException {
    _sessionIDs.removeAllObjects();
    int sessionCount = ERXProperties.intForKey("er.extensions.ERXObjectStoreCoordinatorPool.maxCoordinators");
    String createSessionUrl = urlForDirectActionNamed("testCreateSession", null, null);
    for (int sessionNum = 0; sessionNum < sessionCount; sessionNum++) {
      InputStream is = new URL(createSessionUrl).openStream();
      while (is.available() > 0) {
        is.read();
      }
    }
    System.out.println("DirectAction.testInitializeAction: " + _sessionIDs);
    return pageWithName("Main");
  }

  public WOActionResults testCreateSessionAction() {
    Session session = (Session) context().session();
    _sessionIDs.addObject(session.sessionID());
    return pageWithName("Main");
  }
}