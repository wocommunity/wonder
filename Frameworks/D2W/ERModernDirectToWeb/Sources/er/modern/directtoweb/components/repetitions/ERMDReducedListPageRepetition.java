package er.modern.directtoweb.components.repetitions;

import com.webobjects.appserver.WOContext;

/**
 * Non table based (UL) list page repetition. Displays without labels. Suitable for reduced
 * list display.
 *
 * @d2wKey componentName
 * @d2wKey object
 * @d2wKey pageConfiguration
 * @d2wKey parentPageConfiguration
 * @d2wKey extraListComponentName
 * @d2wKey justification
 * @d2wKey displayNameForProperty
 * @d2wKey sortKeyForList
 * @d2wKey sortCaseInsensitive
 * @d2wKey propertyIsSortable 
 * @d2wKey classForAttributeColumn
 * @d2wKey baseClassForObjectRow
 * 
 * @author davidleber
 */
public class ERMDReducedListPageRepetition extends ERMDListPageRepetition {
	
    public ERMDReducedListPageRepetition(WOContext context) {
        super(context);
    }
}