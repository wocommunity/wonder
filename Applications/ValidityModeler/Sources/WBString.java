import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WBString extends WOComponent {

    public WBString(WOContext arg0) {
		super(arg0);
	}

	@Override
	public boolean synchronizesVariablesWithBindings(){
        return false;
    }

}