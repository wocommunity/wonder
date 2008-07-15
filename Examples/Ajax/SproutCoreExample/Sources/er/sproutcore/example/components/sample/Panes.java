package er.sproutcore.example.components.sample;

import com.webobjects.appserver.WOContext;

import er.sproutcore.views.SCComponent;

public class Panes extends SCComponent {
    public Panes(WOContext context) {
        super(context);
        setClassName("SC.View");
    }
}