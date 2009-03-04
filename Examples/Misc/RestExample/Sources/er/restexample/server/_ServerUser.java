// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ServerUser.java instead.
package er.restexample.server;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _ServerUser extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "ServerUser";

	// Attributes
	public static final String NAME_KEY = "name";
	public static final ERXKey<String> NAME = new ERXKey<String>(NAME_KEY);

	// Relationships
	public static final String POSTS_KEY = "posts";
	public static final ERXKey<er.restexample.server.ServerPost> POSTS = new ERXKey<er.restexample.server.ServerPost>(POSTS_KEY);
	public static final String TOPICS_KEY = "topics";
	public static final ERXKey<er.restexample.server.ServerTopic> TOPICS = new ERXKey<er.restexample.server.ServerTopic>(TOPICS_KEY);

  private static Logger LOG = Logger.getLogger(_ServerUser.class);

  public ServerUser localInstanceIn(EOEditingContext editingContext) {
    ServerUser localInstance = (ServerUser)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey("name");
  }

  public void setName(String value) {
    if (_ServerUser.LOG.isDebugEnabled()) {
    	_ServerUser.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, "name");
  }

  public NSArray<er.restexample.server.ServerPost> posts() {
    return (NSArray<er.restexample.server.ServerPost>)storedValueForKey("posts");
  }

  public NSArray<er.restexample.server.ServerPost> posts(EOQualifier qualifier) {
    return posts(qualifier, null, false);
  }

  public NSArray<er.restexample.server.ServerPost> posts(EOQualifier qualifier, boolean fetch) {
    return posts(qualifier, null, fetch);
  }

  public NSArray<er.restexample.server.ServerPost> posts(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.restexample.server.ServerPost> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.restexample.server.ServerPost.USER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.restexample.server.ServerPost.fetchServerPosts(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = posts();
      if (qualifier != null) {
        results = (NSArray<er.restexample.server.ServerPost>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.restexample.server.ServerPost>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToPosts(er.restexample.server.ServerPost object) {
    includeObjectIntoPropertyWithKey(object, "posts");
  }

  public void removeFromPosts(er.restexample.server.ServerPost object) {
    excludeObjectFromPropertyWithKey(object, "posts");
  }

  public void addToPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerUser.LOG.isDebugEnabled()) {
      _ServerUser.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public void removeFromPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerUser.LOG.isDebugEnabled()) {
      _ServerUser.LOG.debug("removing " + object + " from posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPosts(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public er.restexample.server.ServerPost createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("ServerPost");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "posts");
    return (er.restexample.server.ServerPost) eo;
  }

  public void deletePostsRelationship(er.restexample.server.ServerPost object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "posts");
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship((er.restexample.server.ServerPost)objects.nextElement());
    }
  }

  public NSArray<er.restexample.server.ServerTopic> topics() {
    return (NSArray<er.restexample.server.ServerTopic>)storedValueForKey("topics");
  }

  public NSArray<er.restexample.server.ServerTopic> topics(EOQualifier qualifier) {
    return topics(qualifier, null, false);
  }

  public NSArray<er.restexample.server.ServerTopic> topics(EOQualifier qualifier, boolean fetch) {
    return topics(qualifier, null, fetch);
  }

  public NSArray<er.restexample.server.ServerTopic> topics(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<er.restexample.server.ServerTopic> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.restexample.server.ServerTopic.USER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = er.restexample.server.ServerTopic.fetchServerTopics(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = topics();
      if (qualifier != null) {
        results = (NSArray<er.restexample.server.ServerTopic>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.restexample.server.ServerTopic>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToTopics(er.restexample.server.ServerTopic object) {
    includeObjectIntoPropertyWithKey(object, "topics");
  }

  public void removeFromTopics(er.restexample.server.ServerTopic object) {
    excludeObjectFromPropertyWithKey(object, "topics");
  }

  public void addToTopicsRelationship(er.restexample.server.ServerTopic object) {
    if (_ServerUser.LOG.isDebugEnabled()) {
      _ServerUser.LOG.debug("adding " + object + " to topics relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToTopics(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "topics");
    }
  }

  public void removeFromTopicsRelationship(er.restexample.server.ServerTopic object) {
    if (_ServerUser.LOG.isDebugEnabled()) {
      _ServerUser.LOG.debug("removing " + object + " from topics relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromTopics(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "topics");
    }
  }

  public er.restexample.server.ServerTopic createTopicsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("ServerTopic");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "topics");
    return (er.restexample.server.ServerTopic) eo;
  }

  public void deleteTopicsRelationship(er.restexample.server.ServerTopic object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "topics");
    editingContext().deleteObject(object);
  }

  public void deleteAllTopicsRelationships() {
    Enumeration objects = topics().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteTopicsRelationship((er.restexample.server.ServerTopic)objects.nextElement());
    }
  }


  public static ServerUser createServerUser(EOEditingContext editingContext, String name
) {
    ServerUser eo = (ServerUser) EOUtilities.createAndInsertInstance(editingContext, _ServerUser.ENTITY_NAME);    
		eo.setName(name);
    return eo;
  }

  public static NSArray<ServerUser> fetchAllServerUsers(EOEditingContext editingContext) {
    return _ServerUser.fetchAllServerUsers(editingContext, null);
  }

  public static NSArray<ServerUser> fetchAllServerUsers(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ServerUser.fetchServerUsers(editingContext, null, sortOrderings);
  }

  public static NSArray<ServerUser> fetchServerUsers(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_ServerUser.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ServerUser> eoObjects = (NSArray<ServerUser>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static ServerUser fetchServerUser(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerUser.fetchServerUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerUser fetchServerUser(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ServerUser> eoObjects = _ServerUser.fetchServerUsers(editingContext, qualifier, null);
    ServerUser eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (ServerUser)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ServerUser that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerUser fetchRequiredServerUser(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerUser.fetchRequiredServerUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerUser fetchRequiredServerUser(EOEditingContext editingContext, EOQualifier qualifier) {
    ServerUser eoObject = _ServerUser.fetchServerUser(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ServerUser that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerUser localInstanceIn(EOEditingContext editingContext, ServerUser eo) {
    ServerUser localInstance = (eo == null) ? null : (ServerUser)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
