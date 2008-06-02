import er.extensions.eof.ERXGenericRecord;


public class Record extends ERXGenericRecord {
	
	public String toString() {
		return entityName() + ": " + System.identityHashCode(this);
	}
	
	public String userPresentableDescription() {
		return toString();
	}
}
