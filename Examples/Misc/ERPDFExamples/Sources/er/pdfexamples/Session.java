package er.pdfexamples;

import er.extensions.appserver.ERXSession;
import er.pdfexamples.components.Main;

public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;
	
	private Main  mainpage;

	public Session() {
	}

	public Main getMainpage() {
		return mainpage;
	}

	public void setMainpage(Main mainpage) {
		this.mainpage = mainpage;
	}
}
