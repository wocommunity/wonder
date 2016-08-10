
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
    static void addLinkToNavBar(String targetFilename, String htmlFilename, String linkName) {
        File file = new File(targetFilename);
        FileReader fRdr = null;

        try {
            fRdr = new FileReader(file);
        } catch (java.io.FileNotFoundException fnfe) {
            System.err.println("ERROR: could not open file for reading: " + file);
            System.exit(1);
        }

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
                    StringBuilder sb = new StringBuilder();
                    sb.append("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"");
                    sb.append(dotsForPath(targetFilename));
                    sb.append(htmlFilename);
                    sb.append("\"<FONT CLASS=\"NavBarFont1\"><B>");
                    sb.append(linkName);
                    sb.append("</B></FONT></A>&nbsp;</TD>");
                    output.add(sb.toString());
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
        final String base = "dist/wonder-6.0/Documentation/api";

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

    static void addLineToFiles(String targetPath, String htmlFilename, String linkName) {

        HashSet<File> paths = new HashSet<File>();
        paths.add(new File(targetPath));

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
                addLinkToNavBar(name, htmlFilename, linkName);
        }
    }

    public static void main(String[] arg) {

        /*
        // when debugging the properties settings....
        //
        Properties p = System.getProperties();
        Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            String name = e.nextElement().toString();
            System.out.println("property[\""+name+"\"] -> \""+p.getProperty(name)+"\"");
        }
        */
       if (!"wonder-6.0".equals(System.getProperty("wonder.version"))) System.exit(0);

       String root = System.getProperty("wonder.root")+"/dist/wonder-6.0/Documentation/api/";

       (new File("components.html")).renameTo(new File(root+"components.html"));
       (new File("components.txt")).renameTo(new File(root+"components.txt"));

       (new File("d2wlist.html")).renameTo(new File(root+"d2wlist.html"));
       (new File("d2wlist.txt")).renameTo(new File(root+"d2wlist.txt"));

       addLinkToNavBar(root+"constant-values.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"constant-values.html", "components.html", "Components");

       addLinkToNavBar(root+"deprecated-list.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"deprecated-list.html", "components.html", "Components");

       addLinkToNavBar(root+"help-doc.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"help-doc.html", "components.html", "Components");

       addLinkToNavBar(root+"index-all.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"index-all.html", "components.html", "Components");

       addLinkToNavBar(root+"overview-summary.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"overview-summary.html", "components.html", "Components");

       addLinkToNavBar(root+"overview-tree.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"overview-tree.html", "components.html", "Components");

       addLinkToNavBar(root+"serialized-form.html", "d2wlist.html", "D2WKeys");
       addLinkToNavBar(root+"serialized-form.html", "components.html", "Components");

       addLineToFiles(root+"com", "d2wlist.html", "D2WKeys");
       addLineToFiles(root+"com", "components.html", "Components");

       addLineToFiles(root+"er", "d2wlist.html", "D2WKeys");
       addLineToFiles(root+"er", "components.html", "Components");
    }
}
