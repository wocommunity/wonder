// DO NOT EDIT.  Make changes to ServerTopic.java instead.
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
public abstract class _ServerTopic extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "ServerTopic";

  // Attribute Keys
  public static final ERXKey<String> TITLE = new ERXKey<String>("title");
  // Relationship Keys
  public static final ERXKey<er.restexample.server.ServerForum> FORUM = new ERXKey<er.restexample.server.ServerForum>("forum");
  public static final ERXKey<er.restexample.server.ServerPost> POSTS = new ERXKey<er.restexample.server.ServerPost>("posts");
  public static final ERXKey<er.restexample.server.ServerUser> USER = new ERXKey<er.restexample.server.ServerUser>("user");

  // Attributes
  public static final String TITLE_KEY = TITLE.key();
  // Relationships
  public static final String FORUM_KEY = FORUM.key();
  public static final String POSTS_KEY = POSTS.key();
  public static final String USER_KEY = USER.key();

  private static Logger LOG = Logger.getLogger(_ServerTopic.class);

  public ServerTopic localInstanceIn(EOEditingContext editingContext) {
    ServerTopic localInstance = (ServerTopic)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String title() {
    return (String) storedValueForKey(_ServerTopic.TITLE_KEY);
  }

  public void setTitle(String value) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
    	_ServerTopic.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, _ServerTopic.TITLE_KEY);
  }

  public er.restexample.server.ServerForum forum() {
    return (er.restexample.server.ServerForum)storedValueForKey(_ServerTopic.FORUM_KEY);
  }
  
  public void setForum(er.restexample.server.ServerForum value) {
    takeStoredValueForKey(value, _ServerTopic.FORUM_KEY);
  }

  public void setForumRelationship(er.restexample.server.ServerForum value) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
      _ServerTopic.LOG.debug("updating forum from " + forum() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setForum(value);
    }
    else if (value == null) {
    	er.restexample.server.ServerForum oldValue = forum();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _ServerTopic.FORUM_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _ServerTopic.FORUM_KEY);
    }
  }
  
  public er.restexample.server.ServerUser user() {
    return (er.restexample.server.ServerUser)storedValueForKey(_ServerTopic.USER_KEY);
  }
  
  public void setUser(er.restexample.server.ServerUser value) {
    takeStoredValueForKey(value, _ServerTopic.USER_KEY);
  }

  public void setUserRelationship(er.restexample.server.ServerUser value) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
      _ServerTopic.LOG.debug("updating user from " + user() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setUser(value);
    }
    else if (value == null) {
    	er.restexample.server.ServerUser oldValue = user();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _ServerTopic.USER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _ServerTopic.USER_KEY);
    }
  }
  
  public NSArray<er.restexample.server.ServerPost> posts() {
    return (NSArray<er.restexample.server.ServerPost>)storedValueForKey(_ServerTopic.POSTS_KEY);
  }

  public NSArray<er.restexample.server.ServerPost> posts(EOQualifier qualifier) {
    return posts(qualifier, null);
  }

  public NSArray<er.restexample.server.ServerPost> posts(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<er.restexample.server.ServerPost> results;
      results = posts();
      if (qualifier != null) {
        results = (NSArray<er.restexample.server.ServerPost>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<er.restexample.server.ServerPost>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToPosts(er.restexample.server.ServerPost object) {
    includeObjectIntoPropertyWithKey(object, _ServerTopic.POSTS_KEY);
  }

  public void removeFromPosts(er.restexample.server.ServerPost object) {
    excludeObjectFromPropertyWithKey(object, _ServerTopic.POSTS_KEY);
  }

  public void addToPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
      _ServerTopic.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _ServerTopic.POSTS_KEY);
    }
  }

  public void removeFromPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
      _ServerTopic.LOG.debug("removing " + object + " from posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPosts(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerTopic.POSTS_KEY);
    }
  }

  public er.restexample.server.ServerPost createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( er.restexample.server.ServerPost.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _ServerTopic.POSTS_KEY);
    return (er.restexample.server.ServerPost) eo;
  }

  public void deletePostsRelationship(er.restexample.server.ServerPost object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _ServerTopic.POSTS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration<er.restexample.server.ServerPost> objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship(objects.nextElement());
    }
  }


  public static ServerTopic createServerTopic(EOEditingContext editingContext, String title
, er.restexample.server.ServerForum forum, er.restexample.server.ServerUser user) {
    ServerTopic eo = (ServerTopic) EOUtilities.createAndInsertInstance(editingContext, _ServerTopic.ENTITY_NAME);    
		eo.setTitle(title);
    eo.setForumRelationship(forum);
    eo.setUserRelationship(user);
    return eo;
  }

  public static ERXFetchSpecification<ServerTopic> fetchSpec() {
    return new ERXFetchSpecification<ServerTopic>(_ServerTopic.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<ServerTopic> fetchAllServerTopics(EOEditingContext editingContext) {
    return _ServerTopic.fetchAllServerTopics(editingContext, null);
  }

  public static NSArray<ServerTopic> fetchAllServerTopics(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ServerTopic.fetchServerTopics(editingContext, null, sortOrderings);
  }

  public static NSArray<ServerTopic> fetchServerTopics(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<ServerTopic> fetchSpec = new ERXFetchSpecification<ServerTopic>(_ServerTopic.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ServerTopic> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static ServerTopic fetchServerTopic(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerTopic.fetchServerTopic(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerTopic fetchServerTopic(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<ServerTopic> eoObjects = _ServerTopic.fetchServerTopics(editingContext, qualifier, null);
    ServerTopic eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one ServerTopic that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerTopic fetchRequiredServerTopic(EOEditingContext editingContext, String keyName, Object value) {
    return _ServerTopic.fetchRequiredServerTopic(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static ServerTopic fetchRequiredServerTopic(EOEditingContext editingContext, EOQualifier qualifier) {
    ServerTopic eoObject = _ServerTopic.fetchServerTopic(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no ServerTopic that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static ServerTopic localInstanceIn(EOEditingContext editingContext, ServerTopic eo) {
    ServerTopic localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
