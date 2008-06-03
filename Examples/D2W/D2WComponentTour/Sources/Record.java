

public class Record extends _Record {
	
	public String toString() {
		return entityName() + ": " + System.identityHashCode(this);
	}
	
	public String userPresentableDescription() {
		return toString();
	}
}
