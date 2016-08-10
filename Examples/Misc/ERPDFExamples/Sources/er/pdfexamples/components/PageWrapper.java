package er.pdfexamples.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class PageWrapper extends ERXComponent {
	public Boolean usePrintCss ;
	private String _pageTitle = "ERPDFGeneration Example Application";
    public PageWrapper(WOContext context) {
        super(context);
    }
	public String getPageTitle() {
		return _pageTitle;
	}
	public void setPageTitle(String _pageTitle) {
		this._pageTitle = _pageTitle;
	}
}