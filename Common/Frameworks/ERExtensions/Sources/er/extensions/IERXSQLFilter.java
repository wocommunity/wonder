package er.extensions;

import com.webobjects.eoaccess.EOAdaptorChannel;

/**
 * A SQL Filter can be passed into methods that execute sql scripts 
 * to allow you to tell the sql execution process to skip certain
 * undesirable statements.  For instance, during database migration,
 * you are in a transaction, so you would want to filter out any
 * transaction control statements from your adaptor's sql script
 * generation to avoid conflicts.
 *   
 * @author mschrag
 */
public interface IERXSQLFilter {
	/**
	 * Returns whether or not the given sql string should execute.
	 * 
	 * @param channel the adaptor channel that is currently executing the scripts
	 * @param sql the single line of sql to check
	 * @return true if the sql string should be executed
	 */
	public boolean shouldExecute(EOAdaptorChannel channel, String sql);
}