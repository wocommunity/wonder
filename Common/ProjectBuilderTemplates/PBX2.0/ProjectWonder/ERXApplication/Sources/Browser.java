//
// Browser
// Project ÇPROJECTNAMEÈ
//
// Created by ÇUSERNAMEÈ on ÇDATEÈ
//

import com.webobjects.foundation.*;
import er.extensions.*;

public class Browser extends ERXBasicBrowser {

    public Browser(String browserName, String version, String mozillaVersion,
                                                    String platform, NSDictionary userInfo) {
        super(browserName, version, mozillaVersion, platform, userInfo);
    }

    public boolean isSupportedBrowser() {
        return true;
    }

    public boolean doesHandleCSSLineHeightCorrectly() {
        return ! doesHandleCSSLineHeightIncorrectly();
    }

    public boolean doesHandleCSSLineHeightIncorrectly() {
        return (isNetscape() && isMozilla40Compatible())  ||  (isOmniWeb() && isVersion4());
    }

}
