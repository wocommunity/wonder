package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDCustomEditComponent;

public class ERDEditRelationship extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String parentPageConfiguration = "parentPageConfiguration";
	
	public ERDEditRelationship(WOContext context) {
		super(context);
	}
	
	@Override
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
			return new NSDictionary<>(pageConfiguration, parentPageConfiguration);
		}
		return null;
	}
}