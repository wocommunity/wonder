package er.extensions;

import java.util.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eoaccess.EOModelGroup.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * @author david@cluster9.com
 * 
 * This is the default EOModelGroup delegate. It just implements one method:
 * <code>subEntityForEntity(EOEntity entity, NSDictionary dict)</code>
 * which is being used if <code>ERXLongPrimaryKeyFactory.encodeEntityInPkValue()</code>
 * returns <code>true</code>
 * 
 *  
 */
public class ERXDefaultModelGroupDelegate {

    /**
     *  
     */
    public ERXDefaultModelGroupDelegate() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.webobjects.eoaccess.EOModelGroup.Delegate#subEntityForEntity(com.webobjects.eoaccess.EOEntity,
     *      com.webobjects.foundation.NSDictionary)
     */
    public EOEntity subEntityForEntity(EOEntity entity, NSDictionary pkDict) {
        if (ERXLongPrimaryKeyFactory.encodeEntityInPkValue()) {
            //get the code, we assume that the pkDict contains only one pk value!
            NSArray values = pkDict.allValues();
            if (values.count() > 1) throw new IllegalArgumentException("subEntityForEntity in its default implementation"+
                    " works only with single pk long values ");
            long pkValueWithCode;
            try {
                Number n = (Number) values.objectAtIndex(0);
                pkValueWithCode = n.longValue();
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("subEntityForEntity in its default implementation"+
                " works only with single pk long values, expected a java.lang.Number but got a "+values.objectAtIndex(0));
            }
            long entityCode = pkValueWithCode & (1 << ERXLongPrimaryKeyFactory.CODE_LENGTH) - 1;
            if (entityCode == 0) return null;
            for (Enumeration subEntities = entity.subEntities().objectEnumerator(); subEntities.hasMoreElements();) {
                EOEntity subEntity = (EOEntity) subEntities.nextElement();
                if (((ERXModelGroup) ERXApplication.erxApplication().defaultModelGroup()).entityCode(subEntity) == entityCode) {
                    return subEntity;
                }
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.webobjects.eoaccess.EOModelGroup.Delegate#relationshipForRow(com.webobjects.eoaccess.EOEntity,
     *      com.webobjects.foundation.NSDictionary,
     *      com.webobjects.eoaccess.EORelationship)
     */
    //    public EORelationship relationshipForRow(EOEntity arg0, NSDictionary
    // arg1, EORelationship arg2) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

    /*
     * (non-Javadoc)
     * 
     * @see com.webobjects.eoaccess.EOModelGroup.Delegate#failedToLookupClassNamed(com.webobjects.eoaccess.EOEntity,
     *      java.lang.String)
     */
    //    public Class failedToLookupClassNamed(EOEntity arg0, String arg1) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

    /*
     * (non-Javadoc)
     * 
     * @see com.webobjects.eoaccess.EOModelGroup.Delegate#classForObjectWithGlobalID(com.webobjects.eoaccess.EOEntity,
     *      com.webobjects.eocontrol.EOGlobalID)
     */
    //    public Class classForObjectWithGlobalID(EOEntity arg0, EOGlobalID arg1) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }

    /*
     * (non-Javadoc)
     * 
     * @see com.webobjects.eoaccess.EOModelGroup.Delegate#relationshipFailedToLookupDestinationWithName(com.webobjects.eoaccess.EORelationship,
     *      java.lang.String)
     */
    //    public EOEntity
    // relationshipFailedToLookupDestinationWithName(EORelationship arg0, String
    // arg1) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
}