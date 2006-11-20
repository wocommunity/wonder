package er.extensions;

import com.webobjects.eoaccess.EOAdaptorChannel;

/**
 * FrontBase generates "SET TRANSACTION" statements at the beginning
 * of its generated SQL.  This filter removes those statements.
 * 
 * @author mschrag
 */
public class FrontBaseSQLFilter implements IERXSQLFilter {
	public boolean shouldExecute(EOAdaptorChannel channel, String sql) {
		return !sql.startsWith("SET TRANSACTION ISOLATION LEVEL");
	}

}
