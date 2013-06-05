package er.corebusinesslogic.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

/**
 *
 * @property ERCoreBusinessLogic1.languages
 */
public class ERCoreBusinessLogic1 extends ERXMigrationDatabase.Migration {
    
    public ERCoreBusinessLogic1() {
       super(ERXProperties.arrayForKey("ERCoreBusinessLogic1.languages"));
    }
    
    @Override
    public NSArray<ERXModelVersion> modelDependencies() {
        return null;
    }

    @Override
    public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
        // DO NOTHING
    }

    @Override
    public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
        ERXMigrationTable ercMailMessageTable = database.newTableNamed("ERCMAIL_MESSAG");
        ercMailMessageTable.newStringColumn("BCC_ADDR", 10000000, true);
        ercMailMessageTable.newStringColumn("CC_ADDR", 10000000, true);
        ercMailMessageTable.newFlagBooleanColumn("CONTENT_GZIPPED", false);
        ercMailMessageTable.newTimestampColumn("CREATED", false);
        ercMailMessageTable.newTimestampColumn("DATE_SENT", true);
        ercMailMessageTable.newStringColumn("EXCEPTION_REASON", 1000, true);
        ercMailMessageTable.newStringColumn("FROM_ADDR", 255, false);
        ercMailMessageTable.newIntegerColumn("ID", false);
        ercMailMessageTable.newFlagBooleanColumn("IS_READ", false);
        ercMailMessageTable.newTimestampColumn("LAST_MODIFIED", false);
        ercMailMessageTable.newStringColumn("PLAIN_TEXT_", 10000000, true);
        ercMailMessageTable.newBlobColumn("plain_text_compressed", true);
        ercMailMessageTable.newStringColumn("REPLY_TO_ADDR", 1000, true);
        ercMailMessageTable.newFlagBooleanColumn("SHOULD_ARCHIVE_SENT_MAIL", false);
        ercMailMessageTable.newStringColumn("MAIL_STATE_ID", 4, false);
        ercMailMessageTable.newStringColumn("TEXT_", 10000000, true);
        ercMailMessageTable.newBlobColumn("TEXT_COMPRESSED", true);
        ercMailMessageTable.newStringColumn("TITLE", 255, false);
        ercMailMessageTable.newStringColumn("TO_ADDR", 10000000, false);
        ercMailMessageTable.newStringColumn("X_MAILER", 255, true);
        ercMailMessageTable.create();
        ercMailMessageTable.setPrimaryKey("ID");

        ERXMigrationTable ercMessageAttachmentTable = database.newTableNamed("ERCMESSAG_ATTACH");
        ercMessageAttachmentTable.newStringColumn("FILE_PATH", 1000, false);
        ercMessageAttachmentTable.newIntegerColumn("ID", false);
        ercMessageAttachmentTable.newIntegerColumn("MAIL_MESSAG_ID", false);
        ercMessageAttachmentTable.newStringColumn("MIME_TYPE", 255, false);
        ercMessageAttachmentTable.create();
        ercMessageAttachmentTable.setPrimaryKey("ID");

        ERXMigrationTable ercMailMessageArchiveTable = database.newTableNamed("ERCMAIL_MESSAG_ARCHIVE");
        ercMailMessageArchiveTable.newStringColumn("BCC_ADDR", 10000000, true);
        ercMailMessageArchiveTable.newStringColumn("CC_ADDR", 10000000, true);
        ercMailMessageArchiveTable.newFlagBooleanColumn("CONTENT_GZIPPED", false);
        ercMailMessageArchiveTable.newTimestampColumn("CREATED", false);
        ercMailMessageArchiveTable.newTimestampColumn("DATE_SENT", true);
        ercMailMessageArchiveTable.newStringColumn("EXCEPTION_REASON", 1000, true);
        ercMailMessageArchiveTable.newStringColumn("FROM_ADDR", 255, false);
        ercMailMessageArchiveTable.newIntegerColumn("ID", false);
        ercMailMessageArchiveTable.newFlagBooleanColumn("IS_READ", false);
        ercMailMessageArchiveTable.newTimestampColumn("LAST_MODIFIED", false);
        ercMailMessageArchiveTable.newStringColumn("PLAIN_TEXT_", 10000000, true);
        ercMailMessageArchiveTable.newBlobColumn("plain_text_compressed", true);
        ercMailMessageArchiveTable.newStringColumn("REPLY_TO_ADDR", 1000, true);
        ercMailMessageArchiveTable.newFlagBooleanColumn("SHOULD_ARCHIVE_SENT_MAIL", false);
        ercMailMessageArchiveTable.newStringColumn("MAIL_STATE_ID", 4, false);
        ercMailMessageArchiveTable.newStringColumn("TEXT_", 10000000, true);
        ercMailMessageArchiveTable.newBlobColumn("TEXT_COMPRESSED", true);
        ercMailMessageArchiveTable.newStringColumn("TITLE", 255, false);
        ercMailMessageArchiveTable.newStringColumn("TO_ADDR", 10000000, false);
        ercMailMessageArchiveTable.newStringColumn("X_MAILER", 255, true);
        ercMailMessageArchiveTable.create();
        ercMailMessageArchiveTable.setPrimaryKey("ID");

        ercMessageAttachmentTable.addForeignKey("MAIL_MESSAG_ID", "ERCMAIL_MESSAG", "ID");
    }

}