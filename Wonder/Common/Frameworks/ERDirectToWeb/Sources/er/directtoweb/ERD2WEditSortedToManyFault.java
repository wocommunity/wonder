//
// ERD2WEditSortedToManyRelationship.java: Class file for WO Component 'ERD2WEditSortedToManyRelationship'
// Project ERDirectToWeb
//
// Created by bposokho on Thu Sep 19 2002
//
package er.directtoweb;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*; 

public class ERD2WEditSortedToManyFault extends D2WEditToManyFault {

    public ERD2WEditSortedToManyFault(WOContext context) {
        super(context);
    }

    public NSArray sortedBrowserList()
{
        NSArray result = null;
        EORelationship relationship = entity().relationshipNamed(propertyKey());
        EOEntity destinationEntity = relationship.destinationEntity();
        if(destinationEntity.userInfo().valueForKey("isSortedJoinEntity") != null &&
           ((String)destinationEntity.userInfo().valueForKey("isSortedJoinEntity")).equals("true")){
            D2WContext d2wContext = new D2WContext();
            d2wContext.setEntity(destinationEntity);
            String indexKey = (String)d2wContext.valueForKey("indexKey");
            result =   ERXArrayUtilities.sortedArraySortedWithKey(browserList(),
                                                                  indexKey,
                                                               null);
        }else{
            result = browserList();
        }
        return result;
}

}