package er.excel;

import com.webobjects.appserver.*;

public class Main extends WOComponent {
    public Main(WOContext aContext) {
        super(aContext);
    }

    public WOComponent showSampleAction() {
    	SampleTable nextPage = (SampleTable)pageWithName("SampleTable");
    	return nextPage;
    }
    public WOComponent showExcelAction() {
    	SampleTable nextPage = (SampleTable)pageWithName("SampleTable");
    	nextPage.enabled = true;
    	return nextPage;
    }
}

