package er.extensions.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.components.ERXArrayChooser;
import er.extensions.eof.ERXEOControlUtilities;

/**
 * Back port from WO 5 WOExtensions. This component is binding compatible, but not source compatible.
 * <p>
 * It can also handle non-relationships, you must set the possibleChoices to an NSArray and
 * relationshipName to a property name. It works whether the object is an EO or not. The name could/should probably
 * change because it handles not only relationships, but it was wrongly named n the first place...
 */
public class WOToManyRelationship extends ERXArrayChooser {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

  protected NSArray _selections;

  public WOToManyRelationship(WOContext aContext) {
    super(aContext);
  }

  @Override
  public void reset() {
    super.reset();
    _selections = null;
  }

  public void updateSourceObject(NSArray newValues) {
    Object realSourceObject = realSourceObject();
    String realRelationshipKey = realRelationshipKey();

    newValues = newValues != null ? newValues : NSArray.EmptyArray;

    if (realSourceObject instanceof EOEnterpriseObject && // only add/remove if we have an EO and handle a relationship
        ((EOEnterpriseObject) realSourceObject).classDescriptionForDestinationKey(realRelationshipKey) != null) {
      EOEnterpriseObject eo = (EOEnterpriseObject) realSourceObject;
      NSArray currentValues = (NSArray) eo.valueForKey(realRelationshipKey);
      currentValues = currentValues != null ? currentValues : NSArray.EmptyArray;

      for (int i = currentValues.count() - 1; i >= 0; i--) {
        EOEnterpriseObject o = (EOEnterpriseObject) currentValues.objectAtIndex(i);
        if (newValues.indexOfIdenticalObject(o) == NSArray.NotFound) { // not found
          eo.removeObjectFromBothSidesOfRelationshipWithKey(o, realRelationshipKey);
        }
      }

      for (int i = newValues.count() - 1; i >= 0; i--) {
        EOEnterpriseObject o = (EOEnterpriseObject) newValues.objectAtIndex(i);
        if (currentValues.indexOfIdenticalObject(o) == NSArray.NotFound) { // not found
          eo.addObjectToBothSidesOfRelationshipWithKey(o, realRelationshipKey);
        }
      }
    } else {
      // NOTE ak: this implementation is different from what JavaWOExtensions do. 
      // There, the existing array is fetched and added to/removed from. Here, we simply set the
      // new array. I changed this because it looked like a bad idea to change the array without
      // the sourceObjects's knowledge.
             NSKeyValueCoding.Utility.takeValueForKey(realSourceObject, 
                    (newValues instanceof NSMutableArray) ? newValues : newValues.mutableClone(), 
                            realRelationshipKey);
    }
  }

  public void setSelections(NSArray selections) {
    // set selections to nil if it's an empty array
    if ((selections == null) || (selections.count() == 0)) {
      // deal with isMandatory
      if (isMandatory() && (theList().count() > 0)) {
        Object anObject = theList().objectAtIndex(0);
        selections = new NSArray(anObject);
      } else {
        selections = null;
      }
    }
    _selections = selections;
    updateSourceObject(selections);
  }

    @Override
    public NSArray currentValues() {
    	NSArray current = selections();
    	return current == null ? NSArray.EmptyArray : current;
    }

    public NSArray selections() {
    	if (_selections == null) {
    		NSArray oldValues = (NSArray) NSKeyValueCodingAdditions.Utility.valueForKeyPath(sourceObject(), relationshipKey());
    		if(oldValues != null) {
    			if(oldValues.lastObject() instanceof EOEnterpriseObject) {
    				oldValues = ERXEOControlUtilities.localInstancesOfObjects(editingContext(), oldValues);
    			}
    		}
    		setSelections(oldValues);
    		// deal with isMandatory
    		if ((_selections == null) && isMandatory()) {
    			if (theList().count() > 0) {
    				Object anObject = theList().objectAtIndex(0);
    				setSelections(new NSArray(anObject));
    			}
    		}
    	}
    	return _selections;
    }

    @Override
    protected boolean isSingleSelection() {
    return false;
  }
}