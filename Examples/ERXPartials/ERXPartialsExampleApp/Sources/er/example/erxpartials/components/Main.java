package er.example.erxpartials.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;

public class Main extends WOComponent {
	private static final long serialVersionUID = 1L;

	public String username;
    public String password;
	private String errorMessage;

    public Main(WOContext aContext) {
        super(aContext);
    }

	/**
	 * @return the errorMessage
	 */
	public String errorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public WOActionResults loginAction()
	{
		// ENHANCEME - add appropriate login behaviour here

		return D2W.factory().defaultPage(session());
	}

}
