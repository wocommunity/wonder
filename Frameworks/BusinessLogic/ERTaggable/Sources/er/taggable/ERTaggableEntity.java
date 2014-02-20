package er.taggable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOAttribute;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXGenericRecord;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXCommandLineTokenizer;
import er.extensions.jdbc.ERXSQLHelper;
import er.taggable.model.ERTag;

/**
 * ERTaggableEntity provides entity-level tag management and fetching methods.
 *  
 * Typically you would provide a cover method from your entity class to an 
 * instance of an ERTaggableEntity:
 * 
 * <code>
 * public class Person extends _Person {
 *   ...
 *   public static ERTaggableEntity<Person> taggableEntity() {
 *     return ERTaggableEntity.taggableEntity(Person.ENTITY_NAME);
 *   }
 * }
 * </code>

 * @author mschrag
 *
 * @param <T> the java class of the entity that this ERTaggableEntity is associated with 
 */
public class ERTaggableEntity<T extends ERXGenericRecord> {

  /**
   * Default is white-space and/or comma(s). Multiple string of separators treated as one. 
   */
  private static final String DEFAULT_SEPARATOR = "[\\s,]+";

  /**
   * The key stored in entity userInfo that flags an entity as taggable.
   */
  public static final String ERTAGGABLE_KEY = "_ERTaggable";

  /**
   * The key stored in entity userInfo that specifies the name of the tag entity.
   */
  public static final String ERTAGGABLE_TAG_ENTITY_KEY = "_ERTaggableTagEntity";

  /**
   * The key stored in entity userInfo that specifies the name of the tag relationship.
   */
  public static final String ERTAGGABLE_TAG_RELATIONSHIP_KEY = "_ERTaggableTagRelationship";

  /**
   * The default name of the flattened to-many relationship to the tag entity.
   */
  public static final String DEFAULT_TAGS_RELATIONSHIP_NAME = "tags";

  private static final NSMutableDictionary<String, Class<? extends ERTaggableEntity<?>>> _taggableEntities = new NSMutableDictionary<String, Class<? extends ERTaggableEntity<?>>>();

  private final EOEntity _tagEntity;

  private final EOEntity _entity;
  private final EORelationship _tagsRelationship;

  private final String _separator = ERTaggableEntity.DEFAULT_SEPARATOR;
  private ERTagNormalizer _normalizer = new ERDefaultTagNormalizer();

  /**
   * Constructs an ERTaggableEntity.
   * 
   * @param entity the entity to tag
   */
  protected ERTaggableEntity(EOEntity entity) {
    if (!ERTaggableEntity.isTaggable(entity)) {
      throw new IllegalArgumentException("The entity '" + entity.name() + "' has not been registered as taggable.");
    }

    _entity = entity;

    String tagEntityName = (String) entity.userInfo().objectForKey(ERTaggableEntity.ERTAGGABLE_TAG_ENTITY_KEY);
    _tagEntity = _entity.model().modelGroup().entityNamed(tagEntityName);

    String tagsRelationshipName = (String) entity.userInfo().objectForKey(ERTaggableEntity.ERTAGGABLE_TAG_RELATIONSHIP_KEY);
    _tagsRelationship = _entity.relationshipNamed(tagsRelationshipName);
  }

  @Override
  public int hashCode() {
    return _entity.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof ERTaggableEntity && ((ERTaggableEntity<?>) obj)._entity.equals(_entity));
  }

  /**
   * Sets the taggable entity class for the given entity name.  This allows you to override the 
   * taggable entity that will be used throughout the framework for any particular entity. 
   * 
   * @param taggableEntity the taggable entity class
   * @param entityName the name of the entity to associate with
   */
  public static void setTaggableEntityForEntityNamed(Class<? extends ERTaggableEntity<?>> taggableEntity, String entityName) {
    ERTaggableEntity._taggableEntities.setObjectForKey(taggableEntity, entityName);
  }

  /**
   * Constructs an ERTaggableEntity.
   * 
   * @param entity the entity to tag
   */
  @SuppressWarnings("unchecked")
  public static <T extends ERXGenericRecord> ERTaggableEntity<T> taggableEntity(EOEntity entity) {
    Class<? extends ERTaggableEntity> taggableEntityClass = ERTaggableEntity._taggableEntities.objectForKey(entity.name());
    ERTaggableEntity<T> taggableEntity;
    if (taggableEntityClass == null) {
      taggableEntity = new ERTaggableEntity<T>(entity);
    }
    else {
      try {
        taggableEntity = taggableEntityClass.getConstructor(EOEntity.class).newInstance(entity);
      }
      catch (Exception e) {
        throw new RuntimeException("Failed to create ERTaggableEntity for entity '" + entity + "'.", e);
      }
    }
    return taggableEntity;
  }

  /**
   * Constructs an ERTaggableEntity.
   * 
   * @param entityName the name of the entity to tag
   */
  public static <T extends ERXGenericRecord> ERTaggableEntity<T> taggableEntity(String entityName) {
    return ERTaggableEntity.taggableEntity(EOModelGroup.defaultGroup().entityNamed(entityName));
  }

  /**
   * Shortcut for getting an ERTaggableEntity for an EO.
   * 
   * @param <T> the type of the entity
   * @param eo the EO
   * @return an ERTaggableEntity corresponding to the entity of the EO
   */
  public static <T extends ERXGenericRecord> ERTaggableEntity<T> taggableEntity(T eo) {
    return ERTaggableEntity.taggableEntity(eo.entity());
  }

  /**
   * Fetches all the EOs of all taggable entities that are associated with all of the given tags (unlimited).
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @return a dictionary mapping entities to an array of matching EO's
   */
  public static NSDictionary<EOEntity, NSArray<? extends ERXGenericRecord>> fetchAllTaggedWith(EOEditingContext editingContext, Object tags) {
    return ERTaggableEntity.fetchAllTaggedWith(editingContext, ERTag.Inclusion.ALL, -1, tags);
  }

  /**
   * Fetches all the EOs of all taggable entities that are associated with the given tags (unlimited).
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @return a dictionary mapping entities to an array of matching EO's
   */
  public static NSDictionary<EOEntity, NSArray<? extends ERXGenericRecord>> fetchAllTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, Object tags) {
    return ERTaggableEntity.fetchAllTaggedWith(editingContext, inclusion, -1, tags);
  }

  /**
   * Fetches all the EOs of all taggable entities that are associated with the given tags.
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @param limit the limit of the number of objects to return (or -1 for unlimited)
   * @return a dictionary mapping entities to an array of matching EO's
   */
  public static NSDictionary<EOEntity, NSArray<? extends ERXGenericRecord>> fetchAllTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, int limit, Object tags) {
    NSMutableDictionary<EOEntity, NSArray<? extends ERXGenericRecord>> taggedEntities = new NSMutableDictionary<EOEntity, NSArray<? extends ERXGenericRecord>>();
    for (EOEntity taggableEntity : ERTaggableEntity.taggableEntities()) {
      NSArray<ERXGenericRecord> taggedItems = ERTaggableEntity.taggableEntity(taggableEntity).fetchTaggedWith(editingContext, inclusion, limit, tags);
      taggedEntities.setObjectForKey(taggedItems, taggableEntity);
    }
    return taggedEntities;
  }

  /**
   * Returns whether or not the given entity has been registered as taggable.
   * 
   * @param entity the entity to check
   * @return true if the entity is taggable, false if not
   */
  public static boolean isTaggable(EOEntity entity) {
    return Boolean.TRUE.equals(entity.userInfo().objectForKey(ERTaggableEntity.ERTAGGABLE_KEY));
  }

  /**
   * Returns an array of taggable entities.
   * 
   * @return an array of taggable entities
   */
  @SuppressWarnings("unchecked")
  public static NSArray<EOEntity> taggableEntities() {
    NSMutableArray<EOEntity> taggableEntities = new NSMutableArray<EOEntity>();
    for (EOModel model : EOModelGroup.defaultGroup().models()) {
      for (EOEntity entity : model.entities()) {
        if (ERTaggableEntity.isTaggable(entity)) {
          taggableEntities.addObject(entity);
        }
      }
    }
    return taggableEntities;
  }

  /**
   * Returns the flattened to-many relationship from the taggable entity to the given tag entity.
   *  
   * @param entity the taggable entity
   * @param tagEntity the tag entity
   * @return the flattened to-many relationship between them (or null if there isn't one)
   */
  @SuppressWarnings("unchecked")
  public static EORelationship tagsRelationshipForEntity(EOEntity entity, EOEntity tagEntity) {
    EORelationship tagsRelationship = null;
    for (EORelationship relationship : entity.relationships()) {
      if (relationship.isFlattened() && tagEntity.name().equals(relationship.destinationEntity().name())) {
        tagsRelationship = relationship;
        break;
      }
    }
    return tagsRelationship;
  }

  /**
   * Registers the given entity name in the default model group as taggable.  An entity must
   * be registered as taggable prior to attempting any tagging operations on it.  The application
   * constructor is an obvious place to register an entity as taggable.  If the entity does not 
   * contain a flattened to-many tags relationship, a join entity (between your entity and "ERTag") 
   * and a flattened tags relationship (named "tags") will be automatically generated.
   * 
   * @param entityName the name of the entity to lookup
   * @param taggableEntity the taggable entity to associate with this taggable
   * @return the join entity (you can probably ignore this)
   */
  public static EOEntity registerTaggable(String entityName, Class<? extends ERTaggableEntity<?>> taggableEntity) {
    EOEntity joinEntity = ERTaggableEntity.registerTaggable(entityName);
    ERTaggableEntity.setTaggableEntityForEntityNamed(taggableEntity, entityName);
    return joinEntity;
  }

  /**
   * Registers the given entity name in the default model group as taggable.  An entity must
   * be registered as taggable prior to attempting any tagging operations on it.  The application
   * constructor is an obvious place to register an entity as taggable.  If the entity does not 
   * contain a flattened to-many tags relationship, a join entity (between your entity and "ERTag") 
   * and a flattened tags relationship (named "tags") will be automatically generated.
   * 
   * @param entityName the name of the entity to lookup
   * @return the join entity (you can probably ignore this)
   */
  public static EOEntity registerTaggable(String entityName) {
    EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
    if (entity == null) {
      throw new IllegalArgumentException("There is no entity named '" + entityName + "' in this model group.");
    }
    return ERTaggableEntity.registerTaggable(entity);
  }

  /**
   * Registers the given entity as taggable.  An entity must be registered as taggable prior 
   * to attempting any tagging operations on it.  The application constructor is an obvious 
   * place to register an entity as taggable.  If the entity does not contain a flattened 
   * to-many tags relationship, a join entity (between your entity and "ERTag") and a 
   * flattened tags relationship (named "tags") will be automatically generated.
   * 
   * @param entity the entity to register
   * @return the join entity (you can probably ignore this)
   */
  public static EOEntity registerTaggable(EOEntity entity) {
    return ERTaggableEntity.registerTaggable(entity, ERTaggableEntity.DEFAULT_TAGS_RELATIONSHIP_NAME);
  }

  /**
   * Registers the given entity as taggable.  An entity must be registered as taggable prior 
   * to attempting any tagging operations on it.  The application constructor is an obvious 
   * place to register an entity as taggable.  If the entity does not contain a flattened 
   * to-many tags relationship, a join entity and a flattened tags relationship will be 
   * automatically generated. 
   * 
   * @param entity the entity to register
   * @param tagsRelationshipName the name of the flattened to-many tags relationship
   * @return the join entity (you can probably ignore this)
   */
  public static EOEntity registerTaggable(EOEntity entity, String tagsRelationshipName) {
    EOEntity tagEntity = entity.model().modelGroup().entityNamed(ERTag.ENTITY_NAME);
    if (tagEntity == null) {
      throw new IllegalArgumentException("There is no entity named '" + ERTag.ENTITY_NAME + "' in this model group.");
    }
    return ERTaggableEntity.registerTaggable(entity, tagsRelationshipName, tagEntity, null);
  }

  /**
   * Registers the given entity as taggable.  An entity must be registered as taggable prior 
   * to attempting any tagging operations on it.  The application constructor is an obvious 
   * place to register an entity as taggable.  If the entity does not contain a flattened 
   * to-many tags relationship, a join entity and a flattened tags relationship will be 
   * automatically generated. 
   * 
   * @param entity the entity to register
   * @param tagsRelationshipName the name of the flattened to-many tags relationship
   * @param tagEntity the ERTag entity that contains the tags for this entity
   * @param taggableEntity the taggable entity to associate with this taggable
   * @return the join entity (you can probably ignore this)
   */
  @SuppressWarnings("unchecked")
  public static EOEntity registerTaggable(EOEntity entity, String tagsRelationshipName, EOEntity tagEntity, Class<? extends ERTaggableEntity<?>> taggableEntity) {
    EORelationship tagsRelationship;
    if (tagsRelationshipName == null) {
      tagsRelationship = ERTaggableEntity.tagsRelationshipForEntity(entity, tagEntity);
    }
    else {
      tagsRelationship = entity.relationshipNamed(tagsRelationshipName);
    }

    EOEntity joinEntity = null;
    if (tagsRelationship == null) {
      joinEntity = new EOEntity();
      joinEntity.setName(entity.name() + "Tag");
      joinEntity.setExternalName(joinEntity.name());

      EORelationship joinToItemRelationship = new EORelationship();
      joinToItemRelationship.setName(entity.name());
      joinToItemRelationship.setIsMandatory(true);
      joinToItemRelationship.setToMany(false);
      joinToItemRelationship.setJoinSemantic(EORelationship.InnerJoin);
      joinEntity.addRelationship(joinToItemRelationship);
      for (EOAttribute itemPrimaryKey : entity.primaryKeyAttributes()) {
        EOAttribute itemFKAttribute = new EOAttribute();
        itemFKAttribute.setExternalType(itemPrimaryKey.externalType());
        itemFKAttribute.setValueType(itemPrimaryKey.valueType());
        itemFKAttribute.setName("item_" + itemPrimaryKey.name());
        itemFKAttribute.setColumnName("item_" + itemPrimaryKey.columnName());
        itemFKAttribute.setClassName(itemPrimaryKey.className());
        itemFKAttribute.setWidth(itemPrimaryKey.width());
        itemFKAttribute.setPrecision(itemPrimaryKey.precision());
        itemFKAttribute.setScale(itemPrimaryKey.scale());
        itemFKAttribute.setAllowsNull(false);
        joinEntity.addAttribute(itemFKAttribute);

        EOJoin join = new EOJoin(itemFKAttribute, itemPrimaryKey);
        joinToItemRelationship.addJoin(join);
      }

      EORelationship joinToTagRelationship = new EORelationship();
      joinToTagRelationship.setName(tagEntity.name());
      joinToTagRelationship.setIsMandatory(true);
      joinToTagRelationship.setToMany(false);
      joinToTagRelationship.setJoinSemantic(EORelationship.InnerJoin);
      joinEntity.addRelationship(joinToTagRelationship);
      for (EOAttribute tagPrimaryKey : tagEntity.primaryKeyAttributes()) {
        EOAttribute tagFKAttribute = new EOAttribute();
        tagFKAttribute.setExternalType(tagPrimaryKey.externalType());
        tagFKAttribute.setValueType(tagPrimaryKey.valueType());
        tagFKAttribute.setName("tag_" + tagPrimaryKey.name());
        tagFKAttribute.setColumnName("tag_" + tagPrimaryKey.columnName());
        tagFKAttribute.setClassName(tagPrimaryKey.className());
        tagFKAttribute.setWidth(tagPrimaryKey.width());
        tagFKAttribute.setPrecision(tagPrimaryKey.precision());
        tagFKAttribute.setScale(tagPrimaryKey.scale());
        tagFKAttribute.setAllowsNull(false);
        joinEntity.addAttribute(tagFKAttribute);

        joinToTagRelationship.addJoin(new EOJoin(tagFKAttribute, tagPrimaryKey));
      }

      joinEntity.setPrimaryKeyAttributes(joinEntity.attributes());
      joinEntity.setAttributesUsedForLocking(joinEntity.attributes());
      entity.model().addEntity(joinEntity);

      EORelationship itemToJoinRelationship = new EORelationship();
      itemToJoinRelationship.setEntity(joinToItemRelationship.destinationEntity());
      itemToJoinRelationship.setName("_eofInv_" + joinToItemRelationship.entity().name() + "_" + joinToItemRelationship.name());
      NSArray<EOJoin> joinToItemRelationshipJoins = joinToItemRelationship.joins();
      for (int joinNum = joinToItemRelationshipJoins.count() - 1; joinNum >= 0; joinNum--) {
        EOJoin join = joinToItemRelationshipJoins.objectAtIndex(joinNum);
        EOJoin inverseJoin = new EOJoin(join.destinationAttribute(), join.sourceAttribute());
        itemToJoinRelationship.addJoin(inverseJoin);
      }
      itemToJoinRelationship.setDeleteRule(1); // cascade
      itemToJoinRelationship.setJoinSemantic(EORelationship.InnerJoin);
      itemToJoinRelationship.setToMany(true);
      itemToJoinRelationship.setPropagatesPrimaryKey(true);
      entity.addRelationship(itemToJoinRelationship);

      NSMutableArray properties = entity.classProperties().mutableClone();
      properties.remove(itemToJoinRelationship);
      entity.setClassProperties(properties);

      EORelationship itemToTagsRelationship = new EORelationship();
      itemToTagsRelationship.setName(tagsRelationshipName);
      entity.addRelationship(itemToTagsRelationship);
      itemToTagsRelationship.setDefinition(itemToJoinRelationship.name() + "." + joinToTagRelationship.name());

      tagsRelationship = itemToTagsRelationship;
    }
    else if (!tagsRelationship.isFlattened()) {
      throw new IllegalArgumentException("The relationship '" + tagsRelationship.name() + "' on '" + entity.name() + "' must be flattened.");
    }
    else {
      EORelationship itemToJoinRelationship = (EORelationship) tagsRelationship.componentRelationships().objectAtIndex(0);
      joinEntity = itemToJoinRelationship.destinationEntity();
    }

    NSMutableDictionary userInfo = entity.userInfo().mutableClone();
    userInfo.setObjectForKey(Boolean.TRUE, ERTaggableEntity.ERTAGGABLE_KEY);
    userInfo.setObjectForKey(tagsRelationship.name(), ERTaggableEntity.ERTAGGABLE_TAG_RELATIONSHIP_KEY);
    userInfo.setObjectForKey(tagEntity.name(), ERTaggableEntity.ERTAGGABLE_TAG_ENTITY_KEY);
    entity.setUserInfo(userInfo);

    if (taggableEntity != null) {
      ERTaggableEntity.setTaggableEntityForEntityNamed(taggableEntity, entity.name());
    }

    return joinEntity;
  }

  /**
   * Returns the tag normalizer for this entity.
   * 
   * @return the tag normalizer for this entity
   */
  public ERTagNormalizer normalizer() {
    return _normalizer;
  }

  /**
   * Sets the tag normalizer for this entity.
   * 
   * @param normalizer the tag normalizer for this entity
   */
  public void setNormalizer(ERTagNormalizer normalizer) {
    _normalizer = normalizer;
  }

  /**
   * Fetches the tag with the given name.  If that tag doesn't exist and createIfMissing
   * is true, a tag with that name will be created (otherwise null will be returned). Tags
   * are created in a separate transaction to prevent race conditions with duplicate
   * tag names from rolling back your primary editing context, which means that even if 
   * you rollback your editingContext, any tags created during its lifetime will remain.
   * 
   * @param editingContext the editing context to fetch into
   * @param tagName the name of the tag to lookup
   * @param createIfMissing if true, missing tags will be created
   * @return the corresponding ERTag (or null if not found)
   */
  @SuppressWarnings( { "cast", "unchecked" })
  public ERTag fetchTagNamed(EOEditingContext editingContext, String tagName, boolean createIfMissing) {
    NSArray<ERTag> tags = (NSArray<ERTag>) ERXEOControlUtilities.objectsWithQualifier(editingContext, _tagEntity.name(), ERTag.NAME.is(tagName), null, true, true, true, true);
    ERTag tag;
    if (tags.count() == 0) {
      if (createIfMissing) {
        // Create it in another transaction so we can catch the dupe exception.  Note that
        // this means that tags will ALWAYS be created even if the parent transaction
        // rolls back.  It's mostly for your own good :)
        EOEditingContext newEditingContext = ERXEC.newEditingContext();
        try {
          ERTag newTag = createTagNamed(newEditingContext, tagName);
          newEditingContext.saveChanges();
          tag = newTag.localInstanceIn(editingContext);
        }
        catch (EOGeneralAdaptorException e) {
          // We'll assume this was because of a duplicate key exception and just retry the original
          // fetch WITHOUT createIfMissing.  If that returns a null, then we know it was some other
          // crazy exception and just throw it.
          tag = fetchTagNamed(editingContext, tagName, false);
          if (tag == null) {
            throw e;
          }
        }
      }
      else {
        tag = null;
      }
    }
    else if (tags.count() == 1) {
      tag = tags.objectAtIndex(0);
    }
    else {
      throw new IllegalArgumentException("There was more than one tag with the name '" + tagName + "'");
    }
    return tag;
  }

  /**
   * Creates a tag with the given name.
   * 
   * @param editingContext the editing context to create within
   * @param tagName the new tag name
   * @return the created tag
   */
  public ERTag createTagNamed(EOEditingContext editingContext, String tagName) {
    ERTag tag = (ERTag) EOUtilities.createAndInsertInstance(editingContext, _tagEntity.name());
    tag.setName(tagName);
    return tag;
  }

  /**
   * Factory method for generating an ERTaggable wrapper for an EO.
   * 
   * @param eo the EO to wrap
   * @return an ERTaggable wrapper
   */
  public ERTaggable<T> taggable(T eo) {
    return new ERTaggable<T>(this, eo);
  }

  /**
   * Returns the name of the tags relationship for this entity.
   * 
   * @return the name of the tags relationship for this entity
   */
  public String tagsRelationshipName() {
    return _tagsRelationship.name();
  }

  /**
   * Returns the tags relationship for this entity.
   * 
   * @return the tags relationship for this entity
   */
  public EORelationship tagsRelationship() {
    return _tagsRelationship;
  }
  
  /**
   * Returns whether or not the given separator contains whitespace (and should be escaped).
   * 
   * @return true if the given separator contains whitespace
   */
  public static boolean isWhitespaceSeparator(String separator) {
    return separator != null && (separator.contains("\\s") || separator.contains(" "));
  }

  /**
   * Splits the given "tags" object (String, array of Strings, etc) into an array of normalized tag strings.
   * 
   * @param tags the object that contains the tags to split
   * @return the list of split tag names
   */
  @SuppressWarnings("unchecked")
  public NSArray<String> splitTagNames(Object tags) {
    NSMutableSet<String> tagNames = new NSMutableSet<String>();
    if (tags != null) {
      if (tags instanceof String) {
        String[] strTags;
        if (ERTaggableEntity.isWhitespaceSeparator(_separator)) {
          List<String> strTagsList = new LinkedList<String>();
          ERXCommandLineTokenizer tagTokenizer = new ERXCommandLineTokenizer((String) tags);
          while (tagTokenizer.hasMoreTokens()) {
            String tag = tagTokenizer.nextElement();
            strTagsList.add(tag);
          }
          strTags = strTagsList.toArray(new String[strTagsList.size()]);
        }
        else {
          strTags = ((String) tags).split(_separator);
        }
        addNormalizedTags(tagNames, strTags);
      }
      else if (tags instanceof ERTag) {
        tagNames.addObject(((ERTag) tags).name());
      }
      else if (tags instanceof NSArray) {
        addNormalizedTags(tagNames, ((NSArray<Object>) tags).objects());
      }
      else if (tags instanceof Object[]) {
        addNormalizedTags(tagNames, (Object[]) tags);
      }
      else {
        throw new IllegalArgumentException("Unknown tag type '" + tags.getClass().getName() + "' (" + tags + " ).");
      }
    }
    return tagNames.allObjects();
  }

  /**
   * Normalizes tags from tags array and adds them to set
   * 
   * @param set set that normalized tags should be added to
   * @param tags array of unclean tags
   */
  private void addNormalizedTags(NSMutableSet<String> set, Object[] tags) {
    for (Object objTag : tags) {
      if (objTag instanceof String) {
        String strTag = (String) objTag;
        String normalizedTag = _normalizer.normalize(strTag);
        if (normalizedTag != null && normalizedTag.length() > 0) {
          set.addObject(normalizedTag);
        }
      }
      else if (objTag instanceof ERTag) {
        set.addObject(((ERTag)objTag).name());
      }
      else {
        throw new IllegalArgumentException("Unknown tag type '" + objTag.getClass().getName() + "' (" + objTag + " ).");
      }
    }
  }

  /**
   * Fetches the list of objects of this entity type that are tagged
   * with all of the given tags with unlimited results. 
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @return the array of matching eos
   */
  public NSArray<T> fetchTaggedWith(EOEditingContext editingContext, Object tags) {
    return fetchTaggedWith(editingContext, ERTag.Inclusion.ALL, tags);
  }

  /**
   * Fetches the list of objects of this entity type that are tagged
   * with the given tags with unlimited results. 
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @return the array of matching eos
   */
  public NSArray<T> fetchTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, Object tags) {
    return fetchTaggedWith(editingContext, inclusion, -1, tags);
  }

  /**
   * Fetches the list of objects of this entity type that are tagged
   * with the given tags. 
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @param limit limit the number of results to be returned (-1 for unlimited)
   * @return the array of matching eos
   */
  public NSArray<T> fetchTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, int limit, Object tags) {
    return fetchTaggedWith(editingContext, inclusion, limit, tags, null);
  }

  /**
   * Fetches the list of objects of this entity type that are tagged
   * with the given tags. 
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @param limit limit the number of results to be returned (-1 for unlimited)
   * @param additionalQualifier an additional qualifier to chain in
   * @return the array of matching eos
   */
  @SuppressWarnings("unchecked")
  public NSArray<T> fetchTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, int limit, Object tags, EOQualifier additionalQualifier) {
	  return this.fetchTaggedWith(editingContext, inclusion, limit, tags, additionalQualifier, null);
  }
  
  /**
   * Fetches the sorted list of objects of this entity type that are tagged
   * with the given tags. 
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @param limit limit the number of results to be returned (-1 for unlimited)
   * @param additionalQualifier an additional qualifier to chain in
   * @param sortOrderings sort orderings for the fetch spec
   * @return the array of matching eos
   */
  @SuppressWarnings("unchecked")
  public NSArray<T> fetchTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, int limit, Object tags, EOQualifier additionalQualifier, NSArray<EOSortOrdering> sortOrderings) {
    NSArray<String> tagNames = splitTagNames(tags);
    if (tagNames.count() == 0) {
      throw new IllegalArgumentException("No tags were passed in.");
    }

    ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(_entity.model());

    EOQualifier qualifier = new ERXKey<ERTag>(_tagsRelationship.name()).append(ERTag.NAME).in(tagNames);
    if (additionalQualifier != null) {
      qualifier = ERXQ.and(qualifier, additionalQualifier);
    }
    
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_entity.name(), qualifier, sortOrderings);

    EOSQLExpression sqlExpression = sqlHelper.sqlExpressionForFetchSpecification(editingContext, fetchSpec, 0, limit);
    sqlHelper.addGroupByClauseToExpression(editingContext, fetchSpec, sqlExpression);
    if (inclusion == ERTag.Inclusion.ALL) {
      sqlHelper.addHavingCountClauseToExpression(EOQualifier.QualifierOperatorEqual, tagNames.count(), sqlExpression);
    }

    NSArray<NSDictionary> rawRows = ERXEOAccessUtilities.rawRowsForSQLExpression(editingContext, _entity.model(), sqlExpression, sqlHelper.attributesToFetchForEntity(fetchSpec, _entity));
    NSArray<T> objs;
    objs = ERXEOControlUtilities.faultsForRawRowsFromEntity(editingContext, rawRows, _entity.name());
    objs = ERXEOControlUtilities.objectsForFaultWithSortOrderings(editingContext, objs, fetchSpec.sortOrderings());
    return objs;
  }

  /**
   * Remove all of the tags from instances of this entity type.
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to remove (String to tokenize, NSArray<String>, etc)
   */
  public void removeTags(EOEditingContext editingContext, Object tags) {
    replaceTags(editingContext, ERTag.Inclusion.ALL, tags, null);
  }

  /**
   * Looks for items with oldTags and replaces them with all of newTags.
   *
   * @param editingContext the editing context to remove with
   * @param oldTags the tags to find and remove (String to tokenize, NSArray<String>, etc)
   * @param newTags the tags to add
   * @param inclusion if ANY, finds any tags that match, removes them all, and adds newTags; if all, requires all tags to match before replacing  
   */
  public void replaceTags(EOEditingContext editingContext, ERTag.Inclusion inclusion, Object oldTags, Object newTags) {
    for (T item : fetchTaggedWith(editingContext, inclusion, oldTags)) {
      ERTaggable<T> taggable = taggable(item);
      taggable.removeTags(oldTags);
      taggable.addTags(newTags);
    }
  }

  /**
   * This method counts the number of times the tags have been applied to your objects
   * and, by default, returns a dictionary in the form of { 'tag_name' => count, ... }.  This
   * does not include any restriction on the count required for results to be returned nor
   * does it limit the number of results returned.
   *
   * @param editingContext the editing context to fetch into
   * @return a dictionary of tags and their occurrence count
   */
  public NSDictionary<String, Integer> tagCount(EOEditingContext editingContext) {
	  return tagCount(editingContext, null);
  }
  
  /**
   * This method counts the number of times the tags have been applied to your objects
   * and, by default, returns a dictionary in the form of { 'tag_name' => count, ... }.  This
   * does not include any restriction on the count required for results to be returned nor
   * does it limit the number of results returned.
   *
   * @param editingContext the editing context to fetch into
   * @param additionalQualifier an optional restrictingQualifier
   * @return a dictionary of tags and their occurrence count
   */
  public NSDictionary<String, Integer> tagCount(EOEditingContext editingContext, EOQualifier additionalQualifier) {
    return tagCount(editingContext, -1, additionalQualifier);
  }

  /**
   * This method counts the number of times the tags have been applied to your objects
   * and, by default, returns a dictionary in the form of { 'tag_name' => count, ... }.  This
   * does not include any restriction on the count required for results to be returned.
   *
   * @param editingContext the editing context to fetch into
   * @param limit the limit of the number of results to return (ordered by count DESC)
   * @return a dictionary of tags and their occurrence count
   */
  public NSDictionary<String, Integer> tagCount(EOEditingContext editingContext, int limit) {
	  return tagCount(editingContext, limit, null);
  }
  
  /**
   * This method counts the number of times the tags have been applied to your objects
   * and, by default, returns a dictionary in the form of { 'tag_name' => count, ... }.  This
   * does not include any restriction on the count required for results to be returned.
   *
   * @param editingContext the editing context to fetch into
   * @param limit the limit of the number of results to return (ordered by count DESC)
   * @param additionalQualifier an optional restrictingQualifier
   * @return a dictionary of tags and their occurrence count
   */
  public NSDictionary<String, Integer> tagCount(EOEditingContext editingContext, int limit, EOQualifier additionalQualifier) {
    return tagCount(editingContext, null, -1, limit, additionalQualifier);
  }

  /**
   * This method counts the number of times the tags have been applied to your objects
   * and, by default, returns a dictionary in the form of { 'tag_name' => count, ... }. Providing
   * a selector and count allows you to add a restriction on, for instance, the minimum number of
   * occurrences required for a result to appear. As an example, you might have 
   * selector = EOQualifier.QualifierOperatorGreaterThan, count = 1 to only return tags with more 
   * than one occurrence.
   *
   * @param editingContext the editing context to fetch into
   * @param selector a selector for the count restriction (see EOQualifier.QualifierOperators)
   * @param count the count restriction required for the result to be returned
   * @param limit the limit of the number of results to return (ordered by count DESC)
   * @return a dictionary of tags and their occurrence count
   */
  public NSDictionary<String, Integer> tagCount(EOEditingContext editingContext, NSSelector selector, int count, int limit) {
	  return tagCount(editingContext, selector, count, limit, null);
  }
  
  /**
   * This method counts the number of times the tags have been applied to your objects
   * and, by default, returns a dictionary in the form of { 'tag_name' => count, ... }. Providing
   * a selector and count allows you to add a restriction on, for instance, the minimum number of
   * occurrences required for a result to appear. As an example, you might have 
   * selector = EOQualifier.QualifierOperatorGreaterThan, count = 1 to only return tags with more 
   * than one occurrence.
   *
   * @param editingContext the editing context to fetch into
   * @param selector a selector for the count restriction (see EOQualifier.QualifierOperators)
   * @param count the count restriction required for the result to be returned
   * @param limit the limit of the number of results to return (ordered by count DESC)
   * @param additionalQualifier an optional restrictingQualifier. This is combined with the qualifier returned by additionalTagCountQualifier()
   * @return a dictionary of tags and their occurrence count
   */
  @SuppressWarnings("unchecked")
  public NSDictionary<String, Integer> tagCount(EOEditingContext editingContext, NSSelector selector, int count, int limit, EOQualifier additionalQualifier) {
    NSMutableArray<EOAttribute> fetchAttributes = new NSMutableArray<EOAttribute>();
    ERXEOAttribute tagNameAttribute = new ERXEOAttribute(_entity, _tagsRelationship.name() + "." + ERTag.NAME_KEY);
    tagNameAttribute.setName("tagName");
    fetchAttributes.addObject(tagNameAttribute);

    EOAttribute countAttribute = ERXEOAccessUtilities.createAggregateAttribute(editingContext, "COUNT", ERTag.NAME_KEY, _tagEntity.name(), Number.class, "i", "tagCount", "t2");
    fetchAttributes.addObject(countAttribute);

    ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(_entity.model());
	EOQualifier combinedAdditionalQualifier = null;
	EOQualifier additionalTagCountQualifier = additionalTagCountQualifier();
	if (additionalTagCountQualifier != null || additionalQualifier != null) {
		combinedAdditionalQualifier = ERXQ.and(additionalQualifier, additionalTagCountQualifier);
	}
	EOFetchSpecification fetchSpec = new EOFetchSpecification(_entity.name(), combinedAdditionalQualifier, null);
    EOSQLExpression sqlExpression = sqlHelper.sqlExpressionForFetchSpecification(editingContext, fetchSpec, 0, limit, fetchAttributes);
    NSMutableArray<EOAttribute> groupByAttributes = new NSMutableArray<EOAttribute>(tagNameAttribute);
    sqlHelper.addGroupByClauseToExpression(groupByAttributes, sqlExpression);
    if (selector != null) {
      sqlHelper.addHavingCountClauseToExpression(selector, count, sqlExpression);
    }
    if (limit > 0) {
      // MS: This is lame, but the dynamic attribute is not properly resolved
      // inside of EOSQLExpression because it's not actually part of the entity,
      // so you can't order-by one of these attributes.  So we just have to stick
      // it on the end and hope for the best.
      StringBuilder sqlBuffer = new StringBuilder(sqlExpression.statement());
      int orderByIndex = sqlHelper._orderByIndex(sqlExpression);
      sqlBuffer.insert(orderByIndex, " ORDER BY tagCount DESC");
      sqlExpression.setStatement(sqlBuffer.toString());
    }

    NSMutableDictionary<String, Integer> tagCounts = new NSMutableDictionary<String, Integer>();
    NSArray<NSDictionary> rawRows = ERXEOAccessUtilities.rawRowsForSQLExpression(editingContext, _entity.model(), sqlExpression, fetchAttributes);
    for (NSDictionary rawRow : rawRows) {
      String name = (String) rawRow.objectForKey("tagName");
      Integer nameCount = (Integer) rawRow.objectForKey("tagCount");
      tagCounts.setObjectForKey(nameCount, name);
    }

    return tagCounts;
  }

  /**
   * This method returns a simple count of the number of distinct objects which match the tags provided.
   * 
   * @param editingContext the editing context to fetch into
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   * @param inclusion find matches for ANY tags or ALL tags provided
   * @return the count of distinct objects for the given tags
   */
  public int countUniqueTaggedWith(EOEditingContext editingContext, ERTag.Inclusion inclusion, Object tags) {
    NSArray<String> tagNames = splitTagNames(tags);
    if (tagNames.count() == 0) {
      throw new IllegalArgumentException("No tags were passed in.");
    }

    EOQualifier qualifier = new ERXKey<ERTag>(_tagsRelationship.name()).append(ERTag.NAME).in(tagNames);
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_entity.name(), qualifier, null);
    fetchSpec.setUsesDistinct(true);

    ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(_entity.model());
    EOSQLExpression sqlExpression = sqlHelper.sqlExpressionForFetchSpecification(editingContext, fetchSpec, 0, -1);
    sqlHelper.addGroupByClauseToExpression(editingContext, fetchSpec, sqlExpression);
    if (inclusion == ERTag.Inclusion.ALL) {
      sqlHelper.addHavingCountClauseToExpression(EOQualifier.QualifierOperatorEqual, tagNames.count(), sqlExpression);
    }

    int count = sqlHelper.rowCountForFetchSpecification(editingContext, fetchSpec);
    return count;
  }

  /**
   * Finds other tags that are related to the tags passed through the tags
   * parameter, by finding common records that share similar sets of tags.
   * Useful for constructing 'Related tags' lists.
   *
   * @param tags the tags to search (String to tokenize, NSArray<String>, etc)
   */
  @SuppressWarnings("unchecked")
  public NSArray<String> fetchRelatedTags(EOEditingContext editingContext, Object tags) {
    NSArray<String> tagNames = splitTagNames(tags);
    if (tagNames.count() == 0) {
      throw new IllegalArgumentException("No tags were passed in.");
    }

    NSArray<EOAttribute> pkAttrs = _entity.primaryKeyAttributes();
    if (pkAttrs.count() > 1) {
      throw new IllegalArgumentException("Composite primary keys are not supported for findRelatedTags.");
    }

    NSMutableArray<EOAttribute> fetchAttributes = new NSMutableArray<EOAttribute>();
    fetchAttributes.addObjectsFromArray(_entity.primaryKeyAttributes());

    ERXEOAttribute tagNameAttribute = new ERXEOAttribute(_entity, _tagsRelationship.name() + "." + ERTag.NAME_KEY);
    tagNameAttribute.setName("tagName");
    fetchAttributes.addObject(tagNameAttribute);

    ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(_entity.model());
    EOQualifier tagNameQualifier = new ERXKey<ERTag>(_tagsRelationship.name()).append(ERTag.NAME).in(tagNames);
    EOFetchSpecification fetchSpec = new EOFetchSpecification(_entity.name(), tagNameQualifier, null);
    EOSQLExpression sqlExpression = sqlHelper.sqlExpressionForFetchSpecification(editingContext, fetchSpec, 0, -1, fetchAttributes);
    NSMutableArray<EOAttribute> groupByAttributes = new NSMutableArray<EOAttribute>();
    groupByAttributes.addObjectsFromArray(pkAttrs);
    sqlHelper.addGroupByClauseToExpression(groupByAttributes, sqlExpression);
    sqlHelper.addHavingCountClauseToExpression(EOQualifier.QualifierOperatorEqual, tagNames.count(), sqlExpression);

    // MS: Sketchy, I know, but I don't know how to make it do the
    // join for me without also having the tag name field selected.  I'm sure it's
    // possible if I drop down and use lower level API's than 
    // sqlExpr.selectStatementForAttributes. 
    sqlHelper.removeSelectFromExpression(tagNameAttribute, sqlExpression);

    NSMutableArray<Object> itemPrimaryKeys = new NSMutableArray<Object>();
    NSArray<NSDictionary> rawRows = ERXEOAccessUtilities.rawRowsForSQLExpression(editingContext, _entity.model(), sqlExpression, pkAttrs);
    EOAttribute pkAttr = pkAttrs.objectAtIndex(0);
    for (NSDictionary rawRow : rawRows) {
      Object pk = rawRow.objectForKey(pkAttr.name());
      itemPrimaryKeys.addObject(pk);
    }

    NSMutableArray<EOAttribute> tagsFetchAttributes = new NSMutableArray<EOAttribute>();
    // MS: We put this in just because we want to force it to do the join ... We have to
    // pull them out later.
    tagsFetchAttributes.addObjectsFromArray(_entity.primaryKeyAttributes());

    ERXEOAttribute tagIDAttribute = new ERXEOAttribute(_entity, _tagsRelationship.name() + ".id");
    tagIDAttribute.setName("id");
    tagsFetchAttributes.addObject(tagIDAttribute);
    tagsFetchAttributes.addObject(tagNameAttribute);

    EOAttribute countAttribute = ERXEOAccessUtilities.createAggregateAttribute(editingContext, "COUNT", ERTag.NAME_KEY, _tagEntity.name(), Number.class, "i", "tagCount", "t2");
    tagsFetchAttributes.addObject(countAttribute);

    EOQualifier idQualifier = new ERXKey<Object>("id").in(itemPrimaryKeys);
    EOFetchSpecification tagsFetchSpec = new EOFetchSpecification(_entity.name(), idQualifier, null);
    EOSQLExpression tagsSqlExpression = sqlHelper.sqlExpressionForFetchSpecification(editingContext, tagsFetchSpec, 0, -1, tagsFetchAttributes);
    NSMutableArray<EOAttribute> tagsGroupByAttributes = new NSMutableArray<EOAttribute>(new EOAttribute[] { tagNameAttribute, tagIDAttribute });
    sqlHelper.addGroupByClauseToExpression(tagsGroupByAttributes, tagsSqlExpression);

    // MS: This is lame, but the dynamic attribute is not properly resolved
    // inside of EOSQLExpression because it's not actually part of the entity,
    // so you can't order-by one of these attributes.  So we just have to stick
    // it on the end and hope for the best.
    tagsSqlExpression.setStatement(tagsSqlExpression.statement() + " ORDER BY tagCount DESC");

    for (EOAttribute attribute : _entity.primaryKeyAttributes()) {
      sqlHelper.removeSelectFromExpression(attribute, tagsSqlExpression);
      tagsFetchAttributes.removeObject(attribute);
    }

    NSMutableArray<String> relatedTagNames = new NSMutableArray<String>();
    NSArray<NSDictionary> tagsRawRows = ERXEOAccessUtilities.rawRowsForSQLExpression(editingContext, _entity.model(), tagsSqlExpression, tagsFetchAttributes);
    for (NSDictionary rawRow : tagsRawRows) {
      String name = (String) rawRow.objectForKey("tagName");
      relatedTagNames.addObject(name);
    }

    return relatedTagNames;
  }

  /**
   * Takes the result of a tagCount call and an array of categories and 
   * distributes the entries in the tagCount hash evenly across the
   * categories based on the count value for each tag.
   *
   * Typically, this is used to display a 'tag cloud' in your UI.
   *
   * @param categoryList An array containing the categories to split the tags
   * @return a dictionary mapping each tag name to its corresponding category 
   */
  public <U> NSDictionary<String, U> cloud(EOEditingContext editingContext, NSArray<U> categoryList) {
    return cloud(tagCount(editingContext), categoryList);
  }

  /**
   * Takes the result of a tagCount call and an array of categories and 
   * distributes the entries in the tagCount hash evenly across the
   * categories based on the count value for each tag.
   *
   * Typically, this is used to display a 'tag cloud' in your UI.
   *
   * @param tagHash the tag dictionary returned from a tagCount call
   * @param categoryList An array containing the categories to split the tags
   * @return a dictionary mapping each tag name to its corresponding category 
   */
  public <U> NSDictionary<String, U> cloud(NSDictionary<String, Integer> tagHash, NSArray<U> categoryList) {
    int min = 0;
    int max = 0;
    for (Integer count : tagHash.allValues()) {
      if (count.intValue() > max) {
        max = count.intValue();
      }
      if (count.intValue() < min) {
        min = count.intValue();
      }
    }

    NSMutableDictionary<String, U> cloud = new NSMutableDictionary<String, U>();

    int divisor = ((max - min) / categoryList.count()) + 1;
    for (Map.Entry<String, Integer> entry : tagHash.entrySet()) {
      U obj = categoryList.objectAtIndex((entry.getValue().intValue() - min) / divisor);
      cloud.setObjectForKey(obj, entry.getKey());
    }

    return cloud;
  }

  /**
   * Returns an array of all of the available tags in the system.
   * 
   * @param editingContext the editing context to fetch into
   * @return an array of matching tags
   */
  @SuppressWarnings("unchecked")
  public NSArray<String> fetchAllTags(EOEditingContext editingContext) {
    NSArray<ERTag> erTags = ERTag.fetchAllERTags(editingContext);
    NSArray<String> tags = (NSArray<String>) erTags.valueForKey(ERTag.NAME_KEY);
    return tags;
  }

  /**
   * Returns an array of all of the available tags in the system that start with
   * the given string.
   * 
   * @param startsWith the prefix to lookup
   * @param editingContext the editing context to fetch into
   * @return an array of matching tags
   */
  @SuppressWarnings("unchecked")
  public NSArray<String> fetchTagsLike(EOEditingContext editingContext, String startsWith) {
    NSArray<ERTag> erTags = ERTag.fetchERTags(editingContext, ERTag.NAME.likeInsensitive(startsWith + "*"), null);
    NSArray<String> tags = (NSArray<String>) erTags.valueForKey(ERTag.NAME_KEY);
    return tags;
  }
  
  protected EOQualifier additionalTagCountQualifier () {
	  return null;
  }

  //I just can't muster the strength the port this one right now -- that query is ROUGH :)
  ///**
  //* Finds other records that share the most tags with the record passed
  //* as the +related+ parameter. Useful for constructing 'Related' or 
  //* 'See Also' boxes and lists.
  //*
  //* The options are:
  //* 
  //* +:limit+: defaults to 5, which means the method will return the top 5 records
  //* that share the greatest number of tags with the passed one.        
  //* +:conditions+: any additional conditions that should be appended to the 
  //* WHERE clause of the finder SQL. Just like regular +ActiveRecord::Base#find+ methods.
  //*/
  //public NSArray<T> findRelatedTagged(EOEditingContext editingContext, T related, int limit) {
  //  NSArray<EOSortOrdering> sortOrderings = null;
  //  EOFetchSpecification fetchSpec = new EOFetchSpecification(_entity.name(), null, sortOrderings);
  //
  //  NSArray<EOAttribute> entityAttributes = _entity.attributesToFetch();
  //  NSMutableArray<EOAttribute> fetchAttributes = entityAttributes.mutableClone();
  //
  //  EOAttribute countAttribute = ERXEOAccessUtilities.createAggregateAttribute(editingContext, "COUNT", ERTag.NAME_KEY, _tagEntity.name(), Number.class, "i", "tagCount");
  //  fetchAttributes.addObject(countAttribute);
  //
  //  ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(_entity.model());
  //  
  //  
  //  EOSQLExpression sqlExpression = sqlHelper.sqlExpressionForFetchSpecification(editingContext, fetchSpec, 0, limit);
  //  sqlHelper.addGroupByClauseToExpression(editingContext, fetchSpec, sqlExpression);
  //  if (inclusion == ERTag.Inclusion.ALL) {
  //    sqlHelper.addHavingCountClauseToExpression(EOQualifier.QualifierOperatorEqual, tagNames.count(), sqlExpression);
  //  }
  //
  //  NSArray<NSDictionary> rawRows = ERXEOAccessUtilities.rawRowsForSQLExpression(editingContext, _entity.model(), sqlExpression, sqlHelper.attributesToFetchForEntity(fetchSpec, _entity));
  //  NSArray<T> objs;
  //  objs = ERXEOControlUtilities.faultsForRawRowsFromEntity(editingContext, rawRows, _entity.name());
  //  objs = ERXEOControlUtilities.objectsForFaultWithSortOrderings(editingContext, objs, fetchSpec.sortOrderings());
  //  return objs;
  //}
  //def find_related_tagged(related, options = {})
  //  related_id = related.is_a?(self) ? related.id : related
  //  options = { :limit => 5 }.merge(options)
  //
  //  o, o_pk, o_fk, t, tn, t_pk, t_fk, jt = set_locals_for_sql
  //  sql = "SELECT o.*, COUNT(jt2.#{o_fk}) AS count FROM #{o} o, #{jt} jt, #{t} t, #{jt} jt2 
  //         WHERE jt.#{o_fk}=#{related_id} AND t.#{t_pk} = jt.#{t_fk} 
  //         AND jt2.#{o_fk} != jt.#{o_fk} 
  //         AND jt2.#{t_fk}=jt.#{t_fk} AND o.#{o_pk} = jt2.#{o_fk}"
  //  sql << " AND #{sanitize_sql(options[:conditions])}" if options[:conditions]
  //  sql << " GROUP BY o.#{o_pk}"
  //  sql << " ORDER BY count DESC"
  //  add_limit!(sql, options)
  //
  //  find_by_sql(sql)
  //end
}
