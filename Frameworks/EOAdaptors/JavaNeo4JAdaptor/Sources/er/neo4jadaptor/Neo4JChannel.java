package er.neo4jadaptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.neo4jadaptor.ersatz.Ersatz;
import er.neo4jadaptor.ersatz.webobjects.NSDictionaryErsatz;
import er.neo4jadaptor.storage.Store;
import er.neo4jadaptor.utils.cursor.Cursor;

public class Neo4JChannel <T extends Ersatz> extends EOAdaptorChannel {
	private static final Logger log = LoggerFactory.getLogger(Neo4JChannel.class);

	private boolean isOpen = false;
	
	
	private boolean isFetchInProgress = false;
	private EOEntity fetchedEntity;
	private Cursor<? extends Ersatz> fetchResult;
	private int fetchLimit; 
	private int countFetched = 0;
	private EOFetchSpecification fetchSpec;
	private int fetchTimeTaken;
		
	public Neo4JChannel(Neo4JContext<T> context) {
		super(context);
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void openChannel() {
		isOpen = true;
	}


	@Override
	public void closeChannel() {
		isOpen = false;
	}

	@Override
	public NSDictionary<String, Object> primaryKeyForNewRowWithEntity(EOEntity entity) {
		return adaptorContext()._newPrimaryKey(null, entity);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Neo4JContext<T> adaptorContext() {
		return (Neo4JContext<T>) super.adaptorContext();
	}

	@Override
	public void insertRow(NSDictionary<String, Object> row, EOEntity entity) {
		Store<Ersatz, ?> store = adaptorContext().entityStoreForEntity(entity);
		NSDictionaryErsatz ultimate = NSDictionaryErsatz.full(entity, row);
		
		store.insert(ultimate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeStoredProcedure(EOStoredProcedure paramEOStoredProcedure, NSDictionary paramNSDictionary) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cancelFetch() {
		isFetchInProgress = false;
		if (fetchResult != null) {
			fetchResult.close();
		}
		fetchResult = null;
		fetchedEntity = null;
		
		log.debug("Fetch took {}ms and returned {} results (query from {}: {})", fetchTimeTaken, countFetched, fetchSpec.entityName(), fetchSpec.qualifier());
	}

	@Override
	public boolean isFetchInProgress() {
		return isFetchInProgress;
	}
	
	private void beginFetch(EOEntity entity, EOFetchSpecification fetchSpec) {
		isFetchInProgress = true;
		fetchedEntity = entity;
		countFetched = 0;
		this.fetchSpec = fetchSpec;
		fetchTimeTaken = 0;
	}

	@Override
	public void selectAttributes(NSArray<EOAttribute> attributes, EOFetchSpecification fetchSpec, boolean isLocking, EOEntity entity) {
		beginFetch(entity, fetchSpec);
		Store<?, ?> store = adaptorContext().entityStoreForEntity(entity);
		
		this.fetchLimit = fetchSpec.fetchLimit();
		try {
			long before = System.currentTimeMillis();
			fetchResult = store.query(fetchSpec.qualifier());
			
			if (fetchSpec.sortOrderings() != null && ! fetchSpec.sortOrderings().isEmpty()) {
				fetchResult = er.neo4jadaptor.utils.EOUtilities.sort(fetchResult, entity, fetchSpec.sortOrderings());
			}
			
			long after = System.currentTimeMillis();
			
			fetchTimeTaken += (after - before);
		} catch (RuntimeException e) {
			cancelFetch();
			throw e;
		}
	}

	@Override
	public NSMutableDictionary<String, Object> fetchRow() {
		if (fetchLimit != 0 && countFetched >= fetchLimit) {
			// fetch limit reached
			return null;
		}
		if (fetchResult == null) {
			return null;
		}
		long before = System.currentTimeMillis();
		if (! fetchResult.hasNext()) {
			return null;
		} else {
			Ersatz ultimate = fetchResult.next();
			long after = System.currentTimeMillis();
			NSMutableDictionary<String, Object> ret = toSnapshot(fetchedEntity, ultimate);
			
			countFetched++;
			fetchTimeTaken += (after - before);
			
			return ret;
		}
	}
	
	public NSMutableDictionary<String, Object> toSnapshot(EOEntity entity, Ersatz ultimate) {
		return NSDictionaryErsatz.toSnapshot(ultimate);
	}

	@Override
	public int updateValuesInRowsDescribedByQualifier(NSDictionary<String, Object> dict, EOQualifier qualifier, EOEntity entity) {
		NSDictionaryErsatz ultimate = NSDictionaryErsatz.partial(entity, dict);
		
		Store<Ersatz, T> store = adaptorContext().entityStoreForEntity(entity);
		Cursor<T> result = store.query(qualifier);
		
		try {
			int counter = 0;
			
			while (result.hasNext()) {
				T existingNeo = result.next();
				
				store.update(ultimate, existingNeo);
				
				counter++;
			}
			return counter;
		} finally {
			result.close();
		}
	}

	@Override
	public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
		Store<Ersatz, T> store = adaptorContext().entityStoreForEntity(entity);
		Cursor<T> result = store.query(qualifier);
		
		try {
			int counter = 0;
			
			while (result.hasNext()) {
				T existingNeo = result.next();
				
				store.delete(existingNeo);
				
				counter++;
			}
			return counter;
		} finally {
			result.close();
		}
	}

	@Override
	public void evaluateExpression(EOSQLExpression paramEOSQLExpression) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NSDictionary<?, ?> returnValuesForLastStoredProcedureInvocation() {
		throw new UnsupportedOperationException();
	}

	
	
	
	
	
	
	
	
	
	@Override
	public NSArray<EOAttribute> attributesToFetch() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NSArray<EOAttribute> describeResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttributesToFetch(NSArray<EOAttribute> paramNSArray) {
		// TODO Auto-generated method stub
	}

}
