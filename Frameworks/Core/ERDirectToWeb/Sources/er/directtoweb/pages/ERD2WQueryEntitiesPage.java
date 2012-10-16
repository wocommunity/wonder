package er.directtoweb.pages;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryAllPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;

/**
 * Page that can query a set of entities.
 * It is like the D2WQueryAll page except that you can partition your entities into sections.
 *
 * @author ak on Mon Sep 01 2003
 * 
 * @d2wKey queryConfigurationName
 * @d2wKey listConfigurationName
 */
public class ERD2WQueryEntitiesPage extends ERD2WPage implements QueryAllPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERD2WQueryEntitiesPage.class);

    protected EODatabaseDataSource queryDataSource;
    protected  WODisplayGroup displayGroup;
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WQueryEntitiesPage(WOContext context) {
        super(context);
    }

    public WODisplayGroup displayGroup() {
        if(displayGroup == null) {
            displayGroup = new WODisplayGroup();
        }
        return displayGroup;
    }

    public EODataSource queryDataSource() {
        return queryDataSource;
    }

    public String queryConfigurationName() { return (String)d2wContext().valueForKey("queryConfigurationName"); }
    public String listConfigurationName() { return (String)d2wContext().valueForKey("listConfigurationName"); }
    
    public WOComponent queryAction() {
        WOComponent result = null;
        if(entity() != null) {
            // construct datasource
        	EOEditingContext ec = ERXEC.newEditingContext(session().defaultEditingContext().parentObjectStore());
            queryDataSource = new EODatabaseDataSource(ec, entity().name());
            queryDataSource.setAuxiliaryQualifier(displayGroup().qualifierFromQueryValues());

            ListPageInterface lpi;
            if(listConfigurationName() != null) {
                lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed(listConfigurationName(), session());
            } else {
                lpi = D2W.factory().listPageForEntityNamed(entity().name(), session());
            }
            
            lpi.setDataSource(queryDataSource);
            lpi.setNextPage(context().page());
            
            // remove old values for next iteration
            displayGroup.queryMatch().removeAllObjects();
            displayGroup.queryOperator().removeAllObjects();
            result = (WOComponent)lpi;
        }
        return result;
    }
    
    public WOComponent showRegularQueryAction() {
        QueryPageInterface qpi = null;
        if(queryConfigurationName() != null) {
            qpi = (QueryPageInterface)D2W.factory().pageForConfigurationNamed(queryConfigurationName(), session());
        }  else {
            qpi = D2W.factory().queryPageForEntityNamed(entity().name(), session());
        }
        return (WOComponent)qpi;
    }
}
