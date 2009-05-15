package er.rest;

import com.webobjects.eocontrol.EOClassDescription;

/**
 * The delegate interface used to convert objects to and from request nodes.
 * 
 * @author mschrag
 */
public interface IERXRestDelegate {
	public Object primaryKeyForObject(Object obj);

	/**
	 * Creates a new instance of the entity with the given name.
	 * 
	 * @param name
	 *            the name
	 * @return a new instance of the entity with the given name
	 */
	public Object createObjectOfEntityNamed(String name);

	/**
	 * Creates a new instance of the entity.
	 * 
	 * @param entity
	 *            the entity
	 * @return a new instance of the entity
	 */
	public Object createObjectOfEntity(EOClassDescription entity);

	/**
	 * Returns the object with the given entity name and ID.
	 * 
	 * @param name
	 *            the name of the entity
	 * @param id
	 *            the ID of the object
	 * @return the object with the given entity name and ID
	 */
	public Object objectOfEntityNamedWithID(String name, Object id);

	/**
	 * Returns the object with the given entity and ID.
	 * 
	 * @param entity
	 *            the entity
	 * @param id
	 *            the ID of the object
	 * @return the object with the given entity and ID
	 */
	public Object objectOfEntityWithID(EOClassDescription entity, Object id);
}