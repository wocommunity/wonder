package er.directtoweb.embed;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQuery;
import com.webobjects.directtoweb.D2WSwitchComponent;
import com.webobjects.directtoweb.NextPageDelegate;

import er.directtoweb.delegates.ERD2WQueryActionDelegate;

public class ERXD2WQuery extends D2WQuery {
	/** logging support */
	private static final Logger log = Logger.getLogger(ERXD2WQuery.class);

	/**
	 * Public constructor
	 * 
	 * @param context
	 *            the context
	 */
	public ERXD2WQuery(WOContext context) {
		super(context);
	}

	static {
		D2WSwitchComponent.addToPossibleBindings("queryBindings");
	}

	/**
	 * Overridden to support serialization
	 */
	@Override
	public NextPageDelegate newPageDelegate() {
		return ERD2WQueryActionDelegate.instance;
	}

	public void awake() {}
}