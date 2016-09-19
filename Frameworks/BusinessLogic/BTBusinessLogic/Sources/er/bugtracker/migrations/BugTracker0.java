package er.bugtracker.migrations;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Difficulty;
import er.bugtracker.Priority;
import er.bugtracker.RequirementSubType;
import er.bugtracker.RequirementType;
import er.bugtracker.TestItemState;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;

public class BugTracker0 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
  @Override
  public NSArray<ERXModelVersion> modelDependencies() {
    return new NSArray<>(new ERXModelVersion[] { new ERXModelVersion("ERTaggable", 0),  new ERXModelVersion("ERAttachment", 1) });
  }

  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    // DO NOTHING
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    ERXMigrationTable priorityTable = database.newTableNamed("PRIORITY");
    priorityTable.newStringColumn("ID", 4, false);
    priorityTable.newIntegerColumn("SORT_ORDER", false);
    priorityTable.newStringColumn("DESCRIPTION", 50, false);
    priorityTable.create();
    priorityTable.setPrimaryKey("ID");

    ERXMigrationTable xRequirementTestItemTable = database.newTableNamed("REQ_TEST_ITEM");
    xRequirementTestItemTable.newIntegerColumn("BUG_ID", false);
    xRequirementTestItemTable.newIntegerColumn("ID", false);
    xRequirementTestItemTable.create();
    xRequirementTestItemTable.setPrimaryKey("BUG_ID", "ID");

    ERXMigrationTable commentTable = database.newTableNamed("Comment");
    commentTable.newIntegerColumn("BUG_ID", false);
    commentTable.newTimestampColumn("DATE_SUBMITTED", true);
    commentTable.newIntegerColumn("id", false);
    commentTable.newIntegerColumn("ORIGINATOR_ID", false);
    commentTable.newIntegerColumn("PARENT_ID", true);
    commentTable.newLargeStringColumn("DESCRIPTION", true);
    commentTable.create();
    commentTable.setPrimaryKey("id");

    ERXMigrationTable bugTable = database.newTableNamed("BUG");
    bugTable.newIntegerColumn("COMP_ID", false);
    bugTable.newTimestampColumn("DATE_MODIFIED", true);
    bugTable.newTimestampColumn("DATE_SUBMITTED", true);
    bugTable.newIntegerColumn("ID", false);
    bugTable.newIntBooleanColumn("IS_FEATURE_REQUEST", false);
    bugTable.newIntBooleanColumn("IS_READ", false);
    bugTable.newIntegerColumn("ORIGINATOR_ID", false);
    bugTable.newIntegerColumn("PEOPLE_ID", false);
    bugTable.newIntegerColumn("PREVIOUS_OWNER_ID", true);
    bugTable.newStringColumn("PR_ID", 4, false);
    bugTable.newIntegerColumn("RELEASE_ID", false);
    bugTable.newStringColumn("STATE_ID", 4, false);
    bugTable.newStringColumn("SUBJECT", 50, false);
    bugTable.newStringColumn("type", 1, true);
    bugTable.create();
    bugTable.setPrimaryKey("ID");

    ERXMigrationTable releaseTable = database.newTableNamed("RELEASE");
    releaseTable.newTimestampColumn("DATE_DUE", false);
    releaseTable.newIntegerColumn("ID", false);
    releaseTable.newIntBooleanColumn("IS_OPEN", false);
    releaseTable.newStringColumn("NAME", 50, false);
    releaseTable.create();
    releaseTable.setPrimaryKey("ID");

    ERXMigrationTable componentTable = database.newTableNamed("COMPONENT");
    componentTable.newIntegerColumn("ID", false);
    componentTable.newIntegerColumn("PEOPLE_ID", false);
    componentTable.newIntegerColumn("PARENT_ID", true);
    componentTable.newStringColumn("DESCRIPTION", 1000, false);
    componentTable.create();
    componentTable.setPrimaryKey("ID");

    ERXMigrationTable xCommentAttachmentTable = database.newTableNamed("XCommentAttachment");
    xCommentAttachmentTable.newIntegerColumn("commentId", false);
    xCommentAttachmentTable.newIntegerColumn("eRAttachmentId", false);
    xCommentAttachmentTable.create();
    xCommentAttachmentTable.setPrimaryKey("eRAttachmentId", "commentId");

    ERXMigrationTable xBugTestItemTable = database.newTableNamed("BUG_TEST_ITEM");
    xBugTestItemTable.newIntegerColumn("BUG_ID", false);
    xBugTestItemTable.newIntegerColumn("ID", false);
    xBugTestItemTable.create();
    xBugTestItemTable.setPrimaryKey("BUG_ID", "ID");

    ERXMigrationTable frameworkTable = database.newTableNamed("FRAMEWORK");
    frameworkTable.newIntegerColumn("ID", false);
    frameworkTable.newStringColumn("NAME", 50, false);
    frameworkTable.newIntegerColumn("ORDERING", false);
    frameworkTable.newTimestampColumn("OWNED_SINCE", true);
    frameworkTable.newIntegerColumn("USER_ID", true);
    frameworkTable.create();
    frameworkTable.setPrimaryKey("ID");

    ERXMigrationTable testItemTable = database.newTableNamed("TEST_ITEM");
    testItemTable.newLargeStringColumn("COMMENTS", true);
    testItemTable.newIntegerColumn("MODULE_ID", false);
    testItemTable.newStringColumn("CONTROLLED", 50, false);
    testItemTable.newTimestampColumn("DATE_CREATED", false);
    testItemTable.newIntegerColumn("ID", false);
    testItemTable.newStringColumn("STATE_ID", 4, false);
    testItemTable.newIntegerColumn("TESTED_BY_ID", true);
    testItemTable.newLargeStringColumn("DESCRIPTION", true);
    testItemTable.newStringColumn("TITLE", 100, false);
    testItemTable.create();
    testItemTable.setPrimaryKey("ID");

    ERXMigrationTable peopleTable = database.newTableNamed("PEOPLE");
    peopleTable.newStringColumn("EMAIL", 50, true);
    peopleTable.newIntegerColumn("ID", false);
    peopleTable.newIntBooleanColumn("IS_ACTIVE", false);
    peopleTable.newIntBooleanColumn("IS_ADMIN", false);
    peopleTable.newIntBooleanColumn("IS_CUSTOMER_SERVICE", false);
    peopleTable.newIntBooleanColumn("IS_ENGINEERING", false);
    peopleTable.newStringColumn("LOGIN", 16, false);
    peopleTable.newStringColumn("NAME", 50, true);
    peopleTable.newStringColumn("PASSWORD", 16, false);
    peopleTable.newStringColumn("TEAM", 16, true);
    peopleTable.create();
    peopleTable.setPrimaryKey("ID");

    ERXMigrationTable difficultyTable = database.newTableNamed("DIFFICULTY");
    difficultyTable.newStringColumn("DESCRIPTION", 50, false);
    difficultyTable.newIntegerColumn("ID", false);
    difficultyTable.create();
    difficultyTable.setPrimaryKey("ID");

    ERXMigrationTable requirementSubTypeTable = database.newTableNamed("REQ_SUB_TYPE");
    requirementSubTypeTable.newIntegerColumn("REQ_SUB_TYPE_ID", false);
    requirementSubTypeTable.newStringColumn("SUB_TYPE_DESC", 50, false);
    requirementSubTypeTable.create();
    requirementSubTypeTable.setPrimaryKey("REQ_SUB_TYPE_ID");

    ERXMigrationTable requirementTypeTable = database.newTableNamed("REQ_TYPE");
    requirementTypeTable.newIntegerColumn("REQ_TYPE_ID", false);
    requirementTypeTable.newStringColumn("TYPE_DESCRIPTION", 50, false);
    requirementTypeTable.create();
    requirementTypeTable.setPrimaryKey("REQ_TYPE_ID");

    ERXMigrationTable testItemStateTable = database.newTableNamed("TEST_ITEM_STATE");
    testItemStateTable.newStringColumn("NAME", 50, false);
    testItemStateTable.newStringColumn("ID", 4, false);
    testItemStateTable.newIntegerColumn("SORT_ORDER", false);
    testItemStateTable.create();
    testItemStateTable.setPrimaryKey("ID");

    ERXMigrationTable requirementTable = database.existingTableNamed("BUG");
    requirementTable.newIntegerColumn("DIFFICULTY_ID", true);
    requirementTable.newIntegerColumn("SUB_TYPE_ID", true);
    requirementTable.newIntegerColumn("REQ_TYPE_ID", true);

    xRequirementTestItemTable.addForeignKey("BUG_ID", "BUG", "ID");
    xRequirementTestItemTable.addForeignKey("ID", "TEST_ITEM", "ID");
    commentTable.addForeignKey("BUG_ID", "BUG", "ID");
    commentTable.addForeignKey("ORIGINATOR_ID", "PEOPLE", "ID");
    commentTable.addForeignKey("PARENT_ID", "Comment", "id");
    bugTable.addForeignKey("COMP_ID", "COMPONENT", "ID");
    bugTable.addForeignKey("ORIGINATOR_ID", "PEOPLE", "ID");
    bugTable.addForeignKey("PEOPLE_ID", "PEOPLE", "ID");
    bugTable.addForeignKey("PREVIOUS_OWNER_ID", "PEOPLE", "ID");
    bugTable.addForeignKey("PR_ID", "PRIORITY", "ID");
    bugTable.addForeignKey("RELEASE_ID", "RELEASE", "ID");
    componentTable.addForeignKey("PEOPLE_ID", "PEOPLE", "ID");
    componentTable.addForeignKey("PARENT_ID", "COMPONENT", "ID");
    xCommentAttachmentTable.addForeignKey("commentId", "Comment", "id");
    xCommentAttachmentTable.addForeignKey("eRAttachmentId", "ERAttachment", "id");
    xBugTestItemTable.addForeignKey("BUG_ID", "BUG", "ID");
    xBugTestItemTable.addForeignKey("ID", "TEST_ITEM", "ID");
    frameworkTable.addForeignKey("USER_ID", "PEOPLE", "ID");
    testItemTable.addForeignKey("MODULE_ID", "COMPONENT", "ID");
    testItemTable.addForeignKey("TESTED_BY_ID", "PEOPLE", "ID");
    testItemTable.addForeignKey("STATE_ID", "TEST_ITEM_STATE", "ID");
    requirementTable.addForeignKey("DIFFICULTY_ID", "DIFFICULTY", "ID");
    requirementTable.addForeignKey("SUB_TYPE_ID", "REQ_SUB_TYPE", "REQ_SUB_TYPE_ID");
    requirementTable.addForeignKey("REQ_TYPE_ID", "REQ_TYPE", "REQ_TYPE_ID");
  }

  public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable {
    Priority critical = Priority.clazz.createAndInsertObject(editingContext);
    critical._setValueForPrimaryKey("crtl", "id");
    critical.setSortOrder(Integer.valueOf(1));
    critical.setTextDescription("Critical");

    Priority high = Priority.clazz.createAndInsertObject(editingContext);
    high._setValueForPrimaryKey("high", "id");
    high.setSortOrder(Integer.valueOf(2));
    high.setTextDescription("High");

    Priority medium = Priority.clazz.createAndInsertObject(editingContext);
    medium._setValueForPrimaryKey("medm", "id");
    medium.setSortOrder(Integer.valueOf(3));
    medium.setTextDescription("Medium");

    Priority low = Priority.clazz.createAndInsertObject(editingContext);
    low._setValueForPrimaryKey("low", "id");
    low.setSortOrder(Integer.valueOf(4));
    low.setTextDescription("Low");

    TestItemState open = TestItemState.clazz.createAndInsertObject(editingContext);
    open._setValueForPrimaryKey("open", "oid");
    open.setSortOrder(Integer.valueOf(1));
    open.setName("Open");

    TestItemState bug = TestItemState.clazz.createAndInsertObject(editingContext);
    bug._setValueForPrimaryKey("bug", "oid");
    bug.setSortOrder(Integer.valueOf(2));
    bug.setName("Bug");

    TestItemState closed = TestItemState.clazz.createAndInsertObject(editingContext);
    closed._setValueForPrimaryKey("clsd", "oid");
    closed.setSortOrder(Integer.valueOf(3));
    closed.setName("Closed");

    TestItemState requirement = TestItemState.clazz.createAndInsertObject(editingContext);
    requirement._setValueForPrimaryKey("rqmt", "oid");
    requirement.setSortOrder(Integer.valueOf(4));
    requirement.setName("Requirement");

    Difficulty hardDifficulty = Difficulty.clazz.createAndInsertObject(editingContext);
    hardDifficulty._setValueForPrimaryKey(Integer.valueOf(1), "id");
    hardDifficulty.setDifficultyDescription("Hard");

    Difficulty mediumDifficulty = Difficulty.clazz.createAndInsertObject(editingContext);
    mediumDifficulty._setValueForPrimaryKey(Integer.valueOf(2), "id");
    mediumDifficulty.setDifficultyDescription("Medium");

    Difficulty easyDifficulty = Difficulty.clazz.createAndInsertObject(editingContext);
    easyDifficulty._setValueForPrimaryKey(Integer.valueOf(3), "id");
    easyDifficulty.setDifficultyDescription("Easy");

    RequirementSubType essential = RequirementSubType.clazz.createAndInsertObject(editingContext);
    essential._setValueForPrimaryKey(Integer.valueOf(1), "id");
    essential.setSubTypeDescription("Essential");

    RequirementSubType important = RequirementSubType.clazz.createAndInsertObject(editingContext);
    important._setValueForPrimaryKey(Integer.valueOf(2), "id");
    important.setSubTypeDescription("Important");

    RequirementSubType useful = RequirementSubType.clazz.createAndInsertObject(editingContext);
    useful._setValueForPrimaryKey(Integer.valueOf(3), "id");
    useful.setSubTypeDescription("Useful");

    RequirementSubType cosmetic = RequirementSubType.clazz.createAndInsertObject(editingContext);
    cosmetic._setValueForPrimaryKey(Integer.valueOf(4), "id");
    cosmetic.setSubTypeDescription("Cosmetic");

    RequirementType interfaceType = RequirementType.clazz.createAndInsertObject(editingContext);
    interfaceType._setValueForPrimaryKey(Integer.valueOf(1), "id");
    interfaceType.setTypeDescription("Interface");

    RequirementType documentationType = RequirementType.clazz.createAndInsertObject(editingContext);
    documentationType._setValueForPrimaryKey(Integer.valueOf(2), "id");
    documentationType.setTypeDescription("Documentation");

    RequirementType backendType = RequirementType.clazz.createAndInsertObject(editingContext);
    backendType._setValueForPrimaryKey(Integer.valueOf(3), "id");
    backendType.setTypeDescription("Backend");

    RequirementType communicationType = RequirementType.clazz.createAndInsertObject(editingContext);
    communicationType._setValueForPrimaryKey(Integer.valueOf(4), "id");
    communicationType.setTypeDescription("Communication");
  }
}