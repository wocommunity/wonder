package er.snapshotexplorer.components.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;

import er.rest.routes.IERXRouteComponent;
import er.snapshotexplorer.components.SEPage;

public class SEEOModelIndexPage extends SEPage implements IERXRouteComponent {
  public NSArray<EOModel> _models;
  public EOModel _model;

  @SuppressWarnings( { "cast", "unchecked" })
  public SEEOModelIndexPage(WOContext context) {
    super(context);
    _models = (NSArray<EOModel>) EOModelGroup.defaultGroup().models();
  }
}