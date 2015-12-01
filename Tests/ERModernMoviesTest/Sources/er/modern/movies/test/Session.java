package er.modern.movies.test;

import com.webobjects.eocontrol.EOEnterpriseObject;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.appserver.ERXSession;

public class Session extends ERXSession {
    private static final long serialVersionUID = 1L;

    private MoviesNavigationController _navController;

    public Session() {
    }

    @Override
    public void awake() {
        super.awake();
        ERCoreBusinessLogic.setActor((EOEnterpriseObject) objectStore().valueForKey(
                "user"));
    }

    @Override
    public void sleep() {
        ERCoreBusinessLogic.setActor(null);
        super.sleep();
    }

    public MoviesNavigationController navController() {
        if (_navController == null) {
            _navController = new MoviesNavigationController(this);
        }
        return _navController;
    }
}
