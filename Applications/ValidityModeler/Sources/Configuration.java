import com.gammastream.validity.GSVRule;
import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.appserver.xml.WOXMLCoding;
import com.webobjects.appserver.xml.WOXMLDecoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public final class Configuration extends Object implements WOXMLCoding {

    private NSMutableArray paths = new NSMutableArray();
    private NSMutableArray quickRules = new NSMutableArray();
    private String password;
    private String fontList;
    
    private NSArray availableFontLists = new NSArray(new Object[] {
        "Arial,Helvetica", "Osaka,MS P Gothic" }); 

    public static Configuration configurationWithPath(java.lang.String path){
        return (Configuration)WOXMLDecoder.decoder().decodeRootObject(path);
    }
    
    public Configuration(String s){
        this.setPassword(s);
    }
    
    public void addPath(String path){
        if(!paths.containsObject(path))
            paths.addObject(path);     
    }
    
    public void removePath(String path){
        if(paths.containsObject(path))
            paths.removeObject(path);
    }

    public void removeRule(GSVRule rule){
        if(quickRules.containsObject(rule))
            quickRules.removeObject(rule);
    }
    
    public void addRule(GSVRule rule){
        if(!quickRules.containsObject(rule))
            quickRules.addObject(rule);
    }

    public void setQuickRules(NSMutableArray rules){
            quickRules=rules;
    }

    public NSMutableArray paths(){
        return paths;
    }
    
    public NSMutableArray quickRules(){
        return quickRules;
    }
    
    public String password(){
        return password;
    }
    
    public void setPassword(String s){
        password = s;
    }
    
    public String fontList(){
        return fontList;
    }
    
    public void setFontList(String s){
        fontList = s;
    }
    
    public NSArray availableFontLists(){
        return availableFontLists;
    }
    
    public boolean hasRules() {
        return (quickRules().count()>0);
    }

    public boolean hasRecent() {
        return (paths().count()>0);
    }

    // xml interfaced methods
    public void encodeWithWOXMLCoder(WOXMLCoder coder) {
        coder.encodeObjectForKey((NSArray)paths, "Paths");
        coder.encodeObjectForKey(password, "Password");
        coder.encodeObjectForKey((NSArray)quickRules, "QuickRules");
        coder.encodeObjectForKey(fontList, "FontList");
    }

    public Configuration(WOXMLDecoder decoder) {
        paths = new NSMutableArray((NSArray)decoder.decodeObjectForKey("Paths"));
        password = (String)decoder.decodeObjectForKey("Password");
        quickRules = new NSMutableArray((NSArray)decoder.decodeObjectForKey("QuickRules"));
        fontList = (String)decoder.decodeObjectForKey("FontList");
        if (fontList == null  ||  fontList.length() == 0)
            fontList = (String)availableFontLists.objectAtIndex(0);
    }

    public Class classForCoder() {
        try{
        return Class.forName("Configuration");
        }catch(ClassNotFoundException e){
        System.out.println(e);
        return null;
        }
    }
}
