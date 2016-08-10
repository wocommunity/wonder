import er.extensions.eof.ERXGenericRecord;


/**
 * This class in intentionally not generated.
 * @author ak
 *
 */
public class Record extends ERXGenericRecord {
	
	@Override
	public String toString() {
		return entityName() + ": " + System.identityHashCode(this);
	}
	
	@Override
	public String userPresentableDescription() {
		return toString();
	}
	
	@Override
	public Object handleQueryWithUnboundKey(String key) {
		 log.info("unbound: " + key);
		 return null;
	}
}
