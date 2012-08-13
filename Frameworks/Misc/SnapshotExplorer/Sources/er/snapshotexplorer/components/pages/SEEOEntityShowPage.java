package er.snapshotexplorer.components.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOEntity;

import er.rest.routes.ERXRouteParameter;
import er.rest.routes.IERXRouteComponent;
import er.snapshotexplorer.components.SEPage;
import er.snapshotexplorer.model.SEEntityStats;
import er.snapshotexplorer.model.SEModelStats;

public class SEEOEntityShowPage extends SEPage implements IERXRouteComponent {
  private EOEntity _entity;

  public SEEntityStats _entityStats;

  public SEEOEntityShowPage(WOContext context) {
    super(context);
  }

  @ERXRouteParameter
  public void setEOEntity(EOEntity entity) {
    _entity = entity;
    _entityStats = new SEModelStats(entity.model()).entityStatsForEntityNamed(entity.name());
  }

  @ERXRouteParameter
  public void setEoEntity(EOEntity entity) {
	  setEOEntity(entity);
  }

  public EOEntity eoentity() {
    return _entity;
  }
}