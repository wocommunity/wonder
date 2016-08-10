package er.example.erxpartials;

import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;

public class MainNavigationController
{

	private Session _session;


	public MainNavigationController(Session s)
	{
		super();
		_session = s;
	}

	public WOComponent homeAction()
	{
		return D2W.factory().defaultPage(session());
	}

	// GENERIC ACTIONS

	public WOComponent queryPageForEntityName(String entityName)
	{
		QueryPageInterface newQueryPage = D2W.factory().queryPageForEntityNamed(entityName, session());
		return (WOComponent) newQueryPage;
	}

	public WOComponent newObjectForEntityName(String entityName)
	{
		WOComponent nextPage = null;
		try
		{
			EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(entityName, session());
			epi.setNextPage(session().context().page());
			nextPage = (WOComponent) epi;
		} catch (IllegalArgumentException e)
		{
			ErrorPageInterface epf = D2W.factory().errorPage(session());
			epf.setMessage(e.toString());
			epf.setNextPage(session().context().page());
			nextPage = (WOComponent) epf;
		}
		return nextPage;
	}
	
	// ACCESSORS

	public Session session()
	{
		return _session;
	}

	public void setSession(Session s)
	{
		_session = s;
	}
}
