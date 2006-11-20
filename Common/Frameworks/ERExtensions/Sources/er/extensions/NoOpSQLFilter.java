package er.extensions;

import com.webobjects.eoaccess.EOAdaptorChannel;

/**
 * The "execute everything" implementation of IERXSQLFilter.
 *  
 * @author mschrag
 */
public class NoOpSQLFilter implements IERXSQLFilter {
	public boolean shouldExecute(EOAdaptorChannel channel, String sql) {
		return true;
	}
}
