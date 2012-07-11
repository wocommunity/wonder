package er.neo4jadaptor.storage.neo4j;

/**
 * Thrown when a store is not capable for handling some EO entity.
 * 
 * @author Jedrzej Sobanski
 */
public class UnsupportedEntityException extends RuntimeException {
	private static final long serialVersionUID = 5749904167463089560L;

	public UnsupportedEntityException(String msg) {
		super(msg);
	}
	
}