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
 * @d2wKey id
 * @d2wKey displayNameForDestinationEntity
 */
public class ERDEditableList extends ERDList {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public EODetailDataSource detailDataSource;
	private String closedLabelString;
	private String container;
	
    public ERDEditableList(WOContext context) {
        super(context);
    }
    
    @Override
    public void reset() {
    	detailDataSource = null;
    	closedLabelString = null;
    	container = null;
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
    	if (container == null) container = d2wContext().valueForKey("id") + "_container";
    	return container;
    }
    
    public String closedLabelString() {
    	if (closedLabelString == null) {
    		String localizedEntityName = (String)d2wContext().valueForKey("displayNameForDestinationEntity");
    		closedLabelString = detailDataSource().fetchObjects().count() + " " + localizedEntityName + "s";		// FIXME: RM: perhaps better plurification?
    	} return closedLabelString;
    }
    
    /*
     * forces button on edit pages
     */
    public String submitActionName() {
    	return taskIsEdit() ? "" : null;
    }
}
