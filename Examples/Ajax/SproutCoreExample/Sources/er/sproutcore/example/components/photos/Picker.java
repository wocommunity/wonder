package er.sproutcore.example.components.photos;

import com.webobjects.appserver.WOContext;

import er.sproutcore.views.SCComponent;

public class Picker extends SCComponent {
    public Picker(WOContext context) {
        super(context);
        setClassName("SC.View");
    }
}