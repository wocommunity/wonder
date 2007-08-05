package er.attachment;

import com.webobjects.appserver.WOApplication;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

/**
 * ERAttachment Framework Principal ('ERAttachment' name is the name of the model object)
 * 
 * @author mschrag
 */
public class ERAttachmentPrincipal extends ERXFrameworkPrincipal {
  @SuppressWarnings("unchecked")
  public final static Class[] REQUIRES = new Class[] { ERXExtensions.class };

  static {
    setUpFrameworkPrincipalClass(ERAttachmentPrincipal.class);
  }

  @Override
  public void finishInitialization() {
    WOApplication.application().registerRequestHandler(new ERAttachmentRequestHandler(), ERAttachmentRequestHandler.REQUEST_HANDLER_KEY);
  }
}
