package er.directtoweb.pages;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.appserver.ERXApplication;


/**
 * Creates a GrahpViz page for those that needs such trivial tools.
 * @author ak
 *
 */
public class ERD2WGraphVizPage extends ERD2WPage {
    
    private int _nodeID = 0;

    public ERD2WGraphVizPage(WOContext context) {
        super(context);
    }
    
    public int nodeID() {
        return _nodeID ++;
    }
    
    public NSArray<String> attributes() {
        NSMutableArray<String> result = new NSMutableArray<String>();
        EOEntity entity = entity();
        for (String key : (NSArray<String>)displayPropertyKeys()) {
            if(entity.classDescriptionForInstances().attributeKeys().containsObject(key) && entity.classPropertyNames().containsObject(key)) {
                result.addObject(key);
            }
        }
        return result;
    }
    
    public NSArray<String> toOneRelationships() {
        NSMutableArray<String> result = new NSMutableArray<String>();
        EOEntity entity = entity();
        for (String key : (NSArray<String>)displayPropertyKeys()) {
            if(entity.classDescriptionForInstances().toOneRelationshipKeys().containsObject(key) && entity.classPropertyNames().containsObject(key)) {
                result.addObject(key);
            }
        }
        return result;
    }
    
    public String nodeType() {
        return "->";
    }
    
    public NSArray<String> toManyRelationships() {
        NSMutableArray<String> result = new NSMutableArray<String>();
        EOEntity entity = entity();
        for (String key : (NSArray<String>)displayPropertyKeys()) {
            if(entity.classDescriptionForInstances().toManyRelationshipKeys().containsObject(key) && entity.classPropertyNames().containsObject(key)) {
                result.addObject(key);
            }
        }
        return result;
    }
    
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        // we do NOT want to expose our model to just everyone...
        if (ERXApplication.isDevelopmentModeSafe()) {
            super.appendToResponse(response, context);
        }
        response.setHeader("text/plain", "content-type");
    }
}