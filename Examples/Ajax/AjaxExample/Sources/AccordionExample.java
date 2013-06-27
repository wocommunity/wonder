import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class AccordionExample extends WOComponent {
	public String _name1;
	public String _name2;

	public AccordionExample(WOContext context) {
		super(context);
	}

	public WOActionResults save() {
		System.out.println("AccordionExample.save: name1 = " + _name1);
		System.out.println("AccordionExample.save: name2 = " + _name2);
		return null;
	}
}
