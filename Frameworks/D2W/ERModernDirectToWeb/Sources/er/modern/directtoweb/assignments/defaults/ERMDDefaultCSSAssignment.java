package er.modern.directtoweb.assignments.defaults;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Assignment class to generate the default CSS class and id values used by the ERModernLook.
 * 
 * @author davidleber
 *
 */
public class ERMDDefaultCSSAssignment extends ERDAssignment {

	private static final long serialVersionUID = 1L;

	/** logging support */
    public final static Logger log = Logger.getLogger(ERMDDefaultCSSAssignment.class);

    /** holds the array of keys this assignment depends upon */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	protected static final NSDictionary keys = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys( new Object [] {
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForAttributeRepetitionWrapper",
			new NSArray(new Object[] {"task", "pageConfiguration"}), "classForAttributeRepetition",
			new NSArray(new Object[] {"propertyKey"}), "classForLabelSpan",
			new NSArray(new Object[] {"propertyKey"}), "classForEmptyLabelSpan",
			new NSArray(new Object[] {"task", "pageConfiguration", "propertyKey"}), "classForAttributeValue",
			new NSArray(new Object[] {"task", "pageConfiguration", "sectionKey"}), "classForSection",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForHeader",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForBanner",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForTopActionBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForBottomActionBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForConfirmBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForErrorBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForTopNavBar",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForTopBatchSize",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForBottomNavBar",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForBottomBatchSize",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration", "wrapperCssClass"}), "classForWrapper",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForInnerWrapper",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForObjectTable",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForObjectTableHeader",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForEmbeddedQueryBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForEmbeddedListBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForEmbeddedEditBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForEmbeddedInspectBlock",
			new NSArray(new Object[] {"task", "parentPageConfiguration", "pageConfiguration"}), "classForEmbeddedCreateBlock",
			new NSArray(new Object[] {"propertyKey"}), "classForAttributeColumn",
			new NSArray(new Object[] {"task", "pageConfiguration"}), "pageType",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration"}), "idForRepetitionContainer",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration", "currentRelationship"}), "idForMainContainer",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration"}), "idForPageTabContainer",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration"}), "idForPageConfiguration",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration", "propertyKey"}), "idForPropertyContainer",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration"}), "idForMainBusyIndicator",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration", "parentPageConfiguration"}), "idForParentPageConfiguration",
			new NSArray(new Object[] {"task", "entity.name", "pageConfiguration", "parentPageConfiguration", "parentRelationship", "masterObjectAndRelationshipKey"}), "idForParentMainContainer"
	});

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This array
     * of keys is used when constructing the
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for.
     * @return array of context keys this assignment depends upon.
     */
    @SuppressWarnings("rawtypes")
	public NSArray dependentKeys(String keyPath) {
        return (NSArray)keys.valueForKey(keyPath);
    }
    

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERMDDefaultCSSAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERMDDefaultCSSAssignment (EOKeyValueUnarchiver u) { 
    	super(u);
    }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERMDDefaultCSSAssignment (String key, Object value) { 
    	super(key,value);
    }
    
	// WRAPPER
	
    /**
     * Obtains a class for the attribute repetition wrapper based on the baseClassForAttributeRepetitionWrapper
     */
	public Object classForAttributeRepetitionWrapper(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForAttributeRepetitionWrapper", null);
	}
	
	// REPETITION
	
	public String classForAttributeRepetition(D2WContext c) {
		String base = (String)c.valueForKey("baseClassForAttributeRepetition");
		return base + " " + pageType(c) + base;
	}
	
	// LABEL
	
	public String classForLabelSpan(D2WContext c) {
		String base = (String)c.valueForKey("baseClassForLabelSpan");
		return base + " " + cssClassForPropertyKey(c.propertyKey()) + base;
	}
	
	public String classForEmptyLabelSpan(D2WContext c) {
		String base = (String)c.valueForKey("baseClassForEmptyLabelSpan");
		return base + " " + cssClassForPropertyKey(c.propertyKey()) + base;
	}
	
	// ATTRIBUTE VALUE
	
	public String classForAttributeValue(D2WContext c) {
		String base = (String)c.valueForKey("baseClassForAttributeValue");
		return base + " " + pageType(c) + base + " " + cssClassForPropertyKey(c.propertyKey()) + base;
	}
	
	// SECTION
	
	public String classForSection(D2WContext c) {
		String base = (String)c.valueForKey("baseClassForSection");
		String result = base + " " + pageType(c)+base;
		String sectionName = (String)c.valueForKey("sectionKey");
		if (sectionName == null || sectionName.length() == 0) {
			sectionName = (String)c.valueForKey("pageConfiguration");
		}
		if (sectionName != null) {
			result = result + " " + ERXStringUtilities.capitalize(ERXStringUtilities.safeIdentifierName(sectionName)) + base;
		}
		return result;
	}
	
	// HEADER

	public String classForHeader(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForHeader", null);
	}
	
	// BANNER
	
	public String classForBanner(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForBanner", null);
	}
	
	// ACTIONS
	
	public String classForTopActionBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForTopActions", null);
	}
	
	public String classForBottomActionBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForBottomActions", null);
	}
	
	// CONFIRM
	
	public String classForConfirmBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForConfirmBlock", null);
	}
	
	// ERRORS
	
	public String classForErrorBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForErrorBlock", null);
	}
	
	// BATCH NAV
	
	public String classForTopNavBar(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForTopBatchNav", "baseClassForBatchNav");
	}
	
	public String classForTopBatchSize(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForTopBatchSize", "baseClassForBatchSize");
	}
	
	public String classForBottomNavBar(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForBottomBatchNav", "baseClassForBatchNav");
	}
	
	public String classForBottomBatchSize(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForBottomBatchSize","baseClassForBatchSize");
	}
	
	// WRAPPER
	
	public String classForWrapper(D2WContext c) {
		String result = cssClassBuilderFromBaseName(c, "baseClassForWrapper", null);
		String customClass = (String)c.valueForKey("wrapperCssClass");
		if (customClass != null) {
			result += " " + customClass; 
		}
		return result;
	}
	
	public String classForInnerWrapper(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForInnerWrapper", null);
	}
	
	// OBJECT TABLE
    public String classForObjectTable(D2WContext c) {
    	return cssClassBuilderFromBaseName(c, "baseClassForObjectTable", null);
    }
    
    public String classForObjectTableHeader(D2WContext c) {
    	return cssClassBuilderFromBaseName(c, "baseClassForObjectTableHeader", null);
    }
    
    // ATTRIBUTE COLUMN
    
	public String classForAttributeColumn(D2WContext c) {
		String base = (String)c.valueForKey("baseClassForAttributeColumn");
		return base + " " + cssClassForPropertyKey(c.propertyKey()) + base;	
	}
	
	// EMBEDDED 
	
	public String classForEmbeddedQueryBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForEmbeddedQueryBlock", null);
	}
	
	public String classForEmbeddedListBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForEmbeddedListBlock", null);
	}
	
	public String classForEmbeddedInspectBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForEmbeddedInspectBlock", null);
	}
	
	public String classForEmbeddedCreateBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForEmbeddedCreateBlock", null);
	}
	
	public String classForEmbeddedEditBlock(D2WContext c) {
		return cssClassBuilderFromBaseName(c, "baseClassForEmbeddedEditBlock", null);
	}
	
	// IDs
	
	public String idForMainContainer(D2WContext c) {
        String idForMainContainer = "MUC_" + idForPageConfiguration(c);
        if (c.valueForKey("currentRelationship") != null) {
            EORelationship relationship = (EORelationship) c
                    .valueForKey("currentRelationship");
            // use currentRelationship key to create unique ID (wonder-140)
            idForMainContainer = idForMainContainer.concat("_" + relationship.name());
        }
        return idForMainContainer;
	}
	
	public String idForRepetitionContainer(D2WContext c) {
		return "RUC_" + idForPageConfiguration(c);
	}
	
	public String idForPageTabContainer(D2WContext c) {
		return "PTUC_" + idForPageConfiguration(c);
	}
	
	public String idForPropertyContainer(D2WContext c) {
		return "PCUC_" + idForPageConfiguration(c) + "_" + cssClassForPropertyKey(c.propertyKey());
	}
	
	public String idForParentMainContainer(D2WContext c) {
        String idForParentMainContainer = "MUC_" + idForParentPageConfiguration(c);
        if (c.valueForKey("parentRelationship") != null) {
            EORelationship parentRelationship = (EORelationship) c
                    .valueForKey("parentRelationship");
            // use parentRelationship key to create unique parent ID
            // (wonder-140)
            idForParentMainContainer = idForParentMainContainer.concat("_"
                    + parentRelationship.name());
        }
        return idForParentMainContainer;
	}
	
	public String idForParentPageConfiguration(D2WContext c) {
		String parentConfig = (String)c.valueForKey("parentPageConfiguration");
		if (parentConfig == null) {
			parentConfig = "NO_PARENT_CONFIGURATION_" + idForPageConfiguration(c);
		}
		return parentConfig;
	}
	
	public String idForPageConfiguration(D2WContext c) {
		String result = c.task() + "_NoEntity";
		if (c.dynamicPage() != null) {
			result = c.dynamicPage();
		} else if (c.entity() != null) {
			result = c.task() + "_" + c.entity().name();
		}
		return result;
	}
	
	public String idForMainBusyIndicator(D2WContext c) {
		return "MBI_" + idForPageConfiguration(c);
	}
	
	// UTILITIES
	
	/**
	 * Builds a css friendly name for the page task.
	 * Depends on pageConfiguration and task
	 */
	public String pageType(D2WContext c) {
		String pageConfig = (String)c.valueForKey("pageConfiguration");
		if (pageConfig == null) 
			pageConfig = "";
		String _type;
		if (pageConfig.contains("ConfirmDelete")) {
			_type = "ConfirmDelete";
		} else if (pageConfig.contains("ConfirmCancel")) {
			_type = "ConfirmCancel";
		} else if (pageConfig.contains("EditWizard")) {
			_type = "EditWizard";
		} else if (pageConfig.contains("InspectWizard")) {
			_type = "InspectWizard";
		} else if (pageConfig.contains("Create")) {
			_type = "Create";
		} else {
			_type = ERXStringUtilities.capitalize(c.task());
		}
		return _type;
	}

	/**
	 * Builds a css class for a page element
	 * Looks up the css base and root using the supplied baseName and rootName from the rules
	 * Depends on pageConfiguration, parentPageConfiguration, and task
	 * @param c
	 * @param baseName
	 * @param rootName
	 */
	public String cssClassBuilderFromBaseName(D2WContext c, String baseName, String rootName) {
		String base = (String)c.valueForKey(baseName);
		String root = null;
		if (rootName != null) {
			root = (String)c.valueForKey(rootName);
		}
    	return cssClassBuilder(c, base, root);
	}
	
	/**
	 * Builds a css class for page elements based on the supplied base and root.
	 * Depends on pageConfiguration, parentPageConfiguration and task
	 * @param c
	 * @param base
	 * @param root
	 */
	public String cssClassBuilder(D2WContext c, String base, String root) {
		String tweakedBase = base + " " + pageType(c)+base;
		String parentPageConfig = (String)c.valueForKey("parentPageConfiguration");
		if (parentPageConfig != null) 
			tweakedBase = base + " " +pageType(c)+base + " Embedded"+base + " Embedded"+pageType(c)+base;
		if (root != null) 
			tweakedBase = root + " " + tweakedBase;
		return pageConfigAppender(c, tweakedBase, base);
	}
	
	/**
	 * Appends the pageConfiguration if it exists to a supplied base and suffix
	 * @param c
	 * @param base
	 * @param suffix
	 */
	public String pageConfigAppender(D2WContext c, String base, String suffix) {
		String result = base;
		String pageConfig = (String)c.valueForKey("pageConfiguration");
		if (pageConfig != null) {
			result = result + " " + pageConfig + suffix;
		}
		return result;
	}
	
	/**
	 * Cleans the propertyKey, removing "." and capitalizing key path components
	 * @param key
	 */
	public String cssClassForPropertyKey(String key) {
		if (key != null) {
			String temp = "";
			if (key.indexOf(".") != -1) {
				NSArray<String> components = NSArray.componentsSeparatedByString(key, ".");
				for (String string : components) {
					string = ERXStringUtilities.capitalize(string);
					temp = temp + string;
				}
			} else {
				temp = ERXStringUtilities.capitalize(key);
			}
			key = temp;
		}
		return key;
	}
	
}
