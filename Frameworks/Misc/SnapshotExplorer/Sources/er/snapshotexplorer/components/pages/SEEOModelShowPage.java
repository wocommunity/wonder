package er.snapshotexplorer.components.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.foundation.NSArray;

import er.rest.routes.ERXRouteParameter;
import er.rest.routes.IERXRouteComponent;
import er.snapshotexplorer.components.SEPage;
import er.snapshotexplorer.model.SEEntityStats;
import er.snapshotexplorer.model.SEModelStats;

public class SEEOModelShowPage extends SEPage implements IERXRouteComponent {
  private EOModel _model;

  public SEModelStats _modelStats;
  public SEEntityStats _entityStat;

  public SEEOModelShowPage(WOContext context) {
    super(context);
  }

  @ERXRouteParameter
  public void setEOModel(EOModel model) {
    _model = model;
    _modelStats = new SEModelStats(_model);
  }

  public EOModel eomodel() {
    return _model;
  }

  public NSArray<SEEntityStats> entitiesStats() {
    return _modelStats.entityStats();
  }
}