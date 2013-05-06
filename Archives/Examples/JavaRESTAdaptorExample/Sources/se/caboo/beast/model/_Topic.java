// DO NOT EDIT.  Make changes to Topic.java instead.
package se.caboo.beast.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.eof.*;
import er.extensions.foundation.*;

@SuppressWarnings("all")
public abstract class _Topic extends  ERXGenericRecord {
  public static final String ENTITY_NAME = "Topic";

  // Attribute Keys
  public static final ERXKey<NSTimestamp> CREATED_AT = new ERXKey<NSTimestamp>("createdAt");
  public static final ERXKey<Integer> HITS = new ERXKey<Integer>("hits");
  public static final ERXKey<Boolean> LOCKED = new ERXKey<Boolean>("locked");
  public static final ERXKey<Integer> POSTS_COUNT = new ERXKey<Integer>("postsCount");
  public static final ERXKey<NSTimestamp> REPLIED_AT = new ERXKey<NSTimestamp>("repliedAt");
  public static final ERXKey<Integer> STICKY = new ERXKey<Integer>("sticky");
  public static final ERXKey<String> TITLE = new ERXKey<String>("title");
  public static final ERXKey<NSTimestamp> UPDATED_AT = new ERXKey<NSTimestamp>("updatedAt");
  // Relationship Keys
  public static final ERXKey<se.caboo.beast.model.Forum> FORUM = new ERXKey<se.caboo.beast.model.Forum>("forum");
  public static final ERXKey<se.caboo.beast.model.Post> POSTS = new ERXKey<se.caboo.beast.model.Post>("posts");
  public static final ERXKey<se.caboo.beast.model.User> REPLIED_BY = new ERXKey<se.caboo.beast.model.User>("repliedBy");
  public static final ERXKey<se.caboo.beast.model.User> USER = new ERXKey<se.caboo.beast.model.User>("user");

  // Attributes
  public static final String CREATED_AT_KEY = CREATED_AT.key();
  public static final String HITS_KEY = HITS.key();
  public static final String LOCKED_KEY = LOCKED.key();
  public static final String POSTS_COUNT_KEY = POSTS_COUNT.key();
  public static final String REPLIED_AT_KEY = REPLIED_AT.key();
  public static final String STICKY_KEY = STICKY.key();
  public static final String TITLE_KEY = TITLE.key();
  public static final String UPDATED_AT_KEY = UPDATED_AT.key();
  // Relationships
  public static final String FORUM_KEY = FORUM.key();
  public static final String POSTS_KEY = POSTS.key();
  public static final String REPLIED_BY_KEY = REPLIED_BY.key();
  public static final String USER_KEY = USER.key();

  private static Logger LOG = Logger.getLogger(_Topic.class);

  public Topic localInstanceIn(EOEditingContext editingContext) {
    Topic localInstance = (Topic)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public NSTimestamp createdAt() {
    return (NSTimestamp) storedValueForKey(_Topic.CREATED_AT_KEY);
  }

  public void setCreatedAt(NSTimestamp value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating createdAt from " + createdAt() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.CREATED_AT_KEY);
  }

  public Integer hits() {
    return (Integer) storedValueForKey(_Topic.HITS_KEY);
  }

  public void setHits(Integer value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating hits from " + hits() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.HITS_KEY);
  }

  public Boolean locked() {
    return (Boolean) storedValueForKey(_Topic.LOCKED_KEY);
  }

  public void setLocked(Boolean value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating locked from " + locked() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.LOCKED_KEY);
  }

  public Integer postsCount() {
    return (Integer) storedValueForKey(_Topic.POSTS_COUNT_KEY);
  }

  public void setPostsCount(Integer value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating postsCount from " + postsCount() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.POSTS_COUNT_KEY);
  }

  public NSTimestamp repliedAt() {
    return (NSTimestamp) storedValueForKey(_Topic.REPLIED_AT_KEY);
  }

  public void setRepliedAt(NSTimestamp value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating repliedAt from " + repliedAt() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.REPLIED_AT_KEY);
  }

  public Integer sticky() {
    return (Integer) storedValueForKey(_Topic.STICKY_KEY);
  }

  public void setSticky(Integer value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating sticky from " + sticky() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.STICKY_KEY);
  }

  public String title() {
    return (String) storedValueForKey(_Topic.TITLE_KEY);
  }

  public void setTitle(String value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating title from " + title() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.TITLE_KEY);
  }

  public NSTimestamp updatedAt() {
    return (NSTimestamp) storedValueForKey(_Topic.UPDATED_AT_KEY);
  }

  public void setUpdatedAt(NSTimestamp value) {
    if (_Topic.LOG.isDebugEnabled()) {
    	_Topic.LOG.debug( "updating updatedAt from " + updatedAt() + " to " + value);
    }
    takeStoredValueForKey(value, _Topic.UPDATED_AT_KEY);
  }

  public se.caboo.beast.model.Forum forum() {
    return (se.caboo.beast.model.Forum)storedValueForKey(_Topic.FORUM_KEY);
  }
  
  public void setForum(se.caboo.beast.model.Forum value) {
    takeStoredValueForKey(value, _Topic.FORUM_KEY);
  }

  public void setForumRelationship(se.caboo.beast.model.Forum value) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("updating forum from " + forum() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setForum(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.Forum oldValue = forum();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Topic.FORUM_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Topic.FORUM_KEY);
    }
  }
  
  public se.caboo.beast.model.User repliedBy() {
    return (se.caboo.beast.model.User)storedValueForKey(_Topic.REPLIED_BY_KEY);
  }
  
  public void setRepliedBy(se.caboo.beast.model.User value) {
    takeStoredValueForKey(value, _Topic.REPLIED_BY_KEY);
  }

  public void setRepliedByRelationship(se.caboo.beast.model.User value) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("updating repliedBy from " + repliedBy() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setRepliedBy(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.User oldValue = repliedBy();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Topic.REPLIED_BY_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Topic.REPLIED_BY_KEY);
    }
  }
  
  public se.caboo.beast.model.User user() {
    return (se.caboo.beast.model.User)storedValueForKey(_Topic.USER_KEY);
  }
  
  public void setUser(se.caboo.beast.model.User value) {
    takeStoredValueForKey(value, _Topic.USER_KEY);
  }

  public void setUserRelationship(se.caboo.beast.model.User value) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("updating user from " + user() + " to " + value);
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	setUser(value);
    }
    else if (value == null) {
    	se.caboo.beast.model.User oldValue = user();
    	if (oldValue != null) {
    		removeObjectFromBothSidesOfRelationshipWithKey(oldValue, _Topic.USER_KEY);
      }
    } else {
    	addObjectToBothSidesOfRelationshipWithKey(value, _Topic.USER_KEY);
    }
  }
  
  public NSArray<se.caboo.beast.model.Post> posts() {
    return (NSArray<se.caboo.beast.model.Post>)storedValueForKey(_Topic.POSTS_KEY);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier) {
    return posts(qualifier, null);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<se.caboo.beast.model.Post> results;
      results = posts();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    return results;
  }
  
  public void addToPosts(se.caboo.beast.model.Post object) {
    includeObjectIntoPropertyWithKey(object, _Topic.POSTS_KEY);
  }

  public void removeFromPosts(se.caboo.beast.model.Post object) {
    excludeObjectFromPropertyWithKey(object, _Topic.POSTS_KEY);
  }

  public void addToPostsRelationship(se.caboo.beast.model.Post object) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, _Topic.POSTS_KEY);
    }
  }

  public void removeFromPostsRelationship(se.caboo.beast.model.Post object) {
    if (_Topic.LOG.isDebugEnabled()) {
      _Topic.LOG.debug("removing " + object + " from posts relationship");
    }
    if (er.extensions.eof.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPosts(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, _Topic.POSTS_KEY);
    }
  }

  public se.caboo.beast.model.Post createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName( se.caboo.beast.model.Post.ENTITY_NAME );
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, _Topic.POSTS_KEY);
    return (se.caboo.beast.model.Post) eo;
  }

  public void deletePostsRelationship(se.caboo.beast.model.Post object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, _Topic.POSTS_KEY);
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration<se.caboo.beast.model.Post> objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship(objects.nextElement());
    }
  }


  public static Topic createTopic(EOEditingContext editingContext, NSTimestamp createdAt
, Integer hits
, Boolean locked
, Integer postsCount
, NSTimestamp repliedAt
, Integer sticky
, String title
, NSTimestamp updatedAt
, se.caboo.beast.model.Forum forum, se.caboo.beast.model.User repliedBy, se.caboo.beast.model.User user) {
    Topic eo = (Topic) EOUtilities.createAndInsertInstance(editingContext, _Topic.ENTITY_NAME);    
		eo.setCreatedAt(createdAt);
		eo.setHits(hits);
		eo.setLocked(locked);
		eo.setPostsCount(postsCount);
		eo.setRepliedAt(repliedAt);
		eo.setSticky(sticky);
		eo.setTitle(title);
		eo.setUpdatedAt(updatedAt);
    eo.setForumRelationship(forum);
    eo.setRepliedByRelationship(repliedBy);
    eo.setUserRelationship(user);
    return eo;
  }

  public static ERXFetchSpecification<Topic> fetchSpec() {
    return new ERXFetchSpecification<Topic>(_Topic.ENTITY_NAME, null, null, false, true, null);
  }

  public static NSArray<Topic> fetchAllTopics(EOEditingContext editingContext) {
    return _Topic.fetchAllTopics(editingContext, null);
  }

  public static NSArray<Topic> fetchAllTopics(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _Topic.fetchTopics(editingContext, null, sortOrderings);
  }

  public static NSArray<Topic> fetchTopics(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    ERXFetchSpecification<Topic> fetchSpec = new ERXFetchSpecification<Topic>(_Topic.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<Topic> eoObjects = fetchSpec.fetchObjects(editingContext);
    return eoObjects;
  }

  public static Topic fetchTopic(EOEditingContext editingContext, String keyName, Object value) {
    return _Topic.fetchTopic(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Topic fetchTopic(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<Topic> eoObjects = _Topic.fetchTopics(editingContext, qualifier, null);
    Topic eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one Topic that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Topic fetchRequiredTopic(EOEditingContext editingContext, String keyName, Object value) {
    return _Topic.fetchRequiredTopic(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static Topic fetchRequiredTopic(EOEditingContext editingContext, EOQualifier qualifier) {
    Topic eoObject = _Topic.fetchTopic(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no Topic that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static Topic localInstanceIn(EOEditingContext editingContext, Topic eo) {
    Topic localInstance = (eo == null) ? null : ERXEOControlUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
