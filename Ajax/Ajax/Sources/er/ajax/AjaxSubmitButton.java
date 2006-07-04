package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class AjaxSubmitButton extends AjaxDynamicElement {

  public AjaxSubmitButton(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    //    function addComment(e) { 
    //      // submit the form using Ajax 
    //      new Ajax.Request("comment.php", { 
    //        parameters : Form.serialize(this), 
    //        onSuccess : updateComment 
    //      }); 
    //      Event.stop(e); 
    //     } 
    super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse response, WOContext context) {
  }

  protected WOActionResults handleRequest(WORequest request, WOContext context) {
    return null;
  }

}
