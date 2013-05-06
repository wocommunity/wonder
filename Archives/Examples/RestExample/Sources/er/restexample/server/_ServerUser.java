// DO NOT EDIT.  Make changes to ServerUser.java instead.
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

  // Attribute Keys
  public static final ERXKey<String> NAME = new ERXKey<String>("name");
  // Relationship Keys
  public static final ERXKey<er.restexample.server.ServerPost> POSTS = new ERXKey<er.restexample.server.ServerPost>("posts");
  public static final ERXKey<er.restexample.server.ServerTopic> TOPICS = new ERXKey<er.restexample.server.ServerTopic>("topics");

  // Attributes
  public static final String NAME_KEY = NAME.key();
  // Relationships
  public static final String POSTS_KEY = POSTS.key();
  public static final String TOPICS_KEY = TOPICS.key();

  private static Logger LOG = Logger.getLogger(_ServerUser.class);

  public ServerUser localInstanceIn(EOEditingContext editingContext) {
    ServerUser localInstance = (ServerUser)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String name() {
    return (String) storedValueForKey(_ServerUser.NAME_KEY);
  }

  public void setName(String value) {
    if (_ServerUser.LOG.isDebugEnabled()) {
    	_ServerUser.LOG.debug( "updating name from " + name() + " to " + value);
    }
    takeStoredValueForKey(value, _ServerUser.NAME_KEY);
  }

  public NSArray<er.restexample.server.ServerPost> posts() {
    return (NSArray<er.restexample.server.ServerPost>)storedValueForKey(_ServerUser.POSTS_KEY);
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
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
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
    includeObjectIntoPropertyWithKey(object, _ServerUser.POSTS_KEY);
  }

  public void removeFromPosts(er.restexample.server.ServerPost object) {
    excludeObjectFromPropertyWithKey(object, _ServerUser.POSTS_KEY);
  }

  public void addToPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerUser.LOG.isDebugEnabled()) {
      _ServerUser.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ServerUser.POSTS_KEY);
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
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerUser.POSTS_KEY);
    }
  }

  public er.restexample.server.ServerPost createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.restexample.server.ServerPost.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ServerUser.POSTS_KEY);
    return (er.restexample.server.ServerPost) eo;
  }

  public void deletePostsRelationship(er.restexample.server.ServerPost object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerUser.POSTS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration<er.restexample.server.ServerPost> objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship(objects.nextElement());
    }
  }

  public NSArray<er.restexample.server.ServerTopic> topics() {
    return (NSArray<er.restexample.server.ServerTopic>)storedValueForKey(_ServerUser.TOPICS_KEY);
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
        NSMutableArray<EOQualifier> qualifiers = new NSMutableArray<EOQualifier>();
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
    includeObjectIntoPropertyWithKey(object, _ServerUser.TOPICS_KEY);
  }

  public void removeFromTopics(er.restexample.server.ServerTopic object) {
    excludeObjectFromPropertyWithKey(object, _ServerUser.TOPICS_KEY);
  }

  public void addToTopicsRelationship(er.restexample.server.ServerTopic object) {
    if (_ServerUser.LOG.isDebugEnabled()) {
      _ServerUser.LOG.debug("adding " + object + " to topics relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToTopics(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ServerUser.TOPICS_KEY);
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
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerUser.TOPICS_KEY);
    }
  }

  public er.restexample.server.ServerTopic createTopicsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.restexample.server.ServerTopic.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ServerUser.TOPICS_KEY);
    return (er.restexample.server.ServerTopic) eo;
  }

  public void deleteTopicsRelationship(er.restexample.server.ServerTopic object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerUser.TOPICS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllTopicsRelationships() {
    Enumeration<er.restexample.server.ServerTopic> objects = topics().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteTopicsRelationship(objects.nextElement());
    }
  }


  public static ServerUser createServerUser(EOEditingContext editingContext, String name
) {
    ServerUser eo = (ServerUser) EOUtilities.createAndInsertInstance(editingContext, _ServerUser.ENTITY_NAME);    
		eo.setName(name);
    return eo;
  }

  public static ERXFetchSpecification<ServerUser> fetchSpec() {
    return new ERXFetchSpecification<ServerUser>(_ServerUser.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ServerUser> fetchAllServerUsers(EOEditingContext editingContext) {
    return _ServerUser.fetchAllServerUsers(editingContext, null);
  }

  public static NSArray<ServerUser> fetchAllServerUsers(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ServerUser.fetchServerUsers(editingContext, null, sortOrderings);
  }

  public static NSArray<ServerUser> fetchServerUsers(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ServerUser> fetchSpec = new ERXFetchSpecification<ServerUser>(_ServerUser.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ServerUser> eoObjects = fetchSpec.fetchObjects(editingContext);
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
      eoObject = eoObjects.objectAtIndex(0);
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
    ServerUser localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
