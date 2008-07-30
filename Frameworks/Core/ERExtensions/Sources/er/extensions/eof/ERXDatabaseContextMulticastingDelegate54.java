package er.extensions.eof;

/**
 * Hack around bug in 5.4.2.
 * 
 * @see ERXDatabaseContextMulticastingDelegate
 *
 * @author chill
 */
public class ERXDatabaseContextMulticastingDelegate54 extends ERXDatabaseContextMulticastingDelegate {

	/* 
	 * public NSDictionary databaseContextShouldUpdateCurrentSnapshot(EODatabaseContext dbCtxt, NSDictionary existingSnapshot, NSDictionary fetchedRow, EOGlobalID gid, EODatabaseChannel dbChannel)
     * This method is not implemented on purpose!
     * In WO 5.4.2 there is a bug that makes any implementation of this method prevent EOF from refreshing previously fetched EOs.
	 */ 
}
