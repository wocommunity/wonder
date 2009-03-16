package er.sproutcore.example.components.sample;

import com.webobjects.appserver.WOContext;

import er.sproutcore.views.SCComponent;

public class Welcome extends SCComponent {
    public Welcome(WOContext context) {
        super(context);
        setClassName("SC.View");
    }
}