package er.modern.directtoweb.components.embedded;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDCustomEditComponent;
import er.directtoweb.components.relationships.ERDList;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Embeddable EditRelationship component
 * 
 * @d2wKey editRelationshipEmbeddedConfigurationName
 * @d2wKey parentPageConfiguration
 * 
 * @author davidleber
 *
 */
public class ERMDEditRelationship extends ERDCustomEditComponent {
	
    public static interface Keys extends ERDCustomEditComponent.Keys {
        public static final String parentPageConfiguration = "parentPageConfiguration";
    }
    
	private String _uniqueId;
	
    /* logging support */
    static final Logger log = Logger.getLogger(ERDList.class);

	public ERMDEditRelationship(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    
    public NSDictionary<String,String> settings() {
        String pc = d2wContext().dynamicPage();
        if (pc != null) {
        	return new NSDictionary<String,String>(pc,Keys.parentPageConfiguration);
        }
        return null;
    }
    
	// AJAX UTILITIES
	public String uniqueId() {
		if (_uniqueId == null) {
			_uniqueId = ERXStringUtilities.safeIdentifierName(context().contextID());
		}
		return _uniqueId;
	}
	
	public String mainUpdateContainerId() {
		return "RVUC" + uniqueId();
	}
	
	@Override
	public String key() {
//		NSLog.out.appendln("***ERMDEditRelationship.key: " + key() +"***");
		return super.key();
	}

	public String destinationEntityName() {
		EOClassDescription relatedObjectClassDescription = object().classDescriptionForDestinationKey(key());
		String relationshipEntityName = relatedObjectClassDescription.entityName();
//		NSLog.out.appendln("***ERMDEditRelationship.destinationEntityName: "
//				+ relationshipEntityName +"***");
		return relationshipEntityName;
	}
    
}