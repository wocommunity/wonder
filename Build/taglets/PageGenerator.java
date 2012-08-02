
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;

import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.DocErrorReporter;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;

import org.jdom.input.SAXBuilder;

/**
 * Doclet for generating the "Components" page. It searches for classes
 * which have WOComponent as an ancestor and finds the resources associated
 * with the component and pulls them together for the list.
 *
 * There is a test() method which, internally, checks the logic of the
 * extractions. An new problemmatic comments are found, they can be added to
 * that method.
 * 
 * @author kiddyr
 */
public class PageGenerator {

    static final String TABLE_TOP_START = "<table border=\"1\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\" summary=\"\">\n"+
                                          "<tr bgcolor=\"#CCCCFF\" class=\"TableHeadingColor\">\n"+
                                          "<th ALIGN=\"left\" colspan=\"2\"><font size=\"+2\">\n";

    static final String TABLE_TOP_END = "</tr>\n";

    static final String TABLE_END = "</table>\n";

    static void dumpComps(HashMap<String,HashMap<String,Object>> comps) {
        System.out.println("");
        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            System.out.println("key: \""+key+"\" -> "+comps.get(key));
        }
        System.out.println("");
    }

    static void writeFileToWriter(String filename, FileWriter out) {

        File src = new File(filename);
        FileReader fRdr = null;

        try {
            fRdr = new FileReader(src);
        } catch (java.io.FileNotFoundException fnfe) { throw new IllegalArgumentException(fnfe.getMessage()); }

        LineNumberReader rdr = new LineNumberReader(fRdr);

        String line = "";
        while (line != null) {
            try {
                line = rdr.readLine();
            } catch (java.io.IOException ioe) { line = null; }

            try {
                if (line != null) out.write(line+"\n");
            } catch (java.io.IOException ioe) { throw new IllegalArgumentException(ioe.getMessage()); }
        }
    }

    /**
     * Find the classes for the page being generated and return a HashMap that uses the classname
     * as a key which points to another HashMap that contains data about the class.
     */
    static HashMap<String,HashMap<String,Object>> findSubClassesFromAvailable(ClassDoc[] classes, String classname) {

        HashMap<String,HashMap<String,Object>> found = new HashMap<String,HashMap<String,Object>>();

        for (int i = 0; i < classes.length; i++) {

            ClassDoc aCD = classes[i];

            if (!aCD.toString().startsWith("com.webobjects")) {
                ClassDoc parent = aCD.superclass();
                boolean done = false;

                while (parent != null && !done) {
                    if (parent.toString().equals(classname)) {
                        // System.out.println("found component: "+aCD);
                        found.put(aCD.toString(), new HashMap<String,Object>());
                        done = true;
                    }
                    if (parent.toString().equals("java.lang.Object"))
                        done = true;
                    else
                        parent = parent.superclass();
                }
            }
        }

        return found;
    }

    static void findSourceFiles(HashMap<String,HashMap<String,Object>> comps, ArrayList<String> srcDirs) {

        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();

            int lastDot = 0;
            int currentDot = key.indexOf(".", lastDot);
            while (currentDot > lastDot) {
                lastDot = currentDot;
                currentDot = key.indexOf(".", lastDot+1);
            }
            String leaf = key.substring(lastDot+1);
            comps.get(key).put("componentName", leaf);

            boolean found = false;

            String srcDir = key.replaceAll("\\.", File.separator);

            for (int jdx = 0; !found && jdx < srcDirs.size(); jdx++) {
                String srcFilename = srcDirs.get(jdx)+File.separator+leaf+".java";
                if ((new File(srcFilename)).exists())
                    comps.get(key).put("sourceFile", srcFilename);
            }
        }
    }

    static void findPackages(HashMap<String,HashMap<String,Object>> comps) {

        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String[] parts = key.split("\\.");
            if (parts.length == 0)
                comps.get(key).put("package", "NONE");
            else {
                comps.get(key).put("package", key.substring(0,key.length()-parts[parts.length-1].length()-1));
            }
        }
    }

    static void findApiFiles(HashMap<String,HashMap<String,Object>> comps, ArrayList<String> compDirs) {

        Iterator keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            int lastDot = 0;
            int currentDot = key.indexOf(".", lastDot);
            while (currentDot > lastDot) {
                lastDot = currentDot;
                currentDot = key.indexOf(".", lastDot+1);
            }
            String leaf = key.substring(lastDot+1);
            comps.get(key).put("componentName", leaf);

            boolean found = false;

            for (int jdx = 0; !found && jdx < compDirs.size(); jdx++) {
                String apiFilename = compDirs.get(jdx)+leaf+".api";
                File apiFile = new File(apiFilename);
                if (apiFile.exists())
                    comps.get(key).put("apiFile", apiFilename);
            }
        }
    }

    static void gatherAllComments(HashMap<String,HashMap<String,Object>> comps) {

        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            FileReader fRdr = null;
            String srcFilename = (String)comps.get(key).get("sourceFile");
            if (srcFilename != null) {
                try {
                    fRdr = new FileReader(srcFilename);
                } catch (java.io.FileNotFoundException fnfe) { System.out.println(fnfe.getMessage()); System.exit(1); }

                LineNumberReader rdr = new LineNumberReader(fRdr);

                boolean done = false;
                boolean inComment = false;

                ArrayList<String> comments = new ArrayList<String>();

                String line = "";

                while (line != null && !done) {
                    try {
                        line = rdr.readLine();
                        if (line != null) {
                            if (line.indexOf("/**") >= 0) { inComment = true; }

                            if (line.startsWith("public class")) { done = true; }
                            if (inComment && line.indexOf("*/") >= 0) { done = true; }
                            if (inComment) { comments.add(line); }
                        }
                    } catch (java.io.IOException ioe) { line = null; }
                }
                comps.get(key).put("allComments", comments);
            } else
                comps.get(key).put("allComments", new ArrayList<String>());
        }
    }

    static TreeMap<String,TreeSet<String>> gatherClassesByPrefix(HashMap<String,HashMap<String,Object>> comps, String[] prefixes) {

        // Gather the classnames by prefix.
        //
        TreeSet<String> classNames = new TreeSet<String>(comps.keySet());
        Iterator<String> names = classNames.iterator();

        TreeMap<String,TreeSet<String>> foundPrefixes = new TreeMap<String,TreeSet<String>>();

        while (names.hasNext()) {
            String current = names.next();
            String[] parts = current.split("\\.");
            String lastName = parts[parts.length-1];

            String prefixFound = null;

            for (int idx = 0; idx < prefixes.length && prefixFound == null; idx++) {
                if (lastName.startsWith(prefixes[idx])) prefixFound = prefixes[idx];
            }

            if (prefixFound == null) prefixFound = "NONE";

            // System.out.println("name: \""+current+"\", lastName = \""+lastName+"\", prefix = \""+prefixFound+"\"");

            TreeSet<String> classesForPrefix = foundPrefixes.get(prefixFound);
            if (classesForPrefix == null) {
                foundPrefixes.put(prefixFound, new TreeSet<String>());
                classesForPrefix = foundPrefixes.get(prefixFound);
            }
            classesForPrefix.add(current);
        }

        return foundPrefixes;
    }

    static String classDocURL(String className) {
    	return className.replace('.','/')+".html";
    }

	static void findClassDocURLs(HashMap<String, HashMap<String, Object>> comps) {
		Iterator<String> keys = comps.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String path = key.replace('.', '/');
			comps.get(key).put("classDocURL", path + ".html");
		}
	}

    static String packageNameFromClassName(String className) {
    	return className.substring(0,className.lastIndexOf("."));
    }

    static void findClassComments(HashMap<String,HashMap<String,Object>> comps) {

        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String found = findClassComment((HashMap<String,Object>)comps.get(key));
            //String found = findClassComment((List<String>)comps.get(key).get("allComments"));
            if (found != null) comps.get(key).put("classComment", found);
        }
    }

    /**
     * Returns the first sentence of the javadoc for a component's
     * class. It is a shame that we cannot leverage javadoc for this, but
     * there are no hooks for this. We are looking for "end" strings from
     * the ends array, below. So far, this contains ". ", ".<", and ".\" ".
     * 
     * Because memory is a problem, we are going to remove the class comment
     * from the allComments array as we locate it. 
     */
    @SuppressWarnings("unchecked")
    static String findClassComment(HashMap<String,Object> comp) {
    		//List<String> comments) {

        // System.out.println("start: comments: "+comments);

    	List<String> comments = (List<String>)comp.get("allComments");

        if (comments == null || comments.size() == 0)
            return "";

        
        boolean done = false;

        // Coalesce the lines of the comment into one string.
        //
        String str = comments.get(0).replaceFirst("/\\*\\*", "").trim();
        for (int idx = 1; idx < comments.size(); idx++) {
            String other = comments.get(idx).trim();

            // this does not work the same as the while after...
            //other.replace("^\\**", "");

            while (other.startsWith("*")) { other = other.substring(1); }
            str = (str.trim()+" "+other.trim()).trim();
        }

        if (str.equals("") || str.startsWith("@")) return "";

        // Locate the end markers.
        //
        String[] ends = new String[] { ". ", ".<", ".\" ", " @", ".*/", ".*<" };

        int end = str.length();
        String endMarker = null;

        for (int idx = 0; idx < ends.length; idx++) {
            if (str.indexOf(ends[idx]) >= 0 && str.indexOf(ends[idx]) < end) {
                end = str.indexOf(ends[idx]);
                endMarker = ends[idx];
            }
        }

        if (endMarker != null)
            str = str.substring(0,end+endMarker.length()-1).trim();
 
        // System.out.println("done: str: \""+str+"\"");
        return str;
    }

    static String paddedNumber(int num, int width) {
        String str = ""+num;
        while (str.length() < width) str = "0"+str;
        return str;
    }

    /**
     * Take a list of comment strings, eg ("/**", "Something", "", " * @binding some", "\*\/"),
     * and return a dictionary of binding names to binding comment structures.
     * 
     * @param comps
     * @param tag
     */
    @SuppressWarnings("unchecked")
    static void findTagComments(HashMap<String, HashMap<String, Object>> comps, String tag) {
        // System.out.println("start: comments = "+comments);

        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();

        ArrayList<String> comments = (ArrayList<String>)comps.get(key).get("allComments");

        ArrayList<String> found = new ArrayList<String>();

        boolean inTag = false;

        // Start with the class comment block and turn this into an array of lines that
        // all start with a tag.
        //
        // TODO: we do not get rid of tabs.... (or do we now, with trim())
        //
        for (int idx = 0; idx < comments.size(); idx++) {
            String line = comments.get(idx);
            line = line.trim();
            if (line.startsWith("*") && !line.startsWith("*/")) line = line.substring(1);
            line = line.trim();

            if (line.startsWith(tag)) inTag = true;

            if (line.indexOf("*/") >= 0) inTag = false;
            if (line.length() == 0) inTag = false; 
            if (line.startsWith("@") && !line.startsWith(tag)) inTag = false;

            if (inTag) found.add(line);
        }
        // System.out.println("found: "+found);

        ArrayList<String> found2 = new ArrayList<String>();

        // This will collapse multi-line comments to one line.
        //
        int jdx = -1;
        for (int idx = 0; idx < found.size(); idx++) {
            if (found.get(idx).startsWith("@")) { jdx++; found2.add(found.get(idx)); }
            else { found2.set(jdx, found2.get(jdx)+" "+found.get(idx)); }
        }

        // System.out.println("found2: "+found2);

        HashMap<String,HashMap<String,String>> finished = new HashMap<String,HashMap<String,String>>();

        // Construct the HashMap that holds the bindings. Fix the key, if needed.
        //
        for (int idx = 0; idx < found2.size(); idx++) {
            String line = found2.get(idx);

            line = line.substring(tag.length());
            line = line.trim();

            String name;
            if (line.indexOf(" ") > 0)
                name = line.substring(0,line.indexOf(" "));
            else
                name = line;

//            Character c = name.charAt(name.length()-1);
//            while (!Character.isJavaIdentifierPart(c)) {
//                name = name.substring(0,name.length()-1);
//                c = name.charAt(name.length()-1);
//            }

            if (line.indexOf(" ") > 0)
                line = line.substring(line.indexOf(" "));
            else
                line = "";
            line = line.trim();

            HashMap<String,String> data = new HashMap<String,String>();
            data.put("name", name);
            data.put("comment", line);
            data.put("order", paddedNumber(idx, 3));
            finished.put(name, data);
        }

        comps.get(key).put("comments", finished);
        comps.get(key).remove("allComments");
        }
    }
}
