//
// ERD2WEditSortedManyToMany.java
// Project ERDirectToWeb
//
// Created by patrice on Thu Aug 29 2002
//

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.util.*;
import er.extensions.*;

public class ERD2WEditSortedManyToManyPage extends ERD2WPage implements EditRelationshipPageInterface {

    // this pages allows the editing of a sorted many-to-many relationship
    // d2wContext keys:
    //   indexKey: the key on the join entity that contains sort order info

    public static final ERXLogger log = ERXLogger.getERXLogger(ERD2WEditSortedManyToManyPage.class);
    
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
    protected EOEditingContext _editingContext;
    private String _relationshipKey;
    public WODisplayGroup relationshipDisplayGroup = new WODisplayGroup();
    public EOEnterpriseObject browserItem;
    public NSArray browserSelections;



    public void setMasterObjectAndRelationshipKey(EOEnterpriseObject eo, String relationshipKey) {
        setEditingContext(ERXExtensions.newEditingContext(eo.editingContext(), false)); // a non-validating context
        EOEnterpriseObject newObject=(EOEnterpriseObject)EOUtilities.localInstanceOfObject(editingContext(),eo);
        setObject(newObject);
        _relationshipKey = relationshipKey;

        if (!object().isToManyKey(relationshipKey))
            throw new RuntimeException(relationshipKey+" is not a to-many relationship");
        
        EODetailDataSource relationshipDataSource = new EODetailDataSource(object().classDescription(), _relationshipKey);
        relationshipDataSource.qualifyWithRelationshipKey(_relationshipKey, newObject);
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

    public void awake() {
        super.awake();
        if (editingContext() != null) {
            editingContext().lock();
        }
    }

    public void sleep() {
        if (editingContext() != null) {
           editingContext().unlock();
        }
        super.sleep();
    }

    public EOEditingContext editingContext() {
        return _editingContext;
    }

    protected void setEditingContext(EOEditingContext newEditingContext) {
        if (newEditingContext != editingContext()) {
            if (editingContext() != null) {
                editingContext().unlock();
            }
            _editingContext = newEditingContext;
            if (editingContext() != null) {
                editingContext().lock();
            }
        }
    } 

    public String displayKey() {
        return destinationRelationship().name()+".userPresentableDescription";
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
        log.info("Sorted relationship entity: " + entity().name() + " is sorted? " + isSorted);
        return isSorted;
    }

    public String browserStringForItem() {
        return browserItem!=null ? browserItem.valueForKeyPath(displayKey()).toString() : null;
    }

    public String displayNameForRelationshipKey() {
        return Services.capitalize(_relationshipKey);
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
        return null;
    }

    public WOComponent selectAction() {
        EOEnterpriseObject _localEoToAddToRelationship = _eoToAddToRelationship != null ?
        EOUtilities.localInstanceOfObject(editingContext(),_eoToAddToRelationship) : null;

        if (_localEoToAddToRelationship != null) {
            // we create a join EO
            EOEnterpriseObject joinEO=ERXUtilities.createEO(entity().name(),
                                                             editingContext());

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
            object().addObjectToBothSidesOfRelationshipWithKey(joinEO, _relationshipKey);
            dataSource().insertObject(joinEO);
            relationshipDisplayGroup.fetch();
        } else {
            // no object was selected
            _state = QUERY;
        }

        return null;
    }

    public WOComponent removeFromToManyRelationshipAction() {
        if (browserSelections != null) {
            for (Enumeration e = browserSelections.objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject object=(EOEnterpriseObject)e.nextElement();
                object.editingContext().deleteObject(object);
                dataSource().deleteObject(object);
            }
            relationshipDisplayGroup.fetch(); // updateDisplayedObjects is not doing the trick
        }
        return null;
    }


    public WOComponent displayQueryAction() {
        _state = QUERY;        
        return null;
    }


    public WOComponent returnAction() {
        editingContext().saveChanges();
        WOComponent result = nextPageDelegate() != null ? nextPageDelegate().nextPage(this) : super.nextPage();
        if (result != null) {
            return result;
        }
        result = (WOComponent) D2W.factory().editPageForEntityNamed(object().entityName(), session());
        ((EditPageInterface) result).setObject(object());
        return result;
    }

    public WOComponent newObjectAction() {
        if (isEntityReadOnly()) {
            throw new IllegalStateException("You cannot create new instances of " + entity().name() + "; it is read-only.");
        } else {
            _state = NEW;
            _newEOInRelationship = (EOEnterpriseObject) dataSource().createObject();
            editingContext().insertObject(_newEOInRelationship);
            dataSource().insertObject(_newEOInRelationship);
        }
        return null;
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

            EORelationship destinationRelationship=null;
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
                EORelationship originRelationship = originEntity.relationshipNamed(_relationshipKey);
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
                objectAtIndex(selectedIndex-1).takeValueForKey(new Integer(selectedIndex),
                                                               indexKey());
                browserSelection().takeValueForKey(new Integer(selectedIndex-1),
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
            int selectedIndex = ((Integer)browserSelection().valueForKey(indexKey())).intValue();
            EOEnterpriseObject lastObject =
                (EOEnterpriseObject)relationshipDisplayGroup.displayedObjects().lastObject();
            int lastIndex =
                ((Integer)lastObject.valueForKey(indexKey())).intValue();
            if(selectedIndex!=lastIndex){
                objectAtIndex(selectedIndex+1).takeValueForKey(new Integer(selectedIndex),
                                                               indexKey());
                browserSelection().takeValueForKey(new Integer(selectedIndex+1),
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
                //break;
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
}
