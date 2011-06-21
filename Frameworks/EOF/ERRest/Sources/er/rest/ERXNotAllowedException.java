package er.rest;

/**
 * Use this exception when you want to return a HTTP code 405 (Not Allowed)
 * @author probert
 *
 */
public class ERXNotAllowedException extends Exception {

	public ERXNotAllowedException() {
		super();
	}

	public ERXNotAllowedException(String s) {
		super(s);
	}

	public ERXNotAllowedException(Throwable throwable) {
		super(throwable);
	}

	public ERXNotAllowedException(String s, Throwable throwable) {
		super(s, throwable);
	}

}
