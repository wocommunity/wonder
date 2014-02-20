package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Nested list component, copied over from WXNestedList, better html output. Place actual display in
 * content.
 * @binding list list of items
 * @binding item current item, gets pushed to parent
 * @binding isOrdered  when true, uses a OL, otherwise a UL
 * @binding sublist should return the sublist for the current item.
 * @binding level level if the current item
 *
 */
public class ERXNestedList extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERXNestedList.class);

	public ERXNestedList(WOContext context) {
		super(context);
	}

    /**
     * Method to push the current level (always 1) into the parent;  this always
     * happens at the top/beginning of a list (to let the parent know we are in
     * the list
     */
     
    public void pushLevel()  {
        setValueForBinding(Integer.valueOf(1), "level");
    }


    /**
     * Method to push the current level (always 0) into the parent;  this always
     * happens at the bottom/end of a list (to let the parent know we are done with
     * the list
     */
     
    public void  popLevel()  {
        setValueForBinding(Integer.valueOf(0), "level");
    }


    /**
     * Method to return the current level.  This method always returns null and is 
     * basically a no-op, but it is required by Key-Value coding (since we have a 
     * setCurrentLevel method).
     */
     
    public Number currentLevel() {
        return null;
    }


    /**
     * Method to set the current level (based on the child level).  Whatever the
     * child passes in, we add one (to represent another level deep in the order).
     * By the time the value get to the root, it reflects the total number of levels
     * between the top and bottom.
     */
     
    public void setCurrentLevel(Number aChildLevel)  {
        setValueForBinding(Integer.valueOf(aChildLevel.intValue() + 1) , "level");
    }


    /**
     * Method to return the tag name for the list.  If the 'isOrdered' binding is
     * present, the list is an ORDERED-LIST (<OL>), otherwise the list is an
     * UNORDERED LIST (<UL>).  This information populates the elementName of the
     * generic element for the list.
     */
     
    public String listTagName()  {
        if ( valueForBinding( "isOrdered" ) != null ) {
            return "ol";
        }
        return "ul";
    }
}