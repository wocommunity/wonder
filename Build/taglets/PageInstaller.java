
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

public class PageInstaller {

    static void addLinkToNavBar(String filename) {

        File file = new File(filename);

        FileReader fRdr = null;

        try {
            fRdr = new FileReader(file);
        } catch (java.io.FileNotFoundException fnfe) { System.err.println("ERROR: could not open file for reading: "+file); System.exit(1); }

        LineNumberReader rdr = new LineNumberReader(fRdr);

        String line = "";

        ArrayList<String> output = new ArrayList<String>();

        boolean needsUpdate = false;

        while (line != null) {
            try {
                line = rdr.readLine();
            } catch (java.io.IOException ioe) { line = null; }

            if (line != null) {
                output.add(line);
                if (line.indexOf("CLASS=\"NavBarCell1\"") > 0 && line.indexOf("deprecated-list.html") > 0) {
                    needsUpdate = true;
                    String str = "  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"";
                    str += dotsForPath(filename);
                    str += "components.html";
                    str += "\"<FONT CLASS=\"NavBarFont1\"><B>Components</B></FONT></A>&nbsp;</TD>";
                    output.add(str);
                }
            }
        }

        FileWriter targetFile = null;

        if (needsUpdate) {

            try {
                targetFile = new FileWriter(file);

                for (int idx = 0; idx < output.size(); idx++) {
                      //System.out.println("["+idx+"] "+output.get(idx));
                    targetFile.write(output.get(idx)+"\n");
                }
                targetFile.close();

            } catch (java.io.IOException ioe) { System.err.println("ERROR: could not open file for writing: "+file); System.exit(1); }
        }
    }

    static String dotsForPath(String path) {
        int count = 0;
        final String base = "dist/wonder-5.0/Documentation/api";

        String str = path;

        if (str.indexOf(base) >= 0) str = str.substring(str.indexOf(base)+base.length()+1);

        while (str.indexOf("/") > 0) {
            str = str.substring(str.indexOf("/")+1);
            count++;
        }
        
        StringBuffer dots = new StringBuffer();
        for (int idx = 0; idx < count; idx++) dots.append("../");

        return dots.toString();
    }

    static void addLineToFiles(String path) {

        HashSet<File> paths = new HashSet<File>();
        paths.add(new File(path));

        HashSet<File> addable = new HashSet<File>();

        int count = 0;

        while (paths.size() != count) {

            count = paths.size();

            Iterator<File> files = paths.iterator();
            while (files.hasNext()) {
                File aFile = files.next();
                if (aFile.isDirectory()) {
                    File[] subs = aFile.listFiles();
                    for (int idx = 0; idx < subs.length; idx++) {
                        addable.add(subs[idx]);
                    }
                }
            }

            files = addable.iterator();
            while (files.hasNext()) {
                paths.add(files.next());
            }
        }

        Iterator<File> files = paths.iterator();
        while (files.hasNext()) {
            String name = files.next().toString();
            if (name.endsWith(".html"))
                addLinkToNavBar(name);
        }
    }

    public static void main(String[] arg) {

        /*
        Properties p = System.getProperties();
        Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement().toString();
            System.out.println("property[\""+name+"\"] -> \""+p.getProperty(name)+"\"");
        }
        */
       if (!"wonder-5.0".equals(System.getProperty("wonder.version"))) System.exit(0);

       String root = System.getProperty("wonder.root")+"/dist/wonder-5.0/Documentation/api/";

       (new File("components.html")).renameTo(new File(root+"components.html"));
       (new File("components.txt")).renameTo(new File(root+"components.txt"));

       addLinkToNavBar(root+"constant-values.html");
       addLinkToNavBar(root+"deprecated-list.html");
       addLinkToNavBar(root+"help-doc.html");
       addLinkToNavBar(root+"index-all.html");
       addLinkToNavBar(root+"overview-summary.html");
       addLinkToNavBar(root+"overview-tree.html");
       addLinkToNavBar(root+"serialized-form.html");

       addLineToFiles(root+"com");
       addLineToFiles(root+"er");
    }
}
