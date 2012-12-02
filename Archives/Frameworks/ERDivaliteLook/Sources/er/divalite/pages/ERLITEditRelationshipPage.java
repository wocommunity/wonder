package er.divalite.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.ERD2WEditRelationshipPage;

/**
 * Divalite editRelationship page
 * 
 * @author ravim
 *
 */
public class ERLITEditRelationshipPage extends ERD2WEditRelationshipPage {
    public ERLITEditRelationshipPage(WOContext context) {
        super(context);
    }
    
    /** interface for all the keys used in this pages code */
    public static interface Keys extends ERD2WEditRelationshipPage.Keys {
    	public static final String subTask = "forcedSubTask";
    }
    
    // actions   
    /**
     * Overridden because the action bound should return.
     */
    @Override
    public WOComponent selectAction() {
        super.selectAction();
        return returnAction();
    }
    
    @Override
    public WOComponent queryAction() {
    	d2wContext().takeValueForKey("list", Keys.subTask);
        return super.queryAction();
    }
    
    @Override
    public WOComponent saveAction() {
    	d2wContext().takeValueForKey("query", Keys.subTask);
        return super.saveAction();
    }
    
    @Override
    public WOComponent displayQueryAction() {
    	if (displayNew()) dataSource().editingContext().revert();
    	d2wContext().takeValueForKey("query", Keys.subTask);
        return super.displayQueryAction();
    }
    
    // accessors    
    public String queryPageConfiguration() {
    	return "QueryEditRelationship" + d2wContext().entity().name();
    }
    
    public String listPageConfiguration() {
    	return "Select" + d2wContext().entity().name();
    }
    
    public String newPageConfiguration() {
    	return "EditEditRelationship" + d2wContext().entity().name();		// FIXME: change to CreateEditRelationship
    }
}