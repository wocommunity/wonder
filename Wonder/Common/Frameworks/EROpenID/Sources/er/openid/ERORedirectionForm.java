package er.openid;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

/**
 * ERORedirectionForm provides a simple implementaton of an OpenID 
 * redirection form.
 * 
 * @binding redirectionUrl the url to submit the form to
 * @binding formName the name of the form
 * 
 * @author mschrag
 */
public class ERORedirectionForm extends WOComponent {
  public String _repetitionFormKey;

  public ERORedirectionForm(WOContext context) {
    super(context);
  }

  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public NSArray formKeys() {
    return context().request().formValueKeys();
  }

  public String formValue() {
    return context().request().stringFormValueForKey(_repetitionFormKey);
  }
}