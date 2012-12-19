
package org.ganymede;

import org.ganymede.ui.AddTestResultPage;
import org.ganymede.ui.Main;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {

    public DirectAction(WORequest request) {
        super(request);
    }

    @Override
    public WOActionResults defaultAction() {
        return pageWithName(Main.class);
    }

    public WOActionResults addResultAction() {
        return pageWithName(AddTestResultPage.class);
    }
}
