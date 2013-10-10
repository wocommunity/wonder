package er.extensions.appserver;

/**
 * An extension of {@link IERXPerformWOAction} that allows a result object to be
 * provided such that the {@link IERXPerformWOAction#performAction()} can use that result in its
 * logic.
 *
 * @author kieran
 */
public interface IERXPerformWOActionForResult extends IERXPerformWOAction {
	/**
	 * Provide a result object to the controller
	 *
	 * @param result
	 */
	public void setResult(Object result);
}
