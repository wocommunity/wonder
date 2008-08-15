package er.iui.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponent;

public class ERIUIContainer extends ERXComponent {
  public boolean _renderContainer;
  private int _requestsSinceContainerRendered;

  public ERIUIContainer(WOContext context) {
    super(context);
    _renderContainer = !AjaxUtils.isAjaxRequest(context.request());
    if (!_renderContainer) {
      _requestsSinceContainerRendered = 2;
    }
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public void checkRenderContainer(boolean appendToResponse) {
    if (AjaxUtils.isAjaxRequest(context().request())) {
      if (_renderContainer && appendToResponse && _requestsSinceContainerRendered > 0) {
        _renderContainer = false;
      }
    }
    else {
      _renderContainer = true;
      _requestsSinceContainerRendered = 0;
    }
  }

  @Override
  public void takeValuesFromRequest(WORequest request, WOContext context) {
    checkRenderContainer(false);
    super.takeValuesFromRequest(request, context);
  }

  @Override
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    checkRenderContainer(false);
    return super.invokeAction(request, context);
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    checkRenderContainer(true);
    super.appendToResponse(response, context);
    _requestsSinceContainerRendered++;
  }
}