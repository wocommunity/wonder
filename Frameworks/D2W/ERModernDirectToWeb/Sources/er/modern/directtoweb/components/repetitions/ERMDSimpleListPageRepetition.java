package er.modern.directtoweb.components.repetitions;

import com.webobjects.appserver.WOContext;

/**
 * Modernized table based list page repetition. Suitable for most needs.Eliminiates much of the
 * additions from ERMDListPageRepetition.
 * 
 * @binding displayGroup
 * @binding d2wContext
 * 
 * @d2wKey componentName
 * @d2wKey object
 * @d2wKey displayNameForProperty
 * @d2wKey sortKeyForList
 * @d2wKey sortCaseInsensitive
 * @d2wKey propertyIsSortable 
 * @d2wKey pageConfiguration
 * @d2wKey parentPageConfiguration
 * @d2wKey classForAttributeColumn
 * @d2wKey classForObjectTable
 * @d2wKey tableHeaderComponentName
 * @d2wKey classForObjectTableHeader
 * @d2wKey updateContainerID
 * @d2wKey baseClassForObjectRow
 * 
 * @author davidleber
 */
public class ERMDSimpleListPageRepetition extends ERMDListPageRepetition {
	
    public ERMDSimpleListPageRepetition(WOContext context) {
        super(context);
    }
    
}