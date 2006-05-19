package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

public class AjaxSubmitButton extends AjaxDynamicElement {

  public AjaxSubmitButton(String _name, NSDictionary _associations, WOElement _children) {
    super(_name, _associations, _children);
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    //    function addComment(e) { 
    //      // submit the form using Ajax 
    //      new Ajax.Request("comment.php", { 
    //        parameters : Form.serialize(this), 
    //        onSuccess : updateComment 
    //      }); 
    //      Event.stop(e); 
    //     } 
    super.appendToResponse(_response, _context);
  }

  protected void addRequiredWebResources(WOResponse _response, WOContext _context) {
  }

  protected WOActionResults handleRequest(WORequest _request, WOContext _context) {
    return null;
  }

}
