package er.indexing.example;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.jdbc.ERXSQLHelper;
import er.indexing.ERIndexing;
import er.indexing.example.eof.Asset;
import er.indexing.example.eof.AssetGroup;
import er.indexing.example.eof.Tag;

public class DataCreator {
	
	private EOEditingContext ec;

	private static final Logger log = LoggerFactory.getLogger(DataCreator.class);

	public static void main(String[] args) {
		new DataCreator().createAll();
	}

	private NSDictionary optionsWithPrimaryKeySupportDisabled(NSDictionary options) {
		NSMutableDictionary mutableOptions = options.mutableClone();
		mutableOptions.setObjectForKey("NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		mutableOptions.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		return mutableOptions.immutableClone();
	}

	private void createPrimaryKeySupportForModel(EOModel eomodel, EOAdaptorChannel channel, EOSynchronizationFactory syncFactory) {
		try {
			// AK: the (Object) cast is needed, because in 5.4 new NSArray(obj)
			// != new NSArray(array).
			NSArray pkSupportExpressions = syncFactory.primaryKeySupportStatementsForEntityGroups(new NSArray((Object) eomodel.entities()));
			Enumeration enumeration = pkSupportExpressions.objectEnumerator();
			while (enumeration.hasMoreElements()) {
				EOSQLExpression expression = (EOSQLExpression) enumeration.nextElement();
				channel.evaluateExpression(expression);
			}
		} catch (Exception e) {
		}
	}

	private void createTables(boolean dropTables) {
		for (Enumeration e = EOModelGroup.defaultGroup().models().objectEnumerator(); e.hasMoreElements();) {
			final EOModel eomodel = (EOModel) e.nextElement();
			EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, eomodel.name());
			dbc.lock();
			try {
				EOAdaptorChannel channel = dbc.availableChannel().adaptorChannel();
				if (eomodel.adaptorName().contains("JDBC")) {
					EOSynchronizationFactory syncFactory = (EOSynchronizationFactory) channel.adaptorContext().adaptor().synchronizationFactory();
					ERXSQLHelper helper = ERXSQLHelper.newSQLHelper(channel);
					NSDictionary options = helper.defaultOptionDictionary(true, dropTables);

					// Primary key support creation throws an unwanted exception
					// if
					// EO_PK_TABLE already
					// exists (e.g. in case of MySQL), so we add pk support in a
					// stand-alone step
					options = optionsWithPrimaryKeySupportDisabled(options);
					createPrimaryKeySupportForModel(eomodel, channel, syncFactory);

					String sqlScript = syncFactory.schemaCreationScriptForEntities(eomodel.entities(), options);
					log.info("Creating tables: {}", eomodel.name());
					ERXJDBCUtilities.executeUpdateScript(channel, sqlScript, true);
				}
			} catch (SQLException ex) {
				log.error("Can't update", ex);
			} finally {
				dbc.unlock();
			}
		}

	}

	NSMutableArray<Asset> assets = new NSMutableArray<Asset>();
	NSMutableArray<AssetGroup> groups = new NSMutableArray<AssetGroup>();
	NSMutableArray<Tag> tags = new NSMutableArray<Tag>();
	NSArray<String> words = new NSArray<String>();

	public void createAll() {
		createTables();
		clearIndex();
		createDummyData();
	}

	public void clearIndex() {
	    ERIndexing.indexing().clear();
	}

	public void createTables() {
		ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			boolean dropTables = ERXProperties.booleanForKeyWithDefault("dropTables", true);
			createTables(dropTables);
		} finally {
			ec.unlock();
		}
	}

	public void createDummyData() {
		ec = ERXEC.newEditingContext();
		ec.lock();
		try {
			doCreateDummyData();
		} finally {
			ec.unlock();
		}
	}

	private int randomInt(int max) {
		return new Random().nextInt(max);
	}

	private <T> T randomObject(NSArray<T> array) {
		return array.objectAtIndex(randomInt(array.count()));
	}

	private String randomWord() {
		return randomObject(words);
	}

	private String randomText(int max) {
		StringBuilder content = new StringBuilder();
		while (true) {
			String nextWord = randomWord();
			if ((content.length() + nextWord.length() + 1) < max) {
				content.append(nextWord).append(' ');
			} else {
				break;
			}
		}
		return content.toString();
	}

	private Tag randomTag() {
		return randomObject(tags);
	}

	private AssetGroup randomAssetGroup() {
		return randomObject(groups);
	}

	private BigDecimal randomPrice() {
		return BigDecimal.valueOf((double) randomInt(10000) / (double) 100).setScale(2, BigDecimal.ROUND_DOWN);
	}

	private NSTimestamp randomTime() {
		return new NSTimestamp(randomInt((int) (System.currentTimeMillis() / 1000)) * 1000);
	}

	private void doCreateDummyData() {
		try {
			log.info("load");

			String wordFile = ERXFileUtilities.stringFromFile(new File("/usr/share/dict/words"));
			words = NSArray.componentsSeparatedByString(wordFile, "\n");
			log.info("loaded words: {}", words.count());

			int MAX = 100;
			int MAX_ASSETS = MAX * 10;

			for (int i = 0; i < MAX; i++) {
				Tag tag = Tag.clazz.createAndInsertObject(ec);
				tag.setName(randomWord());
				tags.addObject(tag);
			}
			log.info("created tags: {}", tags.count());

			for (int i = 0; i < MAX; i++) {
				AssetGroup group = AssetGroup.clazz.createAndInsertObject(ec);
				group.setName(randomWord());
				groups.addObject(group);
			}
			log.info("created groups: {}", groups.count());

			for (int i = 0; i < MAX_ASSETS; i++) {
				Asset asset = Asset.clazz.createAndInsertObject(ec);
				asset.setAssetGroup(randomAssetGroup());
				asset.setCreationDate(randomTime());
				asset.setUserCount((long) randomInt(10000));
				asset.setPrice(randomPrice());

				for (int j = 0; j < 10; j++) {
					asset.addToTags(randomTag());
				}

				asset.setContent(randomText(1000));

				asset.setGenericInfo(randomText(1000));

				assets.addObject(asset);
			}
			log.info("created assets: {}", assets.count());

			ec.saveChanges();
			log.info("fin: {}", words.count());
		} catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}
}
