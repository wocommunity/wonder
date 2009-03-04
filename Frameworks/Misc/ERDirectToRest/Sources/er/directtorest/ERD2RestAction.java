package er.directtorest;

import com.webobjects.appserver.WORequest;

import er.directtoweb.ERD2WDirectAction;

public class ERD2RestAction extends ERD2WDirectAction {

    public ERD2RestAction(WORequest r) {
        super(r);
    }

    @Override
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return true;
    }
}
