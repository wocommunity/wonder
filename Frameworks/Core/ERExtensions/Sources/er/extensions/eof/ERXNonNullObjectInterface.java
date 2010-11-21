package er.extensions.eof;

/**
 * This interface exists for the singular purpose of providing a way to test 
 * the nullness of an object in D2W rules without caching the EOs in the rule
 * system's cache. Rather than using a LHS qualifier like <code>object != null</code> 
 * or <code>session.user = null</code>, you can instead use 
 * <code>object.isNonNull = 1</code> or <code>not(session.user.isNonNull = 1)</code>.
 * 
 * @author Ramsey
 *
 */
public interface ERXNonNullObjectInterface {
	public Boolean isNonNull();
}
