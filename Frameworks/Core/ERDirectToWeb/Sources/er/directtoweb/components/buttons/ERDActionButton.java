package er.directtoweb.components.buttons;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.directtoweb.components.ERDCustomComponent;
import er.directtoweb.interfaces.ERDPickPageInterface;

/**
 * Abstract superclass for all actions inside of Wonder D2W.
 * 
 * @author ak on Mon Sep 01 2003
 */
public  class ERDActionButton extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDActionButton.class);

    public interface Keys {
        public static final String object = "object";
        public static final String displayGroup = "displayGroup";
        public static final String dataSource = "dataSource";
        public static final String task = "task";
    }
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDActionButton(WOContext context) {
        super(context);
    }

    /** Action buttons must be stateless. */
    @Override
    public final boolean isStateless() { return true; }

    /** Action buttons do not synchronize their variables. */
    @Override
    public final boolean synchronizesVariablesWithBindings() { return false; }

    /** The current object. */
    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding(Keys.object); }

    /** The current display group. */
    public WODisplayGroup displayGroup() { return (WODisplayGroup)valueForBinding(Keys.displayGroup); }

    /** The current data source. */
    public EODataSource dataSource() {return (EODataSource)valueForBinding(Keys.dataSource); }

    /** The current task.*/
    @Override
    public String task() { return (String)valueForBinding(Keys.task);  }

    /** Utility to return the next page in the enclosing page. */
    public WOComponent nextPageInPage(D2WPage parent) {
        return ERD2WUtilities.nextPageInPage(parent);
    }

    /** Utility to return the first enclosing component that matches the given class, if there is one. */
    protected WOComponent enclosingPageOfClass(Class c) {
        return ERD2WUtilities.enclosingPageOfClass(this, c);
    }

    /** Utility to return the outermost page that is a D2W page. This is needed because this component might be embedded inside a plain page. */
    protected D2WPage topLevelD2WPage() {
        return ERD2WUtilities.topLevelD2WPage(this);
    }

    /** Utility to return the enclosing list page, if there is one. */
    protected ListPageInterface parentListPage() {
        return (ListPageInterface)enclosingPageOfClass(ListPageInterface.class);
    }
    
    /** Utility to return the enclosing edit page, if there is one. */
    protected EditPageInterface parentEditPage() {
        return (EditPageInterface)enclosingPageOfClass(EditPageInterface.class);
    }
    
    /** Utility to return the enclosing select page, if there is one. */
    protected SelectPageInterface parentSelectPage() {
        return (SelectPageInterface)enclosingPageOfClass(SelectPageInterface.class);
    }
    
    /** Utility to return the enclosing query page, if there is one. */
    protected QueryPageInterface parentQueryPage() {
        return (QueryPageInterface)enclosingPageOfClass(QueryPageInterface.class);
    }

    /** Utility to return the enclosing pick page, if there is one. */
    protected ERDPickPageInterface parentPickPage() {
        return (ERDPickPageInterface)enclosingPageOfClass(ERDPickPageInterface.class);
    }

    /** Utility to return the enclosing D2W page, if there is one. */
    public D2WPage parentD2WPage() {
        return (D2WPage)enclosingPageOfClass(D2WPage.class);
    }
    
    /*    public EODataSource dataSource() {
        if(hasBinding("displayGroup"))
        return ((WODisplayGroup)valueForBinding("displayGroup")).dataSource();
    return (EODataSource)valueForBinding("dataSource");
    }*/
    
}
