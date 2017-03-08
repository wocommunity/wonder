package er.reporting;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;

public class WRNestedList extends WOComponent  {

    public WRNestedList(WOContext aContext)  {
        super(aContext);
    }

    /**
     * Override of method for synchronization of local instance variables with
     * bindings (pushing and pulling values from the bindings).  Here we turn
     * OFF synchronization.
     */

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    /**
     * Method to push the current level (always 1) into the parent;  this always
     * happens at the top/beginning of a list (to let the parent know we are in
     * the list
     */

    public void pushLevel()  {
        setValueForBinding( Integer.valueOf(1) , "level" );
    }

    /**
     * Method to push the current level (always 0) into the parent;  this always
     * happens at the bottom/end of a list (to let the parent know we are done with
     * the list
     */

    public void  popLevel()  {
        setValueForBinding( Integer.valueOf(0) , "level" );
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
     * present, the list is an ORDERED-LIST (&lt;OL&gt;), otherwise the list is an
     * UNORDERED LIST (&lt;UL&gt;).  This information populates the elementName of the
     * generic element for the list.
     */

    public String listTagName()  {
        if ( valueForBinding( "isOrdered" ) != null ) {
            return "ol";
        }
        return "ul";
    }

    public boolean notSublistConditional() {
        return !hasBinding("showParentContent");
    }


    @Override
    public void takeValuesFromRequest(WORequest r, WOContext c) {
        //Abort call to super to save all this processing time
    }


}