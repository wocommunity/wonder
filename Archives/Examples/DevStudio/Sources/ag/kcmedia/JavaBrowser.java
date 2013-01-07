//
// Sources/ag/kcmedia/JavaBrowser.java: Class file for WO Component 'JavaBrowser'
// Project DevStudio
//
// Created by ak on Wed Jul 24 2002
//
package ag.kcmedia;

import org.apache.log4j.Logger;

import ag.kcmedia.Jode.ClassProxy;
import ag.kcmedia.Jode.PackageProxy;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
public class JavaBrowser extends WOComponent {
    static final Logger log = Logger.getLogger(JavaBrowser.class);

    public PackageProxy currentPackage;
    public ClassProxy currentClass;
    
    public String currentMethod;
    public String currentVariable;

    public NSArray selectedPackages = new NSArray();
    public NSArray selectedClasses = new NSArray();
    public NSMutableArray visitedClasses = new NSMutableArray();
    public ClassProxy selectedClass;

    public boolean showCode = true;

    public JavaBrowser(WOContext context) {
        super(context);
    }

    public WOComponent toggleCode() {
        showCode = !showCode;
        return null;
    }
    
    public WOComponent largeView() {
        String content = null;
        if(selectedClass() != null) {
            if(showCode)
                content = selectedClass().sourceCode();
            else
                content = selectedClass().documentation();
        }
        WOComponent nextPage = pageWithName("StringHolder");
        nextPage.takeValueForKey(content, "string");
        nextPage.takeValueForKey(Boolean.valueOf(!showCode), "isDocumentation");
        
        return nextPage;
    }

    public WOComponent submit() {
        ClassProxy selectedClass = null;
        if(selectedClasses.count() > 0)
            selectedClass = (ClassProxy)selectedClasses.objectAtIndex(0);
        if(selectedClass != null) {
            if(visitedClasses.containsObject(selectedClass))
                visitedClasses.removeObject(selectedClass);
            visitedClasses.insertObjectAtIndex(selectedClass, 0);
            this.selectedClass = selectedClass;
        }
        log.info("submit() - selectedClass: " + selectedClass);
        return null;
    }
    public WOComponent selectHistory() {
        ClassProxy selectedClass = this.selectedClass;
        if(selectedClass != null) {
            if(false) {
                if(visitedClasses.containsObject(selectedClass))
                    visitedClasses.removeObject(selectedClass);
                visitedClasses.insertObjectAtIndex(selectedClass, 0);
            }
            selectedClasses = new NSArray(selectedClass);
            selectedPackages = new NSArray(selectedClass.packageProxy());
        }
        log.info("selectHistory() - selectedClass: " + selectedClass + " - " + currentClass);
        return null;
    }
    public WOComponent selectFound() {
        ClassProxy selectedClass = this.selectedClass;
        if(selectedClass != null) {
            if(false) {
                if(visitedClasses.containsObject(selectedClass))
                    visitedClasses.removeObject(selectedClass);
                visitedClasses.insertObjectAtIndex(selectedClass, 0);
            }
            selectedClasses = new NSArray(selectedClass);
            selectedPackages = new NSArray(selectedClass.packageProxy());
        }
        log.info("selectHistory() - selectedClass: " + selectedClass + " - " + currentClass);
        return null;
    }
    
    public String actionUrlForClass(String action) {
        if(selectedClass() == null)
            return null;
        return context().directActionURLForActionNamed(action + "." + selectedClass().fullName(), null);
    }

    public String docsUrl() { return actionUrlForClass("docs");}
    public String codeUrl() { return actionUrlForClass("code");}
    public String methodsUrl() { return actionUrlForClass("methods");}
    public String largeViewUrl() { return (showCode? codeUrl(): docsUrl());}
    
    public NSDictionary packages() {
        return Jode.allPackages();
    }

    public PackageProxy selectedPackage() {
        if(selectedPackages.count() > 0) {
            return (PackageProxy)selectedPackages.objectAtIndex(0);
        }
        return null;
    }

    public void setSelectedPackages(NSArray value) {
        selectedPackages = value;
        selectedClasses = null;
        selectedClass = null;
        log.info("setSelectedPackages()" + value);
    }

    public void setClassName(String value) {
        ClassProxy selectedClass = Jode.classProxyForName(value);
        selectedClasses = new NSArray(selectedClass);
        selectedPackages = new NSArray(selectedClass.packageProxy());
        this.selectedClass = selectedClass;
    }

    public void setSelectedClasses(NSArray value) {
        selectedClasses = value;
    }

    public ClassProxy selectedClass() {
        if(selectedClass == null && selectedClasses.count() > 0) {
            selectedClass = (ClassProxy)selectedClasses.objectAtIndex(0);
        }
        return selectedClass;
    }
    public NSArray foundClasses = null;
    public String stringToFind = "";
    public String lastStringToFind = "";

    public void setStringToFind(String value) {
        stringToFind = value;
    }
    public NSArray foundClasses() {
        log.info("start");
        if(stringToFind != null && stringToFind.length() >= 5 && !stringToFind.equals(lastStringToFind)) {
            EOQualifier qualifier = EOQualifier.qualifierWithQualifierFormat("names.toString caseInsensitiveLike %s", new NSArray("*"+stringToFind+"*"));
            foundClasses = EOQualifier.filteredArrayWithQualifier(Jode.allClasses().allValues(), qualifier);
            lastStringToFind = stringToFind;
        }
        log.info("end");
        return foundClasses;
    }
}
