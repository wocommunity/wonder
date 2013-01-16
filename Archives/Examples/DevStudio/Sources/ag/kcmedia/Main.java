package ag.kcmedia;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class Main extends WOComponent {

    public String className = "com.webobjects.appserver.WOContext";
    public String modelPath = System.getProperty("user.home") + "/Roots/ERCoreBusinessLogic.framework/Resources/ERCoreBusinessLogic.eomodeld";
    public String ruleFileName = System.getProperty("user.home") + "/Roots/ERDirectToWeb.framework/Resources/d2w.d2wmodel";
    
    public Main(WOContext aContext) {
        super(aContext);
    }


    public WOComponent javaBrowser() {
        WOComponent nextPage = pageWithName("JavaBrowser");
        nextPage.takeValueForKey(className, "className");
        return nextPage;
    }

    public WOComponent javaDocViewer() {
        WOComponent nextPage = pageWithName("JavaDocViewer");
        nextPage.takeValueForKey(className, "className");
        return nextPage;
    }

    public WOComponent ruleEditor() {
        WOComponent nextPage = pageWithName("RuleEditor");
        nextPage.takeValueForKey(ruleFileName, "ruleFileName");
        return nextPage;
    }

    public WOComponent modeler() {
        WOComponent nextPage = pageWithName("EOModeler");
        nextPage.takeValueForKey(modelPath, "modelPath");
        return nextPage;
    }

}

