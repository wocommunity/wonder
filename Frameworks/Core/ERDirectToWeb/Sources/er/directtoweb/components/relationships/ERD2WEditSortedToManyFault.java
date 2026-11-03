//
// ERD2WEditSortedToManyRelationship.java: Class file for WO Component 'ERD2WEditSortedToManyRelationship'
// Project ERDirectToWeb
//
// Created by bposokho on Thu Sep 19 2002
//
package er.directtoweb.components.relationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WEditToManyFault;
import com.webobjects.directtoweb.ERD2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * @d2wKey allowCollapsing
 * @d2wKey destinationEntityName
 * @d2wKey showIndex
 * @d2wKey browserSize
 * @d2wKey maxBrowserSize
 */
public class ERD2WEditSortedToManyFault extends D2WEditToManyFault {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static final Logger log = LoggerFactory.getLogger(ERD2WEditSortedToManyFault.class);

    public ERD2WEditSortedToManyFault(WOContext context) {
        super(context);
    }

    /**
     * Computes the destination entity that we're editing.  Hits the "destinationEntityName"
     * rule.
     *
     * @return destination entity
     */
    public EOEntity destinationEntity() {
        final String destinationEntityName = (String)d2wContext().valueForKey("destinationEntityName");
        EOEntity result = null;

        if ( destinationEntityName != null )
            result = EOUtilities.entityNamed(object().editingContext(), destinationEntityName);

        return result;
    }

    public String indexKey(){
        final EOEntity destinationEntity = destinationEntity();
        String indexKey = null;

        if ( destinationEntity != null ) {
            final String isSortedJoinValue = (String)destinationEntity.userInfo().valueForKey("isSortedJoinEntity");

            if ( "true".equals(isSortedJoinValue) ) {
                synchronized (_context) {
                    _context.setEntity(destinationEntity);
                    indexKey = (String)_context.valueForKey("indexKey");
                }
            }
        }

        return indexKey;
    }


    private static D2WContext _context=ERD2WContext.newContext();
    public NSArray sortedBrowserList() {
        NSArray result = browserList();
        if (indexKey()!=null)
            result = ERXArrayUtilities.sortedArraySortedWithKey(result,
                                                                indexKey(),
                                                                null);

        return result;
    }

    @Override
    public String browserStringForItem(){
        String result = super.browserStringForItem();
        if(showIndex()){
            Integer index = (Integer)browserItem.valueForKey(indexKey());
            if(index != null){
                result = index.intValue() + ". " + result;
            }
        }
        return result;
    }

    public boolean showIndex(){
        return ERXValueUtilities.booleanValueWithDefault(d2wContext().valueForKey("showIndex"), false);
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

        NSArray sortedBrowserList = sortedBrowserList();
        if(sortedBrowserList != null) {
            int count = sortedBrowserList.count();
            browserSize = (count > browserSize && count < maxBrowserSize) ? count : browserSize;
        }
        return browserSize;
    }
}
