package er.rest.gianduia;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.rest.ERXRestRequestNode;
import er.rest.routes.ERXRoute;
import er.rest.routes.ERXRouteController;
import er.rest.routes.ERXRouteRequestHandler;
import er.rest.routes.ERXRouteResults;

public class ERXGianduiaController extends ERXRouteController {
	public ERXGianduiaController(WORequest request) {
		super(request);
	}

	public static void addRoutes(String entityName, ERXRouteRequestHandler requestHandler) {
		requestHandler.addRoute(new ERXRoute("/gianduia/fetch", ERXRoute.Method.Post, ERXGianduiaController.class, "fetch"));
	}

	@SuppressWarnings("unchecked")
	public WOActionResults fetchAction() throws Exception {
		ERXRestRequestNode node = requestNode();
		String entityName = (String) node.childNamed("entity").value();
		ERXRouteController controller = controller(entityName);

		NSMutableDictionary<String, String> options = new NSMutableDictionary<String, String>();
		ERXRestRequestNode prefetchingKeyPathsNode = node.childNamed("relationshipKeyPathsForPrefetching");
		if (prefetchingKeyPathsNode != null) {
			NSMutableArray<String> prefetchingKeyPaths = new NSMutableArray<String>();
			for (ERXRestRequestNode prefetchingKeyPathNode : prefetchingKeyPathsNode.children()) {
				String prefetchingKeyPath = (String) prefetchingKeyPathNode.value();
				if (prefetchingKeyPath != null) {
					prefetchingKeyPaths.addObject(prefetchingKeyPath);
				}
			}
			if (prefetchingKeyPaths.count() > 0) {
				options.setObjectForKey(prefetchingKeyPaths.componentsJoinedByString(","), "prefetchingKeyPaths");
			}
		}
		controller.setOptions(options);

		WOActionResults fetchResults = null;
		ERXRestRequestNode predicateNode = node.childNamed("predicate");
		if (predicateNode == null) {
			WOActionResults actionResults = controller.performActionNamed("index");
			if (actionResults instanceof ERXRouteResults) {
				ERXRestRequestNode responseNode = ((ERXRouteResults) actionResults).responseNode();
				responseNode.setID(node.id());
				responseNode.setArray(true);
				fetchResults = actionResults;
			}
		}
		else {
			ERXRestRequestNode fetchResultsNode = new ERXRestRequestNode(null, true);
			fetchResultsNode.setID(node.id());
			fetchResultsNode.setArray(true);
			if ("OR".equals(predicateNode.valueForKey("compoundPredicateType"))) {
				ERXRestRequestNode subpredicatesNode = predicateNode.childNamed("subpredicates");
				for (ERXRestRequestNode subpredicateNode : subpredicatesNode.children()) {
					if ("NSComparisonPredicate".equals(subpredicateNode.valueForKey("predicateType")) && "NSKeyPathExpressionType".equals(subpredicateNode.valueForKeyPath("leftExpression.expressionType")) && "objectID.URIRepresentation".equals(subpredicateNode.valueForKeyPath("leftExpression.keyPath"))) {
						if ("NSConstantValueExpressionType".equals(subpredicateNode.valueForKeyPath("rightExpression.expressionType"))) {
							String moGID = (String) subpredicateNode.valueForKeyPath("rightExpression.constantValue");
							int lastSlashIndex = moGID.lastIndexOf('/');
							String moPK = moGID.substring(lastSlashIndex + 2); // + 2 because the PK will be "p10000"
																				// and we want "10000"
							StringBuffer path = new StringBuffer();
							path.append("/");
							path.append(requestHandler().controllerPathForEntityNamed(entityName));
							path.append("/");
							path.append(moPK);
							path.append(".gndp");
							ERXRoute matchingRoute = requestHandler().setupRequestWithRouteForMethodAndPath(request(), "GET", path.toString());
							requestHandler().setupRouteControllerFromUserInfo(controller, request().userInfo());

							NSDictionary<ERXRoute.Key, String> keys = (NSDictionary<ERXRoute.Key, String>) request().userInfo().objectForKey(ERXRouteRequestHandler.KeysKey);
							WOActionResults actionResults = controller.performActionNamed(keys.objectForKey(ERXRoute.ActionKey));
							if (actionResults instanceof ERXRouteResults) {
								ERXRestRequestNode responseNode = ((ERXRouteResults) actionResults).responseNode();
								fetchResultsNode.addChild(responseNode);
							}
							else {
								// ???
							}
						}
					}
				}
			}
			fetchResults = new ERXRouteResults(context(), format(), fetchResultsNode);
		}

		return fetchResults;
	}
}
