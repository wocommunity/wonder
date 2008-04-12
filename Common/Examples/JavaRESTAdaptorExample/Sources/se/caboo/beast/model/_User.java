// $LastChangedRevision: 4733 $ DO NOT EDIT.  Make changes to User.java instead.
package se.caboo.beast.model;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.math.*;
import java.util.*;
import org.apache.log4j.Logger;

import er.extensions.ERXGenericRecord;
import er.extensions.ERXKey;

@SuppressWarnings("all")
public abstract class _User extends  ERXGenericRecord {
	public static final String ENTITY_NAME = "User";

	// Attributes
	public static final String BIO_KEY = "bio";
	public static final ERXKey<String> BIO = new ERXKey<String>(BIO_KEY);
	public static final String BIO_HTML_KEY = "bioHtml";
	public static final ERXKey<String> BIO_HTML = new ERXKey<String>(BIO_HTML_KEY);
	public static final String CREATED_AT_KEY = "createdAt";
	public static final ERXKey<NSTimestamp> CREATED_AT = new ERXKey<NSTimestamp>(CREATED_AT_KEY);
	public static final String DISPLAY_NAME_KEY = "displayName";
	public static final ERXKey<String> DISPLAY_NAME = new ERXKey<String>(DISPLAY_NAME_KEY);
	public static final String LAST_LOGIN_AT_KEY = "lastLoginAt";
	public static final ERXKey<NSTimestamp> LAST_LOGIN_AT = new ERXKey<NSTimestamp>(LAST_LOGIN_AT_KEY);
	public static final String LAST_SEEN_AT_KEY = "lastSeenAt";
	public static final ERXKey<NSTimestamp> LAST_SEEN_AT = new ERXKey<NSTimestamp>(LAST_SEEN_AT_KEY);
	public static final String LOGIN_KEY = "login";
	public static final ERXKey<String> LOGIN = new ERXKey<String>(LOGIN_KEY);
	public static final String POSTS_COUNT_KEY = "postsCount";
	public static final ERXKey<Integer> POSTS_COUNT = new ERXKey<Integer>(POSTS_COUNT_KEY);
	public static final String UPDATED_AT_KEY = "updatedAt";
	public static final ERXKey<NSTimestamp> UPDATED_AT = new ERXKey<NSTimestamp>(UPDATED_AT_KEY);
	public static final String WEBSITE_KEY = "website";
	public static final ERXKey<String> WEBSITE = new ERXKey<String>(WEBSITE_KEY);

	// Relationships
	public static final String POSTS_KEY = "posts";
	public static final ERXKey<se.caboo.beast.model.Post> POSTS = new ERXKey<se.caboo.beast.model.Post>(POSTS_KEY);
	public static final String REPLIED_TO_TOPICS_KEY = "repliedToTopics";
	public static final ERXKey<se.caboo.beast.model.Topic> REPLIED_TO_TOPICS = new ERXKey<se.caboo.beast.model.Topic>(REPLIED_TO_TOPICS_KEY);
	public static final String TOPICS_KEY = "topics";
	public static final ERXKey<se.caboo.beast.model.Topic> TOPICS = new ERXKey<se.caboo.beast.model.Topic>(TOPICS_KEY);

  private static Logger LOG = Logger.getLogger(_User.class);

  public User localInstanceIn(EOEditingContext editingContext) {
    User localInstance = (User)EOUtilities.localInstanceOfObject(editingContext, this);
    if (localInstance == null) {
      throw new IllegalStateException("You attempted to localInstance " + this + ", which has not yet committed.");
    }
    return localInstance;
  }

  public String bio() {
    return (String) storedValueForKey("bio");
  }

  public void setBio(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating bio from " + bio() + " to " + value);
    }
    takeStoredValueForKey(value, "bio");
  }

  public String bioHtml() {
    return (String) storedValueForKey("bioHtml");
  }

  public void setBioHtml(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating bioHtml from " + bioHtml() + " to " + value);
    }
    takeStoredValueForKey(value, "bioHtml");
  }

  public NSTimestamp createdAt() {
    return (NSTimestamp) storedValueForKey("createdAt");
  }

  public void setCreatedAt(NSTimestamp value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating createdAt from " + createdAt() + " to " + value);
    }
    takeStoredValueForKey(value, "createdAt");
  }

  public String displayName() {
    return (String) storedValueForKey("displayName");
  }

  public void setDisplayName(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating displayName from " + displayName() + " to " + value);
    }
    takeStoredValueForKey(value, "displayName");
  }

  public NSTimestamp lastLoginAt() {
    return (NSTimestamp) storedValueForKey("lastLoginAt");
  }

  public void setLastLoginAt(NSTimestamp value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating lastLoginAt from " + lastLoginAt() + " to " + value);
    }
    takeStoredValueForKey(value, "lastLoginAt");
  }

  public NSTimestamp lastSeenAt() {
    return (NSTimestamp) storedValueForKey("lastSeenAt");
  }

  public void setLastSeenAt(NSTimestamp value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating lastSeenAt from " + lastSeenAt() + " to " + value);
    }
    takeStoredValueForKey(value, "lastSeenAt");
  }

  public String login() {
    return (String) storedValueForKey("login");
  }

  public void setLogin(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating login from " + login() + " to " + value);
    }
    takeStoredValueForKey(value, "login");
  }

  public Integer postsCount() {
    return (Integer) storedValueForKey("postsCount");
  }

  public void setPostsCount(Integer value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating postsCount from " + postsCount() + " to " + value);
    }
    takeStoredValueForKey(value, "postsCount");
  }

  public NSTimestamp updatedAt() {
    return (NSTimestamp) storedValueForKey("updatedAt");
  }

  public void setUpdatedAt(NSTimestamp value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating updatedAt from " + updatedAt() + " to " + value);
    }
    takeStoredValueForKey(value, "updatedAt");
  }

  public String website() {
    return (String) storedValueForKey("website");
  }

  public void setWebsite(String value) {
    if (_User.LOG.isDebugEnabled()) {
    	_User.LOG.debug( "updating website from " + website() + " to " + value);
    }
    takeStoredValueForKey(value, "website");
  }

  public NSArray<se.caboo.beast.model.Post> posts() {
    return (NSArray<se.caboo.beast.model.Post>)storedValueForKey("posts");
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier) {
    return posts(qualifier, null, false);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier, boolean fetch) {
    return posts(qualifier, null, fetch);
  }

  public NSArray<se.caboo.beast.model.Post> posts(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<se.caboo.beast.model.Post> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(se.caboo.beast.model.Post.USER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = se.caboo.beast.model.Post.fetchPosts(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = posts();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Post>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToPosts(se.caboo.beast.model.Post object) {
    includeObjectIntoPropertyWithKey(object, "posts");
  }

  public void removeFromPosts(se.caboo.beast.model.Post object) {
    excludeObjectFromPropertyWithKey(object, "posts");
  }

  public void addToPostsRelationship(se.caboo.beast.model.Post object) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("adding " + object + " to posts relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToPosts(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public void removeFromPostsRelationship(se.caboo.beast.model.Post object) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("removing " + object + " from posts relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromPosts(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "posts");
    }
  }

  public se.caboo.beast.model.Post createPostsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Post");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "posts");
    return (se.caboo.beast.model.Post) eo;
  }

  public void deletePostsRelationship(se.caboo.beast.model.Post object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "posts");
    editingContext().deleteObject(object);
  }

  public void deleteAllPostsRelationships() {
    Enumeration objects = posts().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deletePostsRelationship((se.caboo.beast.model.Post)objects.nextElement());
    }
  }

  public NSArray<se.caboo.beast.model.Topic> repliedToTopics() {
    return (NSArray<se.caboo.beast.model.Topic>)storedValueForKey("repliedToTopics");
  }

  public NSArray<se.caboo.beast.model.Topic> repliedToTopics(EOQualifier qualifier) {
    return repliedToTopics(qualifier, null, false);
  }

  public NSArray<se.caboo.beast.model.Topic> repliedToTopics(EOQualifier qualifier, boolean fetch) {
    return repliedToTopics(qualifier, null, fetch);
  }

  public NSArray<se.caboo.beast.model.Topic> repliedToTopics(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<se.caboo.beast.model.Topic> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(se.caboo.beast.model.Topic.REPLIED_BY_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = se.caboo.beast.model.Topic.fetchTopics(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = repliedToTopics();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Topic>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Topic>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToRepliedToTopics(se.caboo.beast.model.Topic object) {
    includeObjectIntoPropertyWithKey(object, "repliedToTopics");
  }

  public void removeFromRepliedToTopics(se.caboo.beast.model.Topic object) {
    excludeObjectFromPropertyWithKey(object, "repliedToTopics");
  }

  public void addToRepliedToTopicsRelationship(se.caboo.beast.model.Topic object) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("adding " + object + " to repliedToTopics relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToRepliedToTopics(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "repliedToTopics");
    }
  }

  public void removeFromRepliedToTopicsRelationship(se.caboo.beast.model.Topic object) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("removing " + object + " from repliedToTopics relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromRepliedToTopics(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "repliedToTopics");
    }
  }

  public se.caboo.beast.model.Topic createRepliedToTopicsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Topic");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "repliedToTopics");
    return (se.caboo.beast.model.Topic) eo;
  }

  public void deleteRepliedToTopicsRelationship(se.caboo.beast.model.Topic object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "repliedToTopics");
    editingContext().deleteObject(object);
  }

  public void deleteAllRepliedToTopicsRelationships() {
    Enumeration objects = repliedToTopics().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteRepliedToTopicsRelationship((se.caboo.beast.model.Topic)objects.nextElement());
    }
  }

  public NSArray<se.caboo.beast.model.Topic> topics() {
    return (NSArray<se.caboo.beast.model.Topic>)storedValueForKey("topics");
  }

  public NSArray<se.caboo.beast.model.Topic> topics(EOQualifier qualifier) {
    return topics(qualifier, null, false);
  }

  public NSArray<se.caboo.beast.model.Topic> topics(EOQualifier qualifier, boolean fetch) {
    return topics(qualifier, null, fetch);
  }

  public NSArray<se.caboo.beast.model.Topic> topics(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
    NSArray<se.caboo.beast.model.Topic> results;
    if (fetch) {
      EOQualifier fullQualifier;
      EOQualifier inverseQualifier = new EOKeyValueQualifier(se.caboo.beast.model.Topic.USER_KEY, EOQualifier.QualifierOperatorEqual, this);
    	
      if (qualifier == null) {
        fullQualifier = inverseQualifier;
      }
      else {
        NSMutableArray qualifiers = new NSMutableArray();
        qualifiers.addObject(qualifier);
        qualifiers.addObject(inverseQualifier);
        fullQualifier = new EOAndQualifier(qualifiers);
      }

      results = se.caboo.beast.model.Topic.fetchTopics(editingContext(), fullQualifier, sortOrderings);
    }
    else {
      results = topics();
      if (qualifier != null) {
        results = (NSArray<se.caboo.beast.model.Topic>)EOQualifier.filteredArrayWithQualifier(results, qualifier);
      }
      if (sortOrderings != null) {
        results = (NSArray<se.caboo.beast.model.Topic>)EOSortOrdering.sortedArrayUsingKeyOrderArray(results, sortOrderings);
      }
    }
    return results;
  }
  
  public void addToTopics(se.caboo.beast.model.Topic object) {
    includeObjectIntoPropertyWithKey(object, "topics");
  }

  public void removeFromTopics(se.caboo.beast.model.Topic object) {
    excludeObjectFromPropertyWithKey(object, "topics");
  }

  public void addToTopicsRelationship(se.caboo.beast.model.Topic object) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("adding " + object + " to topics relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	addToTopics(object);
    }
    else {
    	addObjectToBothSidesOfRelationshipWithKey(object, "topics");
    }
  }

  public void removeFromTopicsRelationship(se.caboo.beast.model.Topic object) {
    if (_User.LOG.isDebugEnabled()) {
      _User.LOG.debug("removing " + object + " from topics relationship");
    }
    if (er.extensions.ERXGenericRecord.InverseRelationshipUpdater.updateInverseRelationships()) {
    	removeFromTopics(object);
    }
    else {
    	removeObjectFromBothSidesOfRelationshipWithKey(object, "topics");
    }
  }

  public se.caboo.beast.model.Topic createTopicsRelationship() {
    EOClassDescription eoClassDesc = EOClassDescription.classDescriptionForEntityName("Topic");
    EOEnterpriseObject eo = eoClassDesc.createInstanceWithEditingContext(editingContext(), null);
    editingContext().insertObject(eo);
    addObjectToBothSidesOfRelationshipWithKey(eo, "topics");
    return (se.caboo.beast.model.Topic) eo;
  }

  public void deleteTopicsRelationship(se.caboo.beast.model.Topic object) {
    removeObjectFromBothSidesOfRelationshipWithKey(object, "topics");
    editingContext().deleteObject(object);
  }

  public void deleteAllTopicsRelationships() {
    Enumeration objects = topics().immutableClone().objectEnumerator();
    while (objects.hasMoreElements()) {
      deleteTopicsRelationship((se.caboo.beast.model.Topic)objects.nextElement());
    }
  }


  public static User createUser(EOEditingContext editingContext, String bio
, String bioHtml
, NSTimestamp createdAt
, String displayName
, NSTimestamp lastLoginAt
, NSTimestamp lastSeenAt
, String login
, Integer postsCount
, NSTimestamp updatedAt
, String website
) {
    User eo = (User) EOUtilities.createAndInsertInstance(editingContext, _User.ENTITY_NAME);    
		eo.setBio(bio);
		eo.setBioHtml(bioHtml);
		eo.setCreatedAt(createdAt);
		eo.setDisplayName(displayName);
		eo.setLastLoginAt(lastLoginAt);
		eo.setLastSeenAt(lastSeenAt);
		eo.setLogin(login);
		eo.setPostsCount(postsCount);
		eo.setUpdatedAt(updatedAt);
		eo.setWebsite(website);
    return eo;
  }

  public static NSArray<User> fetchAllUsers(EOEditingContext editingContext) {
    return _User.fetchAllUsers(editingContext, null);
  }

  public static NSArray<User> fetchAllUsers(EOEditingContext editingContext, NSArray<EOSortOrdering> sortOrderings) {
    return _User.fetchUsers(editingContext, null, sortOrderings);
  }

  public static NSArray<User> fetchUsers(EOEditingContext editingContext, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_User.ENTITY_NAME, qualifier, sortOrderings);
    fetchSpec.setIsDeep(true);
    NSArray<User> eoObjects = (NSArray<User>)editingContext.objectsWithFetchSpecification(fetchSpec);
    return eoObjects;
  }

  public static User fetchUser(EOEditingContext editingContext, String keyName, Object value) {
    return _User.fetchUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static User fetchUser(EOEditingContext editingContext, EOQualifier qualifier) {
    NSArray<User> eoObjects = _User.fetchUsers(editingContext, qualifier, null);
    User eoObject;
    int count = eoObjects.count();
    if (count == 0) {
      eoObject = null;
    }
    else if (count == 1) {
      eoObject = (User)eoObjects.objectAtIndex(0);
    }
    else {
      throw new IllegalStateException("There was more than one User that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static User fetchRequiredUser(EOEditingContext editingContext, String keyName, Object value) {
    return _User.fetchRequiredUser(editingContext, new EOKeyValueQualifier(keyName, EOQualifier.QualifierOperatorEqual, value));
  }

  public static User fetchRequiredUser(EOEditingContext editingContext, EOQualifier qualifier) {
    User eoObject = _User.fetchUser(editingContext, qualifier);
    if (eoObject == null) {
      throw new NoSuchElementException("There was no User that matched the qualifier '" + qualifier + "'.");
    }
    return eoObject;
  }

  public static User localInstanceIn(EOEditingContext editingContext, User eo) {
    User localInstance = (eo == null) ? null : (User)EOUtilities.localInstanceOfObject(editingContext, eo);
    if (localInstance == null && eo != null) {
      throw new IllegalStateException("You attempted to localInstance " + eo + ", which has not yet committed.");
    }
    return localInstance;
  }
}
