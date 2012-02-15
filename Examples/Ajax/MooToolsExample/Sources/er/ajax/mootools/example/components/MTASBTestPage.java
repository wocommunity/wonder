package er.ajax.mootools.example.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class MTASBTestPage extends Main {
	
	public String _wifeName = "Honey";
	public String _kidName = "Gisele";
	public String _dogName = "Astro Nut";
	public String _catName = "Brazil Nut";
	
    public MTASBTestPage(WOContext context) {
        super(context);
    }
    

	public WOComponent updateNames() {
		return null;
	}

	public WOComponent updateNamesSlowly() {

		setTask(new Task());
		task().start();
		do {
			System.out.println(task().getStatus());
		} while(! task().getStatus().equals("Finished"));

		return null;
	}
	
}