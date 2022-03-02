package er.directtoweb.embed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQuery;
import com.webobjects.directtoweb.D2WSwitchComponent;
import com.webobjects.directtoweb.NextPageDelegate;

import er.directtoweb.delegates.ERD2WQueryActionDelegate;

public class ERXD2WQuery extends D2WQuery {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** logging support */
	private static final Logger log = LoggerFactory.getLogger(ERXD2WQuery.class);

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

	@Override
	public void awake() {}
}