package er.sproutcore.example.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;
import er.sproutcore.views.SCView;

public class Main extends ERXComponent {

    public Main(WOContext context) {
		super(context);
	}

    public String renderTree() {
        return SCView.pageItem().toString();
    }
}
