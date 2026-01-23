package er.directtoweb.components.relationships;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;

import er.directtoweb.components.ERDCustomQueryComponent;
import er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier;

/**
 * Allows you to query for objects that have a one or more of a set of related objects.
 * Example: given Child-&gt;School, you could select a few schools and find all the children that are in those schools.
 *
 * @binding displayGroup displayGroup to set the queryMatch in (queryOperator = isContainedInArray)
 * @binding key relationship key ("school")
 * @binding destinationEntityName name of the destination entity ("School")
 * @binding restrictedChoiceKey keypath returning an array of preselections (session.user.district.schools)
 * @binding restrictingFetchSpecification name of a fetchspec (elementarySchoools)
 * @binding keyWhenRelationship display key for destination ("name"-&gt;school.name)
 * @binding sortOrderAttributeName sort key for destination ("name"-&gt;school.name)
 * @binding toManyUIStyle "browser" or "checkbox"
 * @binding numCols number of columns when "checkbox" is the UIStyle
 * @binding size number of rows for "checkbox" or "browser" UIStyle
 * 
 * @author ak on Wed Apr 07 2004
 */
public class ERDQueryIsContainedInArray extends ERDCustomQueryComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = LoggerFactory.getLogger(ERDQueryIsContainedInArray.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryIsContainedInArray(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    
    @Override
    public void awake() {
        super.awake();
        displayGroup().queryOperator().setObjectForKey(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName, key());
    }
    
    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)valueForBinding("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)valueForBinding("restrictingFetchSpecification");
        if(fetchSpecName != null &&  fetchSpecName.length()>0) {
            EOEditingContext ec = session().defaultEditingContext();
            return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, (String)valueForBinding("destinationEntityName"),fetchSpecName,null);
        }
        return null;
    }
}
