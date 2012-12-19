package er.modern.directtoweb.components.repetitions;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.repetitions.ERDQueryPageRepetition;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Modern QueryPage repetition.
 * 
 * @d2wKey componentName
 * @d2wKey hidePropertyName
 * @d2wKey propertyNameComponentName
 * @d2wKey sectionComponentName
 * @d2wKey classForLabelSpan
 * @d2wKey classForAttributeValue
 * @d2wKey classForSection
 * @d2wKey classForEmptyLabelSpan
 * @d2wKey classForAttributeRepetitionWrapper
 * 
 * @author davidleber
 */
public class ERMDQueryPageRepetition extends ERDQueryPageRepetition {
	
	public int index;
	
    public ERMDQueryPageRepetition(WOContext context) {
        super(context);
    }
    
    @Override
    public void reset() {
    	super.reset();
    }
    
    /**
     * CSS class for the current line in the repetition.
     * <p>
     * Examples:
     * <p>
     * "Line OddLine FirstLine QueryLine Attribute1Line"
     * "Line EvenLine QueryLine Attribute2Line"
     * "Line OddLine QueryLine Attribute3Line"
     * "Line EvenLine LastLine QueryLine Attribute4Line"
     * 
     * @return String css class derived from rules and position
     */
	public String lineDivClass() {
		String lineBase = (String)d2wContext().valueForKey("baseClassForLine");
		String evenessAndPosition = "Even" + lineBase;
		int lastIndex = currentSectionKeys().count() - 1;
		if (index %2 == 0) {
			evenessAndPosition = "Odd" + lineBase;
		}
		if (index == 0) {
			evenessAndPosition += " First" + lineBase;
		} else if (index == lastIndex) {
			evenessAndPosition += " Last" + lineBase;
		}
		return lineBase + " " + evenessAndPosition + " " + d2wContext().valueForKey("pageType") + lineBase + " " + ERXStringUtilities.capitalize(propertyKey()) + lineBase;
	}

}