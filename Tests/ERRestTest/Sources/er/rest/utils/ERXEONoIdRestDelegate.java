package er.rest.utils;

import er.rest.ERXEORestDelegate;
import er.rest.ERXRestContext;

public class ERXEONoIdRestDelegate extends ERXEORestDelegate {
	@Override
	public Object primaryKeyForObject(Object obj, ERXRestContext context) {
		return null;
	}
}
