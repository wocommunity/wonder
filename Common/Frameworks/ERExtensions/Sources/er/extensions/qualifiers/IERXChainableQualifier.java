package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOQualifier;

/**
 * IERXQualifier is the definition of methods required for chainable
 * EOAndQualifier.
 * 
 * @author mschrag
 */
public interface IERXChainableQualifier {
	/**
	 * Returns a new qualifier that represents this qualifier and'd to the given
	 * list of qualifiers.
	 * 
	 * @param qualifiers
	 *            the qualifiers to and with this qualifier
	 * @return an ERXAndQualifier
	 */
	public ERXAndQualifier and(EOQualifier... qualifiers);

	/**
	 * Returns a new qualifier that represents this qualifier or'd with the
	 * given list of qualifiers.
	 * 
	 * @param qualifiers
	 *            the qualifiers to or with this qualifier
	 * @return an ERXOrQualifier
	 */
	public ERXOrQualifier or(EOQualifier... qualifiers);

	/**
	 * Returns a new qualifier that represents this qualifier not'd.
	 * 
	 * @return an ERXNotQualifier
	 */
	public ERXNotQualifier not();
}
