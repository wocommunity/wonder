import java.math.BigDecimal;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.directtoweb.ERD2WModel;
import er.directtoweb.pages.ERD2WPage;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;

public class Main extends ERD2WPage {

	public WODisplayGroup displayGroup;
	public EOEntity currentEntity;
	public NSArray entities;
	public String currentTask;
	
    public Main(WOContext context) {
        super(context);
        setEditingContext(session().defaultEditingContext());
        entities = EOModelGroup.defaultGroup().models().lastObject().entities();
        displayGroup = new WODisplayGroup();
        setTaskEntity("edit", EOUtilities.entityNamed(editingContext(), "String"));
    }
    
    private void setTaskEntity(String task, EOEntity entity) {
    	D2WContext c = ERD2WContext.newContext(session());
    	c.setDynamicPage(ERXStringUtilities.capitalize(task) + entity.name());
    	c.setTask(task);
    	c.setEntity(entity);
    	c.setTask(task);
    	// c.takeValueForKey(new NSArray("value"), "displayPropertyKeys");
    	setLocalContext(c);
    	String entityName = d2wContext().entity().name();
    	EOEnterpriseObject object = EOUtilities.createAndInsertInstance(editingContext(), entityName);
    	Object value = null;
    	if("Boolean".equals(entityName)) {
    		value = Boolean.TRUE;
    	} else if("Integer".equals(entityName)) {
    		value = Integer.valueOf("12345");
    	} else if("Decimal".equals(entityName)) {
    		value = new BigDecimal("1234.1234");
    	} else if("String".equals(entityName)) {
    		value = new String("This is a test");
       	} else if("NSTimestamp".equals(entityName)) {
    		value = new NSTimestamp();
      	} else if("NSData".equals(entityName)) {
    		value = ERXConstant.EmptyImage;
      	} else if("ToOneRelation".equals(entityName)) {
    		value = EOUtilities.createAndInsertInstance(editingContext(), "ToOneRelation");
      	} else if("ToManyRelation".equals(entityName)) {
    		value = new NSArray(EOUtilities.createAndInsertInstance(editingContext(), "ToManyRelation"));
    	}
    	object.takeValueForKey(value, "value");

    	d2wContext().takeValueForKey(object, "object");
    	setObject(object);
    	setDataSource(new EODatabaseDataSource(editingContext(), d2wContext().entity().name()));
    	displayGroup.setDataSource(dataSource());
    	d2wContext().setPropertyKey("value");
    	d2wContext().takeValueForKey("D2WDisplayString", "innerComponentName");
    	log.debug(d2wContext().entity().name() + " " + d2wContext().task() 
    			+ ": " + d2wContext().componentsAvailable()
    	);
    	
    	//ERDirectToWeb.setD2wDebuggingEnabled(session(), true);
    	//ERDirectToWeb.setD2wComponentNameDebuggingEnabled(session(), true);
    }
    
    public NSArray availableTasks() {
    	return ((ERD2WModel)d2wContext().model()).availableTasks();
    }

    public WOComponent selectTask() {
        setTaskEntity(currentTask, d2wContext().entity());
    	return context().page();
    }
    
    public WOComponent selectEntity() {
        setTaskEntity(d2wContext().task(), currentEntity);
    	return context().page();
    }
}
