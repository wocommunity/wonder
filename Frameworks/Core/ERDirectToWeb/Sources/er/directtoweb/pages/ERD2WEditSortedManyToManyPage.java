//
// ERD2WEditSortedManyToMany.java
// Project ERDirectToWeb
//
// Created by patrice on Thu Aug 29 2002
//

package er.directtoweb.pages;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.EditRelationshipPageInterface;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.directtoweb.Services;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDSortedManyToManyAssignment;
import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXConstant;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGuardedObjectInterface;
import er.extensions.foundation.ERXValueUtilities;

/**
 * @d2wKey displayKey
 * @d2wKey showIndex
 * @d2wKey indexKey
 * @d2wKey browserSize
 * @d2wKey maxBrowserSize
 */
public class ERD2WEditSortedManyToManyPage extends ERD2WPage implements EditRelationshipPageInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    // this pages allows the editing of a sorted many-to-many relationship
    // d2wContext keys:
    //   indexKey: the key on the join entity that contains sort order info

    public static final Logger log = Logger.getLogger(ERD2WEditSortedManyToManyPage.class);
    
    public ERD2WEditSortedManyToManyPage(WOContext c) {
        super(c);
    }

    public final static int QUERY = 0;
    public final static int LIST = 1;
    public final static int NEW = 2;

    protected int _state = QUERY;

    public EODataSource selectDataSource = null;

    protected EOEnterpriseObject _eoToAddToRelationship;
    protected EOEnterpriseObject _newEOInRelationship;
    private String _relationshipKey;
    public String relationshipKey(){ return _relationshipKey; }
    public WODisplayGroup relationshipDisplayGroup = new WODisplayGroup();
    public EOEnterpriseObject browserItem;
    public NSArray browserSelections;
    public String sortedObjects;

    public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipKey) {
    	EOEditingContext ec = ERXEC.newEditingContext(eo.editingContext(), false);  // a non-validating context
        EOEnterpriseObject newObject=EOUtilities.localInstanceOfObject(ec,eo);
        setObject(newObject);
        _relationshipKey = relationshipKey;

        if (!object().isToManyKey(relationshipKey))
            throw new RuntimeException(relationshipKey+" is not a to-many relationship");
        
        EODetailDataSource relationshipDataSource = new EODetailDataSource(object().classDescription(), relationshipKey());
        relationshipDataSource.qualifyWithRelationshipKey(relationshipKey(), newObject);
        setDataSource(relationshipDataSource);
        relationshipDisplayGroup.setDataSource(relationshipDataSource);

        if(isSortedRelationship()){
            EOSortOrdering indexOrdering = EOSortOrdering.sortOrderingWithKey(indexKey(),
                                                                              EOSortOrdering.CompareAscending);
            relationshipDisplayGroup.setSortOrderings(new NSArray(indexOrdering));
        }

        
        relationshipDisplayGroup.fetch();
        setPropertyKey(displayKey());
      }

    public String displayKey() {
        String displayKeyFromD2W = (String)d2wContext().valueForKey("displayKey");
        if(displayKeyFromD2W!=null && displayKeyFromD2W.length()!=0){
            return displayKeyFromD2W;
        }else{
            return destinationRelationship().name()+".userPresentableDescription";
        }
    }

    public boolean displayQuery() {
        return _state == QUERY;
    }
    public boolean displayList() {
        return _state == LIST;
    }
    public boolean displayNew() {
        return _state == NEW;
    }
    
    public boolean isSortedRelationship(){
        boolean isSorted = false;
        if(entity().userInfo().valueForKey("isSortedJoinEntity") != null &&
            ((String)entity().userInfo().valueForKey("isSortedJoinEntity")).equals("true")){
            isSorted = true;
        }
        if(log.isDebugEnabled()){
            log.debug("Sorted relationship entity: " + entity().name() + " is sorted? " + isSorted);
        }
        return isSorted;
    }

	 	 public boolean showIndex(){
			 return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("showIndex"), false);
		 }
	 
    public String browserStringForItem() {
		 String result =  browserItem!=null &&
		 browserItem.valueForKeyPath(displayKey())!=null ?
		 browserItem.valueForKeyPath(displayKey()).toString() : "missing data";
		 if(indexKey()!=null && showIndex()){
			 Integer index = (Integer)browserItem.valueForKey(indexKey());
			 if(index!=null){
				 result = (index.intValue()+1) + ". "+ result ;
			 }
		 }
		 return result;
    }

    public String displayNameForRelationshipKey() {
        return Services.capitalize(relationshipKey());
    }

    public EOEnterpriseObject objectToAddToRelationship() {
        return _eoToAddToRelationship;
    }
    public void setObjectToAddToRelationship(EOEnterpriseObject newValue) {
        _eoToAddToRelationship = newValue;
    }


    // Switch to query view
    public WOComponent queryAction() {
        _state = LIST;
        return context().page();
    }

    public WOComponent selectAction() {
        EOEnterpriseObject _localEoToAddToRelationship = _eoToAddToRelationship != null ?
        EOUtilities.localInstanceOfObject(editingContext(),_eoToAddToRelationship) : null;

        if (_localEoToAddToRelationship != null) {
            // we create a join
            EOEnterpriseObject joinEO=ERXEOControlUtilities.createAndInsertObject(editingContext(), entity().name());

            NSArray sortedObjects=relationshipDisplayGroup.displayedObjects();
            if(isSortedRelationship()){
                Number lastIndex = null;
                if (sortedObjects!=null && sortedObjects.count()>0) {
                    EOEnterpriseObject lastObject=(EOEnterpriseObject)relationshipDisplayGroup.displayedObjects().lastObject();
                    lastIndex=(Number)lastObject.valueForKey(indexKey());
                }
                int newIndex = lastIndex!=null ? lastIndex.intValue()+1 : 0;
                joinEO.takeValueForKey(ERXConstant.integerForInt(newIndex),indexKey());                
            }
            joinEO.addObjectToBothSidesOfRelationshipWithKey(_localEoToAddToRelationship,
                                                             destinationRelationship().name());
            object().addObjectToBothSidesOfRelationshipWithKey(joinEO, relationshipKey());
            dataSource().insertObject(joinEO);
            relationshipDisplayGroup.fetch();
        } else {
            // no object was selected
            _state = QUERY;
        }

        WOComponent result = null;
        if(selectActionDelegate!=null){
            result = selectActionDelegate.nextPage(context().page());
        }
        return result;
    }

    private NextPageDelegate selectActionDelegate;
    public NextPageDelegate selectActionDelegate(){ return selectActionDelegate; }
    public void setSelectActionDelegate(NextPageDelegate newSelectActionDelegate){ selectActionDelegate = newSelectActionDelegate; }

    public WOComponent removeFromToManyRelationshipAction() {
        if(((ERXSession)session()).javaScriptEnabled())
            updateEOsOrdering();	   
        if (browserSelections != null) {
            for (Enumeration e = browserSelections.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject object=(EOEnterpriseObject)e.nextElement();
                EOEnterpriseObject _localEoToRremoveFromRelationship =
                    (EOEnterpriseObject)object.valueForKey(destinationRelationship().name());
                object.removeObjectFromBothSidesOfRelationshipWithKey(_localEoToRremoveFromRelationship,
                                                                    destinationRelationship().name());
                object().removeObjectFromBothSidesOfRelationshipWithKey(object,
                                                                      relationshipKey());
                if(((object instanceof ERXGuardedObjectInterface) && ((ERXGuardedObjectInterface)object).canDelete()) 
                    || !(object instanceof ERXGuardedObjectInterface)) {
                     editingContext().deleteObject(object);
                }
            }
            relationshipDisplayGroup.fetch(); // updateDisplayedObjects is not doing the trick
            if (isSortedRelationship()) {
                //Now need to reindex the joins if the relationship is sorted
                int i = 0;
                for(Enumeration e = relationshipDisplayGroup.displayedObjects().objectEnumerator();
                    e.hasMoreElements();){
                    EOEnterpriseObject object = (EOEnterpriseObject)e.nextElement();
                    object.takeValueForKey(ERXConstant.integerForInt(i), indexKey());
                    i++;
                }
            }
        }
        return null;
    }

    public WOComponent removeAllFromToManyRelationship(){
        if(((ERXSession)session()).javaScriptEnabled())
            updateEOsOrdering();
        for (Enumeration e = relationshipDisplayGroup.displayedObjects().immutableClone().objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject joinObject=(EOEnterpriseObject)e.nextElement();
            EOEnterpriseObject _localEoToRremoveFromRelationship =
                (EOEnterpriseObject)joinObject.valueForKey(destinationRelationship().name());
            joinObject.removeObjectFromBothSidesOfRelationshipWithKey(_localEoToRremoveFromRelationship,
                                                                  destinationRelationship().name());
            object().removeObjectFromBothSidesOfRelationshipWithKey(joinObject,
                                                                    relationshipKey());
                                                                   
            if(((joinObject instanceof ERXGuardedObjectInterface) && ((ERXGuardedObjectInterface)joinObject).canDelete()) 
            	|| !(joinObject instanceof ERXGuardedObjectInterface)) {
                 editingContext().deleteObject(joinObject);
            }
        }
        relationshipDisplayGroup.fetch(); // updateDisplayedObjects is not doing the trick
        return null;
    }

    public WOComponent displayQueryAction() {
        _state = QUERY;        
        return null;
    }

    public Integer itemHashCode(){
        return Integer.valueOf(browserItem.hashCode());
    }

    public void updateEOsOrdering(){
        if(isSortedRelationship()){
            //If the session uses javascript then we need to update the EOs according
            //to what has been changed by the javascript in the WOBrowser
            NSArray hiddenFieldValues = NSArray.componentsSeparatedByString(sortedObjects, ",");
            if(log.isDebugEnabled()) log.debug("hiddenFieldValues = "+hiddenFieldValues);
            if(hiddenFieldValues != null){
                int i = 0;
                for(Enumeration e = hiddenFieldValues.objectEnumerator(); e.hasMoreElements();){
                    String objectForHashCode = (String)e.nextElement();
                    if(log.isDebugEnabled()) log.debug("objectForHashCode = "+objectForHashCode);
                    EOEnterpriseObject eo = objectForHashCode(objectForHashCode);
                    if(eo!=null){
                        eo.takeValueForKey(ERXConstant.integerForInt(i), indexKey());
                    }else{
                        log.warn("objectForHashCode: "+objectForHashCode+" doesn't have a corresponding object");
                    }
                    i++;
                }
            } 
        }
    }


    public WOComponent returnAction() {
        if(((ERXSession)session()).javaScriptEnabled())
            updateEOsOrdering();	   
        editingContext().saveChanges();
        editingContext().revert();
        WOComponent result = nextPageFromDelegate();
    	if(result == null) {
    		result = super.nextPage();
    	}
        if (result != null) {
            return result;
        }
        result = (WOComponent) D2W.factory().editPageForEntityNamed(object().entityName(), session());
        ((EditPageInterface) result).setObject(object());
        editingContext().dispose();
        return result;
    }

    public WOComponent newObjectAction() {
        WOComponent result;
        if (isEntityReadOnly()) {
            throw new IllegalStateException("You cannot create new instances of " + entity().name() + "; it is read-only.");
        } else {
            _state = NEW;
            EOEnterpriseObject eo = ERXEOControlUtilities.createAndInsertObject( editingContext(), destinationEntity().name());
            EditPageInterface epi = D2W.factory().editPageForEntityNamed(destinationEntity().name(),
                                                                         session());
            epi.setObject(eo);
            CreateNewEODelegate delegate = new CreateNewEODelegate();
            delegate.editRelationshipPage = this;
            delegate.createdEO = eo;
            epi.setNextPageDelegate(delegate);
            result = (WOComponent)epi;
        }
        return result;
    }

    public static class CreateNewEODelegate implements NextPageDelegate {
        public WOComponent editRelationshipPage;
        public EOEnterpriseObject createdEO;
        public WOComponent nextPage(WOComponent sender){
            ((ERD2WEditSortedManyToManyPage)editRelationshipPage)._eoToAddToRelationship = createdEO;
            ((ERD2WEditSortedManyToManyPage)editRelationshipPage).selectAction();
            ((ERD2WEditSortedManyToManyPage)editRelationshipPage)._state = ERD2WEditSortedManyToManyPage.QUERY; 
            return editRelationshipPage;
        }
    }
    
    public EOEnterpriseObject newObjectInRelationship() {
        return _newEOInRelationship;
    }


    public WOComponent saveAction() {
        relationshipDisplayGroup.fetch();
        _state = QUERY;
        return null;
    }

    private EORelationship _destinationRelationship;
    public EORelationship destinationRelationship() {
        if (_destinationRelationship==null) {
            NSArray joinRelationships = ERDSortedManyToManyAssignment.joinRelationshipsForJoinEntity(entity());

            String originEntityName=object().entityName();
            //General case
            for (Enumeration e=joinRelationships.objectEnumerator(); e.hasMoreElements();) {
                EORelationship r=(EORelationship)e.nextElement();
                if (!originEntityName.equals(r.destinationEntity().name())) {
                    _destinationRelationship=r;
                    break;
                }
            }
            // In the case we have a self join relationship we have to be more clever
            if(_destinationRelationship==null){
                EOEntity originEntity = EOModelGroup.defaultGroup().entityNamed(originEntityName);
                EORelationship originRelationship = originEntity.relationshipNamed(relationshipKey());
                EORelationship inverseOriginRelationship = originRelationship.inverseRelationship();
                for (Enumeration e=joinRelationships.objectEnumerator(); e.hasMoreElements();) {
                    EORelationship r=(EORelationship)e.nextElement();
                    if (r!=inverseOriginRelationship) {
                        _destinationRelationship=r;
                        break;
                    }
                }
            }

        }
        return _destinationRelationship;
    }

    public EOEntity destinationEntity() {
        return destinationRelationship().destinationEntity();
    }
    
    public String indexKey() {
        return (String)d2wContext().valueForKey("indexKey");
    }

    public WOComponent moveObjectUp()
    {
        if(log.isDebugEnabled()){
            log.debug("browserSelection ="+browserSelection());
        }
        if(browserSelection()!=null){
            NSArray sortedObjects=relationshipDisplayGroup.displayedObjects();
           int selectedIndex = ((Integer)browserSelection().valueForKey(indexKey())).intValue();
           if(log.isDebugEnabled()){
               log.debug("sortedObjects ="+sortedObjects);
               log.debug("selectedIndex = "+selectedIndex);
               log.debug("indexKey = "+indexKey());
           } 
            if(selectedIndex!=0){
                objectAtIndex(selectedIndex-1).takeValueForKey(Integer.valueOf(selectedIndex),
                                                               indexKey());
                browserSelection().takeValueForKey(Integer.valueOf(selectedIndex-1),
                                                   indexKey());
            }
            
        }
        relationshipDisplayGroup.updateDisplayedObjects();
        return null;
    }

    public WOComponent moveObjectDown()
    {
        if(browserSelection()!=null){
            NSArray sortedObjects=relationshipDisplayGroup.displayedObjects();
            // make the compiler happy
            sortedObjects.count(); 
            int selectedIndex = ((Integer)browserSelection().valueForKey(indexKey())).intValue();
            EOEnterpriseObject lastObject =
                (EOEnterpriseObject)relationshipDisplayGroup.displayedObjects().lastObject();
            int lastIndex =
                ((Integer)lastObject.valueForKey(indexKey())).intValue();
            if(selectedIndex!=lastIndex){
                objectAtIndex(selectedIndex+1).takeValueForKey(Integer.valueOf(selectedIndex),
                                                               indexKey());
                browserSelection().takeValueForKey(Integer.valueOf(selectedIndex+1),
                                                   indexKey());
            }
        }
        relationshipDisplayGroup.updateDisplayedObjects();
        return null;
    }

    public EOEnterpriseObject objectAtIndex(int index){
        EOEnterpriseObject result = null;
        if(log.isDebugEnabled()){
            log.debug("looking for index "+index);
        }
        for(Enumeration e = relationshipDisplayGroup.displayedObjects().objectEnumerator();
            e.hasMoreElements();){
            EOEnterpriseObject indexObject = (EOEnterpriseObject)e.nextElement();
            if(log.isDebugEnabled()){
                log.debug("object is at index"+indexObject.valueForKey(indexKey()));
            }
            if( ((Integer)indexObject.valueForKey(indexKey())).intValue() == index){
                result = indexObject;
                break;
            }
        }
        return result;
    }

    public EOEnterpriseObject objectForHashCode(String hashCode){
        EOEnterpriseObject result = null;
        if(log.isDebugEnabled()){
            log.debug("looking for hashCode "+hashCode+".");
        }
        for(Enumeration e = relationshipDisplayGroup.displayedObjects().objectEnumerator();
            e.hasMoreElements();){
            EOEnterpriseObject indexObject = (EOEnterpriseObject)e.nextElement();
            if(log.isDebugEnabled()){
                log.debug("object's hashCode is "+indexObject.hashCode());
            }
            if( (Integer.toString(indexObject.hashCode())).equals(hashCode)){
                result = indexObject;
                break;
            }
        }
        return result;
    }

    public EOEnterpriseObject browserSelection(){
        EOEnterpriseObject result = null;
        if (browserSelections != null && browserSelections.count()!=0) {
            if(browserSelections.count()>1){
                throw new RuntimeException("Please choose only one element");
            }else
                result = (EOEnterpriseObject)browserSelections.objectAtIndex(0);
        }
        return result;
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c){
        if(((ERXSession)session()).javaScriptEnabled()){
            StringBuilder result = new StringBuilder();
            for(Enumeration e = relationshipDisplayGroup.displayedObjects().objectEnumerator();
                e.hasMoreElements();){
                result.append(e.nextElement().hashCode()+",");
            }
            sortedObjects = result.toString();
        }
        super.appendToResponse(r,c);
    }

    public int browserSize() {
        int browserSize = 10;  // reasonable default value
        int maxBrowserSize = 20;

        String contextSize = (String)d2wContext().valueForKey("browserSize");
        if(contextSize != null) {
            try {
                browserSize = Integer.parseInt(contextSize);
            } catch(NumberFormatException nfe) {
                log.error("browserSize not a number: " + browserSize);
            }
        }
        String maxContextSize = (String)d2wContext().valueForKey("maxBrowserSize");
        if(maxContextSize != null) {
            try {
                maxBrowserSize = Integer.parseInt(maxContextSize);
            } catch(NumberFormatException nfe) {
                log.error("maxBrowserSize not a number: " + maxBrowserSize);
            }
        }
        int count = relationshipDisplayGroup.displayedObjects().count();
        browserSize = (count > browserSize && count < maxBrowserSize) ? count : browserSize;
        return browserSize;
    }
}
