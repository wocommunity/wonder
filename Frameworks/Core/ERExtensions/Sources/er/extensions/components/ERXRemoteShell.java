package er.extensions.components;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPathUtilities;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXRuntimeUtilities;
import er.extensions.foundation.ERXRuntimeUtilities.Result;
import er.extensions.foundation.ERXRuntimeUtilities.TimeoutException;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXSystem;

/**
 *
 * @property java.io.tmpdir used for the default upload directory
 */
public class ERXRemoteShell extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static String alternativeQuote = "!#ALT_QUOTE#!";

    public String dir = "/";

    public String selectedPath;

    public NSMutableArray pathHistory = new NSMutableArray();

    public String currentPath;

    public int timeout = 3 * 60 * 1000; // 3 minutes

    public String envp;

    public String uploadPath = ERXSystem.getProperty("java.io.tmpdir");

    public String consoleText = null;

    public String consoleTextHistory = null;

    public String command = null;

    // file upload stuff
    public String filePath;

    public String finalFilePath;

    public String mimeType;

    public String streamToFilePath() {
        return new File(uploadPath, NSPathUtilities.lastPathComponent(filePath)).getAbsolutePath();
    }

    public ERXRemoteShell(WOContext context) {
        super(context);
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        if (session().objectForKey("ERXRemoteShell.enabled") != null) {
            dir = "";
            super.appendToResponse(r, c);
        } else {
            r.appendContentString("please use the ERXDirectAction remoteShellAction to login first!");
        }
    }

    public WOComponent execute() {
        if (ERXStringUtilities.stringIsNullOrEmpty(dir)) {
            dir = selectedPath;
        } else if (!pathHistory.containsObject(dir)) {
            pathHistory.addObject(dir);
        }
        // get the new command
        String[] commandArray = buildCommandArray(command);
        String[] envpArray = buildEnvpArray(envp);
        if (envpArray == null || envpArray.length == 0) {
            envpArray = new String[] { "TEST=t" };
        }
        if (ERXStringUtilities.stringIsNullOrEmpty(dir)) {
            dir = "/";
        }
        try {
            Result result = ERXRuntimeUtilities.execute(commandArray, envpArray, new File(dir), timeout);
            String response = result.getResponseAsString();
            consoleText = "\n" + response;
            consoleTextHistory += "\n";
            consoleTextHistory += "\n";
			consoleTextHistory += "<b>"+command+"</b>";
            consoleTextHistory += "\n";
            consoleTextHistory += consoleText;
            return context().page();
        } catch (IOException e) {
            consoleText += "\n";
            consoleText += "an exception occured";
            consoleText += "\n";
            consoleText += e.getMessage();
            return context().page();
        } catch (TimeoutException e) {
            consoleText += "\n";
            consoleText += "process did not timeout after " + timeout + " seconds";
            return context().page();
        }
    }

    public WOComponent clearConsole() {
        consoleText = "";
        consoleTextHistory = "";
        return context().page();
    }

    public WOComponent uploadFile() {
        return context().page();
    }

    private static String[] buildEnvpArray(String envp) {
        NSArray a = NSArray.componentsSeparatedByString(envp, Character.LINE_SEPARATOR + "");
        return ERXArrayUtilities.toStringArray(a);
    }

    private static String[] buildCommandArray(String command) {
        String newCommand = StringUtils.replace(command, "\\\"", alternativeQuote);
        NSMutableArray a = new NSMutableArray();
        StringBuilder buf = new StringBuilder();
        int length = newCommand.length();
        boolean insideQuote = false;
        for (int i = 0; i < length; i++) {
            char c = newCommand.charAt(i);
            if ('"' == c) {
                if (insideQuote) {
                    // quoted string ends here
                    String s = buf.toString().replaceAll(alternativeQuote, "\"");
                    if (s.length() > 0) {
                        a.addObject(s);
                    }
                    buf = new StringBuilder();
                } else {
                    // quoted string starts here
                    insideQuote = true;
                    buf = new StringBuilder();
                }
            } else if (' ' == c) {
                if (insideQuote) {
                    // space inside a quoted string is OK
                    buf.append(c);
                } else {
                    // string sequence ends here
                    String s = buf.toString().replaceAll(alternativeQuote, "\"");
                    if (s.length() > 0) {
                        a.addObject(s);
                    }
                    buf = new StringBuilder();
                }
            } else {
                buf.append(c);
            }
        }
        if (buf.length() > 0)
            a.addObject(buf.toString().replaceAll(alternativeQuote, "\""));
        return ERXArrayUtilities.toStringArray(a);
    }
}
