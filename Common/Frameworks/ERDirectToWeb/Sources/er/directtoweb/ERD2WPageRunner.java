package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.directtoweb.ConfirmPageInterface;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.directtoweb.SelectPageInterface;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXEC;
import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXSession;
import er.extensions.ERXWOContext;

/**
 * Runs through an array of given page configurations and renders them. 
 * Basically, you run through your app manually to collect all the page configurations. Doing to
 * will collect all page configs in ERD2WPage.allConfigurationNames().
 * Then you save the array somewhere and later run through all pages with this class.
 * This is pretty useful in conjunction when localizing, as you get create all the keys that get touched.
 * Also, you can do simple tests with a scheme similar to this one. It's not totally correct,
 * as your pages need not map one-to-one to your pages, but for me it works pretty well.
 * 
 * @author ak
 *
 */
public class ERD2WPageRunner {
	
	private NSArray _pages;
	
	private static final Logger log = Logger.getLogger(ERD2WPageRunner.class);
	
	public ERD2WPageRunner(NSArray pages) {
		_pages = pages;
	}

	public void createPages() {
		ERXSession session = ERXSession.session();
		// session = (ERXSession)context.session();
		for (Enumeration pages = _pages.objectEnumerator(); pages.hasMoreElements();) {
			String pageName = (String) pages.nextElement();
			WOContext context = ERXWOContext.newContext();
			session._awakeInContext(context);
			try {
				EOEditingContext ec = ERXEC.newEditingContext();
				ec.lock();
				try {
					log.info("Creating page: " + pageName);
					WOComponent page = D2W.factory().pageForConfigurationNamed(pageName, session);
					context._setPageElement(page);
					context._setCurrentComponent(page);
					String task = ERD2WFactory.taskFromPage(page);
					String entityName = ERD2WFactory.entityNameFromPage(page);
					if (page instanceof InspectPageInterface) {
						InspectPageInterface ipi = (InspectPageInterface) page;
						ipi.setObject(EOUtilities.createAndInsertInstance(ec, entityName));
						ipi.setNextPage(page);
					} else if (page instanceof ListPageInterface) {
						ListPageInterface lpi = (ListPageInterface) page;
						lpi.setDataSource(ERXEOControlUtilities.dataSourceForArray(ec, entityName, new NSArray(EOUtilities.createAndInsertInstance(ec, entityName))));
						lpi.setNextPage(page);
					} else if (page instanceof SelectPageInterface) {
						SelectPageInterface lpi = (SelectPageInterface) page;
						lpi.setDataSource(ERXEOControlUtilities.dataSourceForArray(ec, entityName, new NSArray(EOUtilities.createAndInsertInstance(ec, entityName))));
					} else if (page instanceof ConfirmPageInterface) {
						ConfirmPageInterface cpi = (ConfirmPageInterface) page;
						// nothing
					} else if (page instanceof QueryPageInterface) {
						QueryPageInterface qpi = (QueryPageInterface) page;
						// nothing
					} else {
						log.info("Unsupported: " + pageName + " -> " + page.name());
					}
					page.appendToResponse(new WOResponse(), context);
				} finally {
					ec.unlock();
				}
			} catch(Throwable t) {
				log.error("Error running: " + pageName + ":" +  t.getMessage() + " Tree: " + ERXWOContext.componentPath(context));
			} finally {
				session._sleepInContext(context);
			}

		}
	}
}
