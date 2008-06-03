package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Action button that lets you pick the setup of a non-flattened to-many relationship and creates 
 * the intermediate objects, like when you have an invoice with line items and a relationship to articles.
 *
 * @binding pickRelationshipName name of the relationship keypath (eg: items.article)
 * @binding pickConfigurationName name of the page configuration to use (optional)
 * @binding pickButtonLabel label for the button (optional)
 *
 * @created ak on 07.11.05
 * @project ERDirectToWeb
 */

public class ERDPickIntermediateButton extends ERDActionButton {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDPickIntermediateButton.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDPickIntermediateButton(WOContext context) {
        super(context);
    }

    public String pickButtonLabel() {
    	String pickButtonLabel = (String) valueForBinding("pickButtonLabel"); 
    	if(pickButtonLabel == null) {
    		pickButtonLabel = "Select";
    	}
    	return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(pickButtonLabel);
    }
    
    public WOComponent pickAction() {
    	WOComponent nextPage = context().page();
    	String keyPath = (String) valueForBinding("pickRelationshipName");
    	String relationshipName = ERXStringUtilities.keyPathWithoutLastProperty(keyPath);
    	String pickRelationshipName = ERXStringUtilities.lastPropertyKeyInKeyPath(keyPath);
    	EOEntity entity = ERXEOAccessUtilities.entityForEo(object());
    	EOEntity pickEntity = ERXEOAccessUtilities.destinationEntityForKeyPath(entity, keyPath);
    	String pickConfigurationName = (String) valueForBinding("pickConfigurationName");
    	if(pickConfigurationName == null) {
    		pickConfigurationName = "Pick" + pickEntity.name();
    	}
    	ERDPickPageInterface epi= (ERDPickPageInterface)D2W.factory().pageForConfigurationNamed(pickConfigurationName, session());
    	epi.setDataSource(new EODatabaseDataSource(object().editingContext(), pickEntity.name()));
    	epi.setSelectedObjects(((NSArray)object().valueForKeyPath(keyPath + ".@unique")).mutableClone());
    	epi.setNextPageDelegate( new ERDPickIntermediateDelegate(object(), relationshipName, pickRelationshipName, context().page()));
    	
    	nextPage = (WOComponent)epi;
    	return nextPage;
    }
}
