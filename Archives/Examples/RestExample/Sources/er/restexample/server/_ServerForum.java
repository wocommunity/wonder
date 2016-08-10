// DO NOT EDIT.  Make changes to ServerForum.java instead.
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
public abstract class _ServerForum extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "ServerForum";

  // Attribute Keys
  public static final ERXKey<String> TITLE = new ERXKey<String>("title");
  // Relationship Keys
  public static final ERXKey<er.restexample.server.ServerPost> POSTS = new ERXKey<er.restexample.server.ServerPost>("posts");
  public static final ERXKey<er.restexample.server.ServerTopic> TOPICS = new ERXKey<er.restexample.server.ServerTopic>("topics");

  // Attributes
  public static final String TITLE_KEY = TITLE.key();
  // Relationships
  public static final String POSTS_KEY = POSTS.key();
  public static final String TOPICS_KEY = TOPICS.key();

  private static Logger LOG = Logger.getLogger(_ServerForum.class);

  public ServerForum localInstanceIn(EOEditingContext editingContext) {
    ServerForum localInstance = (ServerForum)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String title() {
    return (String) storedValueForKey(_ServerForum.TITLE_KEY);
  }

  public void setTitle(String value) {
    if (_ServerForum.LOG.isDebugEnabled()) {
    	_ServerForum.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, _ServerForum.TITLE_KEY);
  }

  public NSArray<er.restexample.server.ServerPost> posts() {
    return (NSArray<er.restexample.server.ServerPost>)storedValueForKey(_ServerForum.POSTS_KEY);
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
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.restexample.server.ServerPost.FORUM_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
    includeObjectIntoPropertyWithKey(object, _ServerForum.POSTS_KEY);
  }

  public void removeFromPosts(er.restexample.server.ServerPost object) {
    excludeObjectFromPropertyWithKey(object, _ServerForum.POSTS_KEY);
  }

  public void addToPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerForum.LOG.isDebugEnabled()) {
      _ServerForum.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ServerForum.POSTS_KEY);
    }
  }

  public void removeFromPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerForum.LOG.isDebugEnabled()) {
      _ServerForum.LOG.debug("removing " + object + " from posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPosts(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerForum.POSTS_KEY);
    }
  }

  public er.restexample.server.ServerPost createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.restexample.server.ServerPost.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ServerForum.POSTS_KEY);
    return (er.restexample.server.ServerPost) eo;
  }

  public void deletePostsRelationship(er.restexample.server.ServerPost object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerForum.POSTS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration<er.restexample.server.ServerPost> objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship(objects.nextElement());
    }
  }

  public NSArray<er.restexample.server.ServerTopic> topics() {
    return (NSArray<er.restexample.server.ServerTopic>)storedValueForKey(_ServerForum.TOPICS_KEY);
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
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.restexample.server.ServerTopic.FORUM_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
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
    includeObjectIntoPropertyWithKey(object, _ServerForum.TOPICS_KEY);
  }

  public void removeFromTopics(er.restexample.server.ServerTopic object) {
    excludeObjectFromPropertyWithKey(object, _ServerForum.TOPICS_KEY);
  }

  public void addToTopicsRelationship(er.restexample.server.ServerTopic object) {
    if (_ServerForum.LOG.isDebugEnabled()) {
      _ServerForum.LOG.debug("adding " + object + " to topics relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToTopics(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ServerForum.TOPICS_KEY);
    }
  }

  public void removeFromTopicsRelationship(er.restexample.server.ServerTopic object) {
    if (_ServerForum.LOG.isDebugEnabled()) {
      _ServerForum.LOG.debug("removing " + object + " from topics relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromTopics(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerForum.TOPICS_KEY);
    }
  }

  public er.restexample.server.ServerTopic createTopicsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.restexample.server.ServerTopic.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ServerForum.TOPICS_KEY);
    return (er.restexample.server.ServerTopic) eo;
  }

  public void deleteTopicsRelationship(er.restexample.server.ServerTopic object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerForum.TOPICS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllTopicsRelationships() {
    Enumeration<er.restexample.server.ServerTopic> objects = topics().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteTopicsRelationship(objects.nextElement());
    }
  }


  public static ServerForum createServerForum(EOEditingContext editingContext, String title
) {
    ServerForum eo = (ServerForum) EOUtilities.createAndInsertInstance(editingContext, _ServerForum.ENTITY_NAME);    
		eo.setTitle(title);
    return eo;
  }

  public static ERXFetchSpecification<ServerForum> fetchSpec() {
    return new ERXFetchSpecification<ServerForum>(_ServerForum.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ServerForum> fetchAllServerForums(EOEditingContext editingContext) {
    return _ServerForum.fetchAllServerForums(editingContext, null);
  }

  public static NSArray<ServerForum> fetchAllServerForums(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ServerForum.fetchServerForums(editingContext, null, sortOrderings);
  }

  public static NSArray<ServerForum> fetchServerForums(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ServerForum> fetchSpec = new ERXFetchSpecification<ServerForum>(_ServerForum.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ServerForum> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ServerForum fetchServerForum(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerForum.fetchServerForum(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerForum fetchServerForum(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ServerForum> eoObjects = _ServerForum.fetchServerForums(editingContext, qualifier, null);
    ServerForum eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ServerForum that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerForum fetchRequiredServerForum(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerForum.fetchRequiredServerForum(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerForum fetchRequiredServerForum(EOEditingContext editingContext, EOQualifier qualifier) {
    ServerForum eoObject = _ServerForum.fetchServerForum(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ServerForum that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerForum localInstanceIn(EOEditingContext editingContext, ServerForum eo) {
    ServerForum localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
