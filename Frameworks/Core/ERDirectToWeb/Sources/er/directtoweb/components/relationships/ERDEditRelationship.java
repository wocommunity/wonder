package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSDictionary;

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
		String pageConfiguration = d2wContext().dynamicPage();
		if(pageConfiguration != null) {
			return new NSDictionary<String, Object>(pageConfiguration, "parentPageConfiguration");
		}
		return null;
	}
}