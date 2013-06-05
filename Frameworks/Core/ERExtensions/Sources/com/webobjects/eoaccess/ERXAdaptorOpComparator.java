package com.webobjects.eoaccess;


import com.webobjects.foundation.NSArray;


/**
 * EOAdaptorOpComparator that handles vertical inheritance. 
 *
 */
public class ERXAdaptorOpComparator extends EOAdaptorOpComparator {

	
	/**
	 * @param entityNameOrdering ordering of entity names to base sorting on
	 */
	public ERXAdaptorOpComparator(NSArray entityNameOrdering) {
		super(entityNameOrdering);
	}

	
    @Override
    public int compare(Object object1, Object object2)
    	throws com.webobjects.foundation.NSComparator.ComparisonException {

    	if(object1 == null || 
    	   object2 == null || 
    	   !(object1 instanceof EOAdaptorOperation) || 
    	   !(object2 instanceof EOAdaptorOperation)) {
    		throw new com.webobjects.foundation.NSComparator.ComparisonException("Unable to compare objects. Objects should be instance of class EOAdaptorOperation. Comparison was made with " + object1 + " and " + object2 + ".");
    	}

        EOAdaptorOperation a = (EOAdaptorOperation)object1;
        EOAdaptorOperation b = (EOAdaptorOperation)object2;	
        int aOpType = a.adaptorOperator();
        int bOpType = b.adaptorOperator();

        if(aOpType == 4) {
        	aOpType = decodeAdaptorTypeForEntityAndStoredProcedure(a.entity(), a.storedProcedure());
        }

		if(bOpType == 4) {
			bOpType = decodeAdaptorTypeForEntityAndStoredProcedure(b.entity(), b.storedProcedure());
		}
	
		if(aOpType != bOpType) {
			return aOpType >= bOpType ? 1 : -1;
	    }
		
        NSArray entityNames = _context;
        
        String entityNameA = a.entity().name();
        String entityNameB = b.entity().name();
        
        // Use correct name for flattened attributes
        if (entityNameA.equals(entityNameB) && a.entity().parentEntity() != null) {
        	entityNameA = entityNameFromAdaptorOperation(a);
        	entityNameB = entityNameFromAdaptorOperation(b);
        }
        
        int aPriority = entityNames == null ? 0 : entityNames.indexOfObject(entityNameA);
        int bPriority = entityNames == null ? 0 : entityNames.indexOfObject(entityNameB);
	    int order = 1;
	    if(aPriority == bPriority) {
	    	order = 0;
	    } 
	    else if(aPriority < bPriority) {
	    	order = -1;
	    } 
	
	    return (aOpType == 3) ? -order : order;
    }
    
    
    /**
	 * Returns the name of EOEntity updated by adaptorOp.  If this is updating through a flattened
	 * attribute, the name returned is the Entity of the target attribute.
	 * 
	 * @param adaptorOp EOAdaptorOperation to return Entity name from
	 * @return name of EOEntity updated by adaptorOp
	 */
	protected String entityNameFromAdaptorOperation(EOAdaptorOperation adaptorOp) {
		EOEntity entity = adaptorOp.entity();

		// If there are no changed valued (e.g. EOAdaptorDeleteOperator), use the standard entity
		if (adaptorOp.changedValues() == null)
		{
			return entity.name();
		}
		
		// Flattened attributes are grouped together by entity so any attribute in changedValues should work
		NSArray<String> attrNames = adaptorOp.changedValues().allKeys();
		for(int i = 0; i < attrNames.count(); i++) {
			EOAttribute attribute = entity.attributeNamed(attrNames.objectAtIndex(i));
			/*
			 * Sometimes attributes are named like "NeededByEOF0" for vertical inheritance. 
			 * With single table inheritance, attributes names from other entities may appear. 
			 * Better safe than sorry :) Keep checking until we find an attribute on the 
			 * entity for the given attribute name.
			 * 
			 * Long discussion about this here:
			 * http://article.gmane.org/gmane.comp.web.webobjects.wonder-disc/8055
			 * 
			 * I think this should do the trick and make vertical and single table happy :)
			 */
			if(attribute != null) {
				if(!attribute.isFlattened()) {
					return entity.name();
				}
				return attribute.targetAttribute().entity().name();
			}
		}
		
		return entity.name();
	}


	private int decodeAdaptorTypeForEntityAndStoredProcedure(EOEntity entity, EOStoredProcedure sp)
    {
    	if(entity.storedProcedureForOperation("EOInsertProcedure") == sp)
        {
    		return 1;
        }
    	
    	return entity.storedProcedureForOperation("EODeleteProcedure") != sp ? 4 : 3;
    }
    
}
