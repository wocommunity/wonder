package er.pdfexamples.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class SimplePDFGeneration1 extends ERXComponent {
	public Boolean usePrintCss  = true;
	
    public Boolean getUsePrintCss() {
		return usePrintCss;
	}

	public void setUsePrintCss(Boolean usePrintCss) {
		this.usePrintCss = usePrintCss;
	}

	public SimplePDFGeneration1(WOContext context) {
        super(context);
    }
    
}