package com.webobjects.monitor.application;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class ConfirmationPage extends MonitorComponent {
    
    public interface Delegate {
        public int pageType();
        public String question();
        public String explaination();
        public WOComponent confirm();
        public WOComponent cancel();
    }
    
    private Delegate _delegate;
    
    public ConfirmationPage(WOContext context) {
        super(context);
    }
    
    public Delegate delegate() {
        return _delegate;
    }
    
    public void setDelegate(Delegate value) {
        _delegate = value;
    }


    public static ConfirmationPage create(WOContext context, Delegate delegate) {
        assert delegate != null;
        ConfirmationPage page =  (ConfirmationPage)  context.page().pageWithName(ConfirmationPage.class.getName());
        page.setDelegate(delegate);
        return page;
    }

}