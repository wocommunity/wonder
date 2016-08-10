
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class AjaxTabbedPanelInModalDialogExample extends WOComponent {
	
    public String formValueA;
    public String formValueB;
    public String formValueC;
	
    public AjaxTabbedPanelInModalDialogExample(WOContext context) {
        super(context);
    }
    
    public WOComponent save() {
    	System.out.println("formValueA " + formValueA);
    	System.out.println("formValueB " + formValueB);
    	System.out.println("formValueC " + formValueC);
    	return context().page();
    }
    
    
}