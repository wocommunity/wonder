// $LastChangedRevision: 5773 $ DO NOT EDIT.  Make changes to Join.java instead.
package er.neo4jadaptor.test.eo;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public abstract class _Join extends  EOGenericRecord {
	public static final String ENTITY_NAME = "Join";

	// Attributes

	// Relationships
	public static final String FIRST_ENTITY_KEY = "firstEntity";
	public static final String SECOND_ENTITY_KEY = "secondEntity";

  private static Logger LOG = Logger.getLogger(_Join.class);

  public Join localInstanceIn(EOEditingContext editingContext) {
    Join localInstance = (Join)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public er.neo4jadaptor.test.eo.FirstEntity firstEntity() {
    return (er.neo4jadaptor.test.eo.FirstEntity)storedValueForKey("firstEntity");
  }

  public void setFirstEntityRelationship(er.neo4jadaptor.test.eo.FirstEntity value) {
    if (_Join.LOG.isDebugEnabled()) {
      _Join.LOG.debug("updating firstEntity from " + firstEntity() + " to " + value);
    }
    if (value == null) {
    	er.neo4jadaptor.test.eo.FirstEntity oldValue = firstEntity();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "firstEntity");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "firstEntity");
    }
  }
  
  public er.neo4jadaptor.test.eo.SecondEntity secondEntity() {
    return (er.neo4jadaptor.test.eo.SecondEntity)storedValueForKey("secondEntity");
  }

  public void setSecondEntityRelationship(er.neo4jadaptor.test.eo.SecondEntity value) {
    if (_Join.LOG.isDebugEnabled()) {
      _Join.LOG.debug("updating secondEntity from " + secondEntity() + " to " + value);
    }
    if (value == null) {
    	er.neo4jadaptor.test.eo.SecondEntity oldValue = secondEntity();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "secondEntity");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "secondEntity");
    }
  }
  

  public static Join createJoin(EOEditingContext editingContext, er.neo4jadaptor.test.eo.FirstEntity firstEntity, er.neo4jadaptor.test.eo.SecondEntity secondEntity) {
    Join eo = (Join) EOUtilities.createAndInsertInstance(editingContext, _Join.ENTITY_NAME);    
    eo.setFirstEntityRelationship(firstEntity);
    eo.setSecondEntityRelationship(secondEntity);
    return eo;
  }

  public static NSArray<Join> fetchAllJoins(EOEditingContext editingContext) {
    return _Join.fetchAllJoins(editingContext, null);
  }

  public static NSArray<Join> fetchAllJoins(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Join.fetchJoins(editingContext, null, sortOrderings);
  }

  public static NSArray<Join> fetchJoins(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_Join.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Join> eoObjects = (NSArray<Join>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static Join fetchJoin(EOEditingContext editingContext, String keyName, Object value) {
    return _Join.fetchJoin(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Join fetchJoin(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Join> eoObjects = _Join.fetchJoins(editingContext, qualifier, null);
    Join eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (Join)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Join that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Join fetchRequiredJoin(EOEditingContext editingContext, String keyName, Object value) {
    return _Join.fetchRequiredJoin(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Join fetchRequiredJoin(EOEditingContext editingContext, EOQualifier qualifier) {
    Join eoObject = _Join.fetchJoin(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Join that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Join localInstanceIn(EOEditingContext editingContext, Join eo) {
    Join localInstance = (eo == null) ? null : (Join)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
