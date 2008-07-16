package er.sproutcore.example.components.sample;

import com.webobjects.appserver.WOContext;

import er.sproutcore.views.SCComponent;

public class Collections extends SCComponent {
    public Collections(WOContext context) {
        super(context);
        setClassName("SC.View");
    }
    
    @Override
    protected String style() {
    	return "width: 100%; height: 100%; position: absolute;";
    }
}