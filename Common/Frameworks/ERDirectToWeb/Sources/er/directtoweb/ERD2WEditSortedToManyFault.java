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

	public String indexKey(){
		String indexKey = null;
		EORelationship relationship = entity().relationshipNamed(propertyKey());
		if (relationship!=null) {
			EOEntity destinationEntity = relationship.destinationEntity();
			if(destinationEntity!=null &&
		destinationEntity.userInfo().valueForKey("isSortedJoinEntity") != null &&
		((String)destinationEntity.userInfo().valueForKey("isSortedJoinEntity")).equals("true")) {
				synchronized (_context) {
					_context.setEntity(destinationEntity);
					indexKey = (String)_context.valueForKey("indexKey");
				}
			}
		}
		return indexKey;
	}


    private static D2WContext _context=new D2WContext();
    public NSArray sortedBrowserList() {
        NSArray result = browserList();
        if (indexKey()!=null)
            result = ERXArrayUtilities.sortedArraySortedWithKey(result,
																					 indexKey(),
																					 null);
        
        return result;
	 }

	 public String browserStringForItem(){
		 String result = super.browserStringForItem();
		 if(showIndex()){
			 Integer index = (Integer)browserItem.valueForKey(indexKey());
			 if(index != null){
				 result = (index.intValue()+1)+". "+result;
			 }
		 }
		 return result;
	 }

	 public boolean showIndex(){
		 return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("showIndex"), false);
	 }

}