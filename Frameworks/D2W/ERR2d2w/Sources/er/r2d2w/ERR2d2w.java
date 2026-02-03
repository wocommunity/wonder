package er.r2d2w;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.ERDirectToWeb;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXRetainer;
import er.r2d2w.delegates.PreferenceHandlerDelegate;

public class ERR2d2w extends ERXFrameworkPrincipal {

	public static final String DERIVED_COUNT = "DerivedCount";
    public static final Logger log = LoggerFactory.getLogger(ERR2d2w.class);
    public final static Class<?>[] REQUIRES = new Class[] {ERXExtensions.class, ERDirectToWeb.class};

    protected static volatile ERR2d2w sharedInstance;

    // Registers the class as the framework principal
    static {
    	log.debug("Static Initializer for ERR2d2w");
    	setUpFrameworkPrincipalClass (ERR2d2w.class);
    }

    public static ERR2d2w sharedInstance() {
        if (sharedInstance == null) {
        	synchronized (ERR2d2w.class) {
        		if(sharedInstance == null) {
        			sharedInstance = sharedInstance(ERR2d2w.class);
        		}
        	}
        }
        return sharedInstance;
    }
    
	@Override
    public void finishInitialization() {
        // Initialized shared data
		if(createsDerivedCounts()) {
			NSNotificationCenter.defaultCenter().addObserver(this,
	    	        new NSSelector<Void>("applicationDidFinishLaunching",
	    	                ERXConstant.NotificationClassArray),
	    	                WOApplication.ApplicationDidFinishLaunchingNotification,
	    					null);
		}
    }

    /**
     * Registers notification handlers for user preference notifications.
     */
    private static void registerHandlers() {
        log.debug("Registering preference handlers");
        Object handler = null;
        String className = ERXProperties.stringForKey("er.corebusinesslogic.ERCoreUserPreferences.handlerClassName");
    	if(className != null) {
    		try {
    			handler = Class.forName(className).newInstance();
    		} catch (Exception e) {
    			throw NSForwardException._runtimeExceptionForThrowable(e);
    		}
    	}
        if(handler == null) {
        	handler = new PreferenceHandlerDelegate();
        }
        ERXRetainer.retain(handler);        
    }

    /**
     * Checks the system property <code>er.directtoweb.ERDirectToWeb.createsDerivedCounts</code>.
     */
    public static boolean createsDerivedCounts() {
        return ERXProperties.booleanForKeyWithDefault("er.r2d2w.ERR2d2w.createsDerivedCounts", false);
    }

    public static NSArray<String> modelsToIgnore() {
    	return ERXProperties.arrayForKeyWithDefault("er.r2d2w.ERR2d2w.ignoreModels", NSArray.EmptyArray);
    }
    
    public static void applicationDidFinishLaunching(NSNotification n) {
    	registerHandlers();
    	
    	NSArray<EOModel> models = EOModelGroup.defaultGroup().models();
    	for(EOModel model: models) {
    		if(modelsToIgnore().contains(model.name())) {
    			continue;
    		}
	    	
	    	if(createsDerivedCounts()) {
	    		createDerivedCountAttributes(model);
	    	}
    	}
    }
    
    //TODO: this is a MySQL only hack right now.  Hoping to make this
    //more general purpose later.
	public static void createDerivedCountAttributes(EOModel model) {
		log.debug("Model added: Begin adding derived counts");
		
		// Get all to-many relationships
		for(EOEntity entity: model.entities()) {
			
			//Compound keys aren't supported
			if(!entity.hasSimplePrimaryKey()) {continue;}

			EOEntityClassDescription ecd = new EOEntityClassDescription(entity);
			
			//Create derived attribute to fetch relationship counts
			for(String relationshipName : ecd.toManyRelationshipKeys()) {
				EORelationship rel = entity.relationshipNamed(relationshipName);
				
				// Entities must share the same model (Assuming different model means different DB)
				if(!rel.destinationEntity().model().equals(entity.model())){continue;}
				
				//CHECKME can I do this with flattened many-to-manys??
				// Relationship cannot be flattened
				if(rel.isFlattened()) {continue;}
				
				// Finally, build derived attribute
				EOAttribute att = new EOAttribute();
				
				// Set att name
				att.setName(rel.name() + DERIVED_COUNT);
				log.debug("Building derived attribute for entity named " + entity.name() + ": " + att.name());
				
				att.setValueType(Character.toString(EOAttribute._VTInteger));
				att.setClassName(Number.class.getName());
				
				// Other important settings
				att.setAllowsNull(true);
				att.setReadOnly(true);
				
				// Add to entity BEFORE setting definition
				entity.addAttribute(att);
				
				// Create and set definition
				StringBuilder attDef = new StringBuilder();
				attDef.append("(SELECT COUNT(*) FROM ");
				attDef.append(rel.destinationEntity().externalName());
				attDef.append(" WHERE ");
				attDef.append(rel.destinationEntity().externalName());
				attDef.append(".");
				attDef.append(rel.destinationAttributes().objectAtIndex(0).columnName());
				attDef.append(" = ");
				attDef.append(entity.primaryKeyAttributes().objectAtIndex(0).columnName());
				attDef.append(")");
				att.setDefinition(attDef.toString());
			}
		}
	}

}
