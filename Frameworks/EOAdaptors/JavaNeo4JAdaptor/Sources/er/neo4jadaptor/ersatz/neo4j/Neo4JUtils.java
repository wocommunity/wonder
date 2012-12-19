package er.neo4jadaptor.ersatz.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;

public class Neo4JUtils {
	@SuppressWarnings("unused")
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Neo4JUtils.class);
	
	private static final Map<EORelationship, RelationshipType> resultsCache = new HashMap<EORelationship, RelationshipType>();
	
	/**
	 * Get Neo4J relationship type for the given EO relationship
	 * 
	 * @param r
	 * @return Neo4J relationship type
	 */
	public static RelationshipType getRelationshipType(EORelationship r) {
		RelationshipType ret = resultsCache.get(r);
		
		if (ret == null) {
			EOEntity e = r.entity();
			String label = e.name() + ":" + r.name();
			
			ret = DynamicRelationshipType.withName(label);
			resultsCache.put(r, ret);
		}
		return ret;
	}
}
