package er.snapshotexplorer;

import er.rest.ERXRestNameRegistry;
import er.rest.routes.ERXRouteRequestHandler;
import er.snapshotexplorer.controllers.SEEntityController;
import er.snapshotexplorer.controllers.SEModelController;
import er.snapshotexplorer.controllers.SEModelGroupController;

public class SESnapshotExplorer {
  public static void register() {
    ERXRouteRequestHandler routeRequestHandler = new ERXRouteRequestHandler();
    SESnapshotExplorer.register(routeRequestHandler);
    ERXRouteRequestHandler.register(routeRequestHandler);
  }
  
  public static void register(ERXRouteRequestHandler requestHandler) {
    ERXRestNameRegistry.registry().setExternalNameForInternalName("Model", "EOModel");
    ERXRestNameRegistry.registry().setExternalNameForInternalName("Entity", "EOEntity");
    ERXRestNameRegistry.registry().setExternalNameForInternalName("ModelGroup", "EOModelGroup");
    requestHandler.addDefaultRoutes("EOModelGroup", false, SEModelGroupController.class);
    requestHandler.addDefaultRoutes("EOModel", false, SEModelController.class);
    requestHandler.addDefaultRoutes("EOEntity", false, SEEntityController.class);
  }
}
