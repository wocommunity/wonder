package er.neo4jadaptor.test.tools;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXModelGroup;
import er.neo4jadaptor.Neo4JAdaptor;
import er.neo4jadaptor.Neo4JContext;
import er.neo4jadaptor.ersatz.neo4j.Neo4JErsatz;
import er.neo4jadaptor.storage.Store;
import er.neo4jadaptor.utils.cursor.Cursor;

public class Tools {
	private static final String MODEL_NAME = "MyModel";
	
	static {
		ERXApplication.setup(new String [] {});
		Neo4JAdaptor.init();
	}
	
	public static void ensureInitialized() {
		// do nothing, it will trigger static class initializer
	}
	
	public static void cleanup() {
		Neo4JContext context = context();
		EOModel model = ERXModelGroup.globalModelGroup().modelNamed(MODEL_NAME);
		
		context.beginTransaction();
		try {
			for (EOEntity e : model.entities()) {
				Store<?, Neo4JErsatz> store = context.entityStoreForEntity(e);
				
				Cursor<Neo4JErsatz> cursor = store.query(null);
				
				try {
					while (cursor.hasNext()) {
						store.delete(cursor.next());
					}
				} finally {
					cursor.close();
				}
			}
			context.commitTransaction();
		} finally {
			context.rollbackTransaction();
		}
	}
	
	private static Neo4JContext context() {
		EOEditingContext ec = ERXEC.newEditingContext();
		EODatabaseContext dbContext = EOUtilities.databaseContextForModelNamed(ec, MODEL_NAME);
		Neo4JContext context = (Neo4JContext) dbContext.availableChannel().adaptorChannel().adaptorContext();
		
		return context;
	}
}

