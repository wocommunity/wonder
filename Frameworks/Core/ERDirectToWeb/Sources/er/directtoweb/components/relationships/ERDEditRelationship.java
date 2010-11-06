package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.directtoweb.components.ERDCustomEditComponent;

public class ERDEditRelationship extends ERDCustomEditComponent {
	public static final String parentPageConfiguration = "parentPageConfiguration";
	
	public ERDEditRelationship(WOContext context) {
		super(context);
	}
	
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
	
	public String destinationEntityName() {
		EOClassDescription destinationClassDescription = object().classDescriptionForDestinationKey(key());
		String destinationEntityName = destinationClassDescription.entityName();
		return destinationEntityName;
	}

	public NSDictionary<String, Object> settings() {
		NSMutableDictionary<String, Object> settings = new NSMutableDictionary<String, Object>();
		String pageConfiguration = d2wContext().dynamicPage();
		EORelationship smartRelationship = (EORelationship)d2wContext().valueForKey("smartRelationship");
		if(pageConfiguration != null) {
			settings.setObjectForKey(pageConfiguration, "parentPageConfiguration");
		}
		if(smartRelationship != null) {
			settings.setObjectForKey(smartRelationship, "parentRelationship");
		}
		return settings.isEmpty()?null:settings;
	}
}