package er.taggable.migrations;

import java.sql.SQLException;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXModelVersion;
import er.taggable.ERTaggableEntity;
import er.taggable.model.ERTag;

/**
 * ERTaggableEntity0 provides a superclass for creating the join table
 * for your taggable entities.
 * 
 * @author mschrag
 */
public class ERTaggableEntity0 extends ERXMigrationDatabase.Migration {
  private String _entityName;
  private String _tagEntityName;
  private String _tagsRelationshipName;

  /**
   * Constructs an ERTaggableEntity0.
   * 
   * @param entityName the name of your entity to tag
   */
  public ERTaggableEntity0(String entityName) {
    this(entityName, ERTaggableEntity.DEFAULT_TAGS_RELATIONSHIP_NAME, ERTag.ENTITY_NAME);
  }

  /**
   * Constructs an ERTaggableEntity0.
   * 
   * @param entityName the name of your entity to tag
   * @param tagsRelationshipName the name of the flattened to-many relationship to ERTag (defaults to "tags")
   */
  public ERTaggableEntity0(String entityName, String tagsRelationshipName) {
    this(entityName, tagsRelationshipName, ERTag.ENTITY_NAME);
  }

  /**
   * Constructs an ERTaggableEntity0.
   * 
   * @param entityName the name of your entity to tag
   * @param tagsRelationshipName the name of the flattened to-many relationship to ERTag (defaults to "tags")
   * @param tagEntityName the name of the tag entity (defaults to "ERTag")
   */
  public ERTaggableEntity0(String entityName, String tagsRelationshipName, String tagEntityName) {
    _entityName = entityName;
    _tagsRelationshipName = tagsRelationshipName;
    _tagEntityName = tagEntityName;
  }

  @Override
  public NSArray<ERXModelVersion> modelDependencies() {
    return new NSArray<>(new ERXModelVersion("ERTaggable", 0));
  }

  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // DO NOTHING
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    ERTaggableEntity0.upgrade(editingContext, database.adaptorChannel(), database.model(), _entityName, _tagsRelationshipName, _tagEntityName);
  }

  public static void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model, String itemEntityName) throws SQLException {
    ERTaggableEntity0.upgrade(editingContext, channel, model, itemEntityName, ERTaggableEntity.DEFAULT_TAGS_RELATIONSHIP_NAME);
  }

  public static void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model, String itemEntityName, String tagsRelationshipName) throws SQLException {
    ERTaggableEntity0.upgrade(editingContext, channel, model, itemEntityName, tagsRelationshipName, ERTag.ENTITY_NAME);
  }

  /**
   * If you don't want to subclass ERTaggableEntity0, you can call this static method from your
   * own migration.
   * 
   * @param editingContext the editing context
   * @param channel the adaptor channel
   * @param model the model
   * @param entityName the name of your entity to tag
   * @param tagsRelationshipName the name of the flattened to-many relationship to ERTag (defaults to "tags")
   * @param tagEntityName the name of the tag entity (defaults to "ERTag")
   * @throws SQLException if the update script fails 
   */
  @SuppressWarnings("unchecked")
  public static void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model, String entityName, String tagsRelationshipName, String tagEntityName) throws SQLException {
    EOEntity joinEntity = ERTaggableEntity.registerTaggable(model.entityNamed(entityName), tagsRelationshipName, model.modelGroup().entityNamed(tagEntityName), null);
    EODatabaseContext databaseContext = EODatabaseContext.registeredDatabaseContextForModel(model, editingContext);
    EOSchemaGeneration generation = databaseContext.adaptorContext().adaptor().synchronizationFactory();
    NSArray<EOSQLExpression> createTableStatements = generation.createTableStatementsForEntityGroup(new NSArray<>(joinEntity));
    ERXJDBCUtilities.executeUpdateScript(channel, ERXMigrationDatabase._stringsForExpressions(createTableStatements));
  }
}
