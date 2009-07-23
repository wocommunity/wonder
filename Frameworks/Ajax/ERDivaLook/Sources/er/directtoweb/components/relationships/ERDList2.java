package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Same as ERDList except it uses a detail datasource so that you may edit the list as well.
 * 
 * @see ERDList
 * 
 * @author mendis
 * @author dschonen			Added collapsing
 *
 */
public class ERDList2 extends ERDList {
	public EODetailDataSource detailDataSource;
	
    public ERDList2(WOContext context) {
        super(context);
    }
    
    public void reset() {
    	detailDataSource = null;
    	super.reset();
    }
    
    // accessors
    public EODetailDataSource detailDataSource() {
    	if (detailDataSource == null) {
    		EOEnterpriseObject object = (EOEnterpriseObject) valueForBinding("object");
    		detailDataSource = new EODetailDataSource(object.classDescription(), key());
    		detailDataSource.qualifyWithRelationshipKey(key(), object);
    		detailDataSource.fetchObjects();
    	} return detailDataSource;
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public NSDictionary settings() {
    	NSMutableDictionary settings = super.settings().mutableClone();
    	settings.setObjectForKey(object(), "object");
    	return settings.immutableClone();
    }
    
    public String container() {
    	return d2wContext().valueForKey("id") + "_container";
    }
    
    public String closedLabelString() {
        String localizedEntityName = (String)d2wContext().valueForKey("displayNameForDestinationEntity");
    	return detailDataSource().fetchObjects().count() + " " + localizedEntityName + "s";		// FIXME: RM: perhaps better plurification?
    }
}
