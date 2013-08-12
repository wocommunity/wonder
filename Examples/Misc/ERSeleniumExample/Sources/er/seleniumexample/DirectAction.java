package er.seleniumexample;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }

    @Override
    public WOActionResults defaultAction() {
        return pageWithName(Main.class.getName());
    }
}
