// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to ServerTopic.java instead.
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

	// Attributes
	public static final String TITLE_KEY = "title";
	public static final ERXKey<String> TITLE = new ERXKey<String>(TITLE_KEY);

	// Relationships
	public static final String FORUM_KEY = "forum";
	public static final ERXKey<er.restexample.server.ServerForum> FORUM = new ERXKey<er.restexample.server.ServerForum>(FORUM_KEY);
	public static final String POSTS_KEY = "posts";
	public static final ERXKey<er.restexample.server.ServerPost> POSTS = new ERXKey<er.restexample.server.ServerPost>(POSTS_KEY);
	public static final String USER_KEY = "user";
	public static final ERXKey<er.restexample.server.ServerUser> USER = new ERXKey<er.restexample.server.ServerUser>(USER_KEY);

  private static Logger LOG = Logger.getLogger(_ServerTopic.class);

  public ServerTopic localInstanceIn(EOEditingContext editingContext) {
    ServerTopic localInstance = (ServerTopic)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String title() {
    return (String) storedValueForKey("title");
  }

  public void setTitle(String value) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
    	_ServerTopic.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, "title");
  }

  public er.restexample.server.ServerForum forum() {
    return (er.restexample.server.ServerForum)storedValueForKey("forum");
  }
  
  public void setForum(er.restexample.server.ServerForum value) {
    takeStoredValueForKey(value, "forum");
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
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "forum");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "forum");
    }
  }
  
  public er.restexample.server.ServerUser user() {
    return (er.restexample.server.ServerUser)storedValueForKey("user");
  }
  
  public void setUser(er.restexample.server.ServerUser value) {
    takeStoredValueForKey(value, "user");
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
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, "user");
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, "user");
    }
  }
  
  public NSArray<er.restexample.server.ServerPost> posts() {
    return (NSArray<er.restexample.server.ServerPost>)storedValueForKey("posts");
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
    includeObjectIntoPropertyWithKey(object, "posts");
  }

  public void removeFromPosts(er.restexample.server.ServerPost object) {
    excludeObjectFromPropertyWithKey(object, "posts");
  }

  public void addToPostsRelationship(er.restexample.server.ServerPost object) {
    if (_ServerTopic.LOG.isDebugEnabled()) {
      _ServerTopic.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "posts");
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


  public static ServerTopic createServerTopic(EOEditingContext editingContext, String title
, er.restexample.server.ServerForum forum, er.restexample.server.ServerUser user) {
    ServerTopic eo = (ServerTopic) EOUtilities.createAndInsertInstance(editingContext, _ServerTopic.ENTITY_NAME);    
		eo.setTitle(title);
    eo.setForumRelationship(forum);
    eo.setUserRelationship(user);
    return eo;
  }

  public static NSArray<ServerTopic> fetchAllServerTopics(EOEditingContext editingContext) {
    return _ServerTopic.fetchAllServerTopics(editingContext, null);
  }

  public static NSArray<ServerTopic> fetchAllServerTopics(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _ServerTopic.fetchServerTopics(editingContext, null, sortOrderings);
  }

  public static NSArray<ServerTopic> fetchServerTopics(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_ServerTopic.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<ServerTopic> eoObjects = (NSArray<ServerTopic>)editingContext.objectsWithFetchSpecification(fetchSpec);
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
      eoObject = (ServerTopic)eoObjects.objectAtIndex(0);
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
    ServerTopic localInstance = (eo == null) ? null : (ServerTopic)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
