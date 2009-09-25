package er.snapshotexplorer.components.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOModelGroup;

import er.rest.routes.ERXRouteParameter;
import er.rest.routes.IERXRouteComponent;
import er.snapshotexplorer.components.SEPage;
import er.snapshotexplorer.model.SEModelGroupStats;
import er.snapshotexplorer.model.SEModelStats;

public class SEEOModelGroupShowPage extends SEPage implements IERXRouteComponent {
  private EOModelGroup _modelGroup;

  public SEModelGroupStats _modelGroupStats;
  public SEModelStats _modelStats;

  public SEEOModelGroupShowPage(WOContext context) {
    super(context);
  }

  @ERXRouteParameter
  public void setEOModelGroup(EOModelGroup modelGroup) {
    _modelGroup = modelGroup;
    _modelGroupStats = new SEModelGroupStats(modelGroup);
  }

  public EOModelGroup eomodelGroup() {
    return _modelGroup;
  }
}