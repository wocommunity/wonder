//Check path to Config File.
//Check the Expire Date.

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.xml.WOXMLCoder;
import com.webobjects.foundation.NSPathUtilities;

public class Application extends WOApplication {

    private Configuration config;
    private String configPath;

    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }

    public Application() {
        super();
        configPath = NSPathUtilities.stringByAppendingPathComponent(this.path(), "Configuration.xml");
        try {
            config = Configuration.configurationWithPath(configPath);
        } catch(Exception e){
            System.out.println(e);
            config = new Configuration("");
            this.saveConfiguration();
        }
    }

    public boolean saveConfiguration(){
        String codedString = WOXMLCoder.coder().encodeRootObjectForKey(config, "Configuration");
        try{
            File configurationFile = new File(configPath);
            FileOutputStream fos = new FileOutputStream(configurationFile);
            fos.write(codedString.getBytes());
            fos.close();
            return true;
        }catch(IOException e){
            System.out.println(e);
            return false;
        }
    }

    public Configuration config(){
        return config;
    }
    
    public String fontList() {
        return config.fontList();
    }
    
}
