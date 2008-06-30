package er.sproutcore;

import com.webobjects.appserver.WOApplication;

import er.extensions.ERXFrameworkPrincipal;

public class SproutCore extends ERXFrameworkPrincipal {

    public static final String SC_KEY = "_sc_";

    @Override
    public void finishInitialization() {
        WOApplication.application().registerRequestHandler(new SCRequestHandler(), SC_KEY);
    }

}
