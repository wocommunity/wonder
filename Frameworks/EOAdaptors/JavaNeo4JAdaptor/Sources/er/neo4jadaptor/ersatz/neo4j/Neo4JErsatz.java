package er.neo4jadaptor.ersatz.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;

import er.neo4jadaptor.ersatz.Ersatz;

/**
 * Ersatz stored using Neo4J {@link PropertyContainer}.
 * 
 * @author Jedrzej Sobanski
 */
public abstract class Neo4JErsatz extends Ersatz {
	protected final EOEntity entity;
	private final PropertyContainer pc;
	
	// source attribute name -> relationship
	private final Map<String, EORelationship> foreignKeys = new HashMap<String, EORelationship>();
	
	/**
	 * Create ersatz using values stored in the given property container.
	 * 
	 * @param entity entity that the property container contains record for
	 * @param pc property container to read record values from
	 * @return ersatz for a record stored in property container
	 */
	public static Neo4JErsatz create(EOEntity entity, PropertyContainer pc) {
		if (pc instanceof Node) {
			return new Neo4JNodeErsatz(entity, (Node) pc);
		} else if (pc instanceof Relationship) {
			return new Neo4JRelationshipErsatz(entity, (Relationship) pc);
		} else {
			throw new IllegalArgumentException("Property container " + pc + " is not supported/known");
		}
	}
	
	protected Neo4JErsatz(EOEntity entity, PropertyContainer pc) {
		this.entity = entity;
		this.pc = pc;
		
		for (EORelationship rel : entity.relationships()) {
			if (! rel.isCompound() && ! rel.isToMany()) {
				if (rel.sourceAttributes().size() != 1) {
					throw new IllegalArgumentException();
				} else {				
					EOAttribute att = rel.sourceAttributes().get(0);
					
					foreignKeys.put(att.name(), rel);
				}
			}
		}
	}
	
	public PropertyContainer getPropertyContainer() {
		return pc;
	}

	@Override
	public Iterable<EOAttribute> attributes() {
		return entity.attributes();
	}

	private boolean isForeignKey(EOAttribute att) {
		return foreignKeys.containsKey(att.name());
	}
	
	/**
	 * Gets ID of an object referenced by the given foreign key  
	 * 
	 * @param rel foreign key attribute
	 * @return referenced object ID
	 */
	protected abstract Number getForeignKeyValue(EORelationship rel);

	/**
	 * Sets relationship value. 
	 * 
	 * @param rel relationship to set value for
	 * @param val ID of an object referenced by the relationship 
	 */
	protected abstract void setForeignKeyValue(EORelationship rel, Number val);
	
	@Override
	public Object get(EOAttribute att) {
		if (isForeignKey(att)) {
			EORelationship rel = foreignKeys.get(att.name());
			
			return getForeignKeyValue(rel);
		} else {
			return getAttribute(pc, att);
		}
	}

	@Override
	public void put(EOAttribute att, Object value) {
		if (isForeignKey(att)) {
			EORelationship rel = foreignKeys.get(att.name());
			
			setForeignKeyValue(rel, (Number) value);
		} else {
			setAttribute(pc, att, value);
		}
	}

	/**
	 * Gets EO attribute value from a property container. 
	 * 
	 * @param container
	 * @param att
	 * @return EO attribute value
	 */
	public static Object getAttribute(PropertyContainer container, EOAttribute att) {
		Object libraryValue = container.getProperty(att.name(), null);	// if property is not set then use null as default
		
		return Neo4JTranslator.instance.toNeutralValue(libraryValue, att);
	}
	
	protected static void setAttribute(PropertyContainer pc, EOAttribute att, Object value ) {
		Object libraryValue = Neo4JTranslator.instance.fromNeutralValue(value, att);
		
		if (libraryValue != null) {
			pc.setProperty(att.name(), libraryValue);
		} else {
			pc.removeProperty(att.name());
		}
	}

	@Override
	public void remove(EOAttribute att) {
		pc.removeProperty(att.name());
	}
}
