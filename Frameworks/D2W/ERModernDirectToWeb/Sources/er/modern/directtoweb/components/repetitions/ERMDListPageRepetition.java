package er.modern.directtoweb.components.repetitions;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.repetitions.ERDListPageRepetition;

/**
 * Table based list repetition. I've not looked at this very closely. I currently use
 * {@link er.modern.directtoweb.components.repetitions.ERMDSimpleListPageRepetition} this is included for backwards compatibility.
 *
 * @binding displayGroup
 * @binding d2wContext
 * 
 * @d2wKey componentName
 * @d2wKey object
 * @d2wKey extraListComponentName
 * @d2wKey justification
 * @d2wKey displayNameForProperty
 * @d2wKey sortKeyForList
 * @d2wKey sortCaseInsensitive
 * @d2wKey propertyIsSortable 
 * @d2wKey baseClassForObjectRow
 * 
 * @author davidleber
 */
public class ERMDListPageRepetition extends ERDListPageRepetition {
	
    public ERMDListPageRepetition(WOContext context) {
        super(context);
    }
    
    /**
     * CSS class for the current table row in the repetition.
     * <p>
     * Examples:
     * <p>
     * "ObjRow OddObjRow FirstObjRow ListMovieObjRow"
     * "ObjRow EvenObjRow ListMovieObjRow"
     * "ObjRow OddObjRow  ListMovieObjRow"
     * "ObjRow EvenObjRow LastObjRow ListMovieObjRow"
     * 
     * @return String css class derived from rules and position
     */
	public String objectRowClass() {
		String objRowBase = (String)d2wContext().valueForKey("baseClassForObjectRow");
		String evenessAndPosition = "Even" + objRowBase;
		int lastIndex = displayGroup().displayedObjects().count() - 1;
		if (rowIndex % 2 == 0) {
			evenessAndPosition = "Odd" + objRowBase;
		}
		if (rowIndex == 0) {
			evenessAndPosition += " First" + objRowBase;
		} else if (rowIndex == lastIndex) {
			evenessAndPosition += " Last" + objRowBase;
		}
		String result = objRowBase + " " + evenessAndPosition;
		String pageConfig = (String)d2wContext().valueForKey("pageConfiguration");
		if (pageConfig != null) {
			result = result + " " + pageConfig + objRowBase;
		}
		return result;
	}
	
	// UTILITIES
	
	public boolean  hasLeftActions() {
		return leftActions() != null && leftActions().count() > 0;
	}
	
	public boolean hasRightActions() {
		return rightActions() != null && rightActions().count() > 0;
	}
	
}