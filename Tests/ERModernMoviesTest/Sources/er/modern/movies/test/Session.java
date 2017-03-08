package er.modern.movies.test;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.appserver.ERXSession;
import webobjectsexamples.businesslogic.movies.common.Studio;

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
    
    public NSArray<?> allStudios() {
        EOFetchSpecification fs = new EOFetchSpecification("Studio", null, new NSArray<>(
                new EOSortOrdering(Studio.NameKey, EOSortOrdering.CompareAscending)));
        return defaultEditingContext().objectsWithFetchSpecification(fs);
    }

}
