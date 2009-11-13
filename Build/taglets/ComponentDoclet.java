
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
 */
public class ComponentDoclet extends com.sun.javadoc.Doclet {

    static ArrayList<String> srcDirs;
    static ArrayList<String> compDirs;

    static HashMap<String,HashMap<String,Object>> comps;

    @SuppressWarnings("unchecked")
    public static boolean start(RootDoc root) {

        test();

        ClassDoc[] classes = root.classes();

        String[] prefixes = new String[] { "Selenium", "ERDAjax", "PayPal", "ERD2W", "ERDIV", "ERIUI", "ERNEU",
                                           "ERPDF", "ERXJS", "Ajax", "SEEO", "UJAC", "D2W", "ERC", "ERD", "ERO", "ERP", "ERX",
                                           "GSV", "WOL", "YUI", "ER", "GC", "IM", "JS", "SC", "SE", "WO", "WR", "WX" };

        comps = new HashMap<String,HashMap<String,Object>>();

        // Collect into comps{} new hashes for classes that are sub-classes of WOComponent.
        //
        for (int i = 0; i < classes.length; i++) {

            ClassDoc aCD = classes[i];

            if (!aCD.toString().startsWith("com.webobjects")) {
                ClassDoc parent = aCD.superclass();
                boolean done = false;

                while (parent != null && !done) {
                    if (parent.toString().equals("com.webobjects.appserver.WOElement")) {
                        // System.out.println("found component: "+aCD);
                        comps.put(aCD.toString(), new HashMap<String,Object>());
                        done = true;
                    }
                    if (parent.toString().equals("java.lang.Object"))
                        done = true;
                    else
                        parent = parent.superclass();
                }
            }
        }

        // Gather the classnames by prefix.
        //
        TreeSet<String> classNames = new TreeSet<String>(comps.keySet());
        Iterator<String> names = classNames.iterator();

        TreeMap<String,TreeSet<String>> classNamePrefixes = new TreeMap<String,TreeSet<String>>();

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

            TreeSet<String> classesForPrefix = classNamePrefixes.get(prefixFound);
            if (classesForPrefix == null) {
                classNamePrefixes.put(prefixFound, new TreeSet<String>());
                classesForPrefix = classNamePrefixes.get(prefixFound);
            }
            classesForPrefix.add(current);
        }

        // For each subclass of WOComponent that had been found, get its source file name
        // and api file name (if one exists) and component name and put it in the hashmap pointed
        // to by the class name in comps.
        //
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

            found = false;

            String srcDir = key.replaceAll("\\.", File.separator);

            for (int jdx = 0; !found && jdx < srcDirs.size(); jdx++) {
                String srcFilename = srcDirs.get(jdx)+File.separator+leaf+".java";
                if ((new File(srcFilename)).exists())
                    comps.get(key).put("sourceFile", srcFilename);
            }
        }

        // Gather the class comments, binding comments and package name for the classes of the components.
        //
        keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            // For null-safety later on
            //
            comps.get(key).put("package", "NONE");

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
                            if (line.startsWith("package ")) {
                                String pName = line.substring("package ".length());
                                while (pName.endsWith(";")) pName = pName.substring(0,pName.length()-1);
                                comps.get(key).put("package", pName);

                                pName = pName.replace('.','/');
                                comps.get(key).put("classDocURL",pName+"/"+comps.get(key).get("componentName")+".html");
                            }
                            if (line.indexOf("/**") >= 0) { inComment = true; }

                            // xxxxx
                            if (line.startsWith("public class")) { done = true; }
                            if (inComment && line.indexOf("*/") >= 0) { done = true; }
                            if (inComment) { comments.add(line); }
                        }
                    } catch (java.io.IOException ioe) { line = null; }
                }

                comps.get(key).put("comments", findBindingComments(comments));
                comps.get(key).put("classComment", findClassComment(comments));
            } else {
                comps.get(key).put("comments", new HashMap<String,String>());
                comps.get(key).put("classComment", "");
            }
        }

        // Gather the bindings for the component from the api file.
        //
        keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            String apiFilename = (String)comps.get(key).get("apiFile");
            if (apiFilename != null) {

                File apiFile = new File(apiFilename);
                SAXBuilder builder = new SAXBuilder();
                Document doc = null;

                ArrayList<String> bindingNames = new ArrayList<String>();

                try {
                    doc = builder.build(apiFile);
                } catch (org.jdom.JDOMException jde) { System.out.println(jde.getMessage()); System.exit(1); }
                Element parent = doc.getRootElement().getChild("wo");
                Iterator bindings = parent.getChildren("binding").iterator();
                while (bindings.hasNext()) {
                    Attribute attr = ((Element)bindings.next()).getAttribute("name");
                    bindingNames.add(attr.getValue());
                }
                comps.get(key).put("apiBindings", bindingNames);
            } else
                comps.get(key).put("apiBindings", new ArrayList<String>());
        }

        // Check the condition of the bindings documentation.
        //
        keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            HashMap<String,Object> map = comps.get(key);

            if (!map.containsKey("apiBindings") && !map.containsKey("comments")) { comps.get(key).put("Ok", "YES"); }

            if (map.containsKey("apiBindings") && !map.containsKey("comments")) { comps.get(key).put("Ok", "NO"); }
            if (!map.containsKey("apiBindings") && map.containsKey("comments")) { comps.get(key).put("Ok", "NO"); }

            if (map.containsKey("apiBindings") && map.containsKey("comments")) {

                ArrayList<String> apiBindings = (ArrayList<String>)comps.get(key).get("apiBindings");
                java.util.Set jdBindings = ((HashMap<String,String>)comps.get(key).get("comments")).keySet();

                if (apiBindings == null && jdBindings == null) comps.get(key).put("Ok", "YES");

                if (apiBindings == null && jdBindings != null) comps.get(key).put("Ok", "NO");
                if (apiBindings != null && jdBindings == null) comps.get(key).put("Ok", "NO");

                if (apiBindings != null && jdBindings != null) {

                    if (apiBindings.size() == 0 && jdBindings.size() == 0) { comps.get(key).put("Ok", "YES"); }

                    if (apiBindings.size() != jdBindings.size()) { comps.get(key).put("Ok", "NO"); }

                    if (apiBindings.size() != 0 && apiBindings.size() == jdBindings.size()) {
                        java.util.HashSet apiSet = new java.util.HashSet(apiBindings);
                        comps.get(key).put("Ok", (jdBindings.equals(apiSet)) ? "YES" : "NO");
                    }
                }
            }
        }

        TreeMap<String,ArrayList<String>> packageInfo = new TreeMap<String,ArrayList<String>>();

        keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            ArrayList<String> aPackage = packageInfo.get((String)comps.get(key).get("package"));
            if (aPackage == null) {
                packageInfo.put((String)comps.get(key).get("package"), new ArrayList<String>());
                aPackage = packageInfo.get((String)comps.get(key).get("package"));
            }
            aPackage.add(key);
        }

        // System.out.println("packageInfo: "+packageInfo);

        FileWriter out = null;
        FileWriter check = null;
 
        try {
            out = new FileWriter("components.html");
            check = new FileWriter("components.txt");

            writeHead(out);

            out.write("<ul>\n");
            out.write("<li><a href=\"#ListedByPrefix\">Prefixes</a></li>\n");
            out.write("<li><a href=\"#ListedByPackage\">Packages/Classnames</a></li>\n");
            out.write("<li><a href=\"#ComponentDetails\">Component Details</a></li>\n");
            out.write("</ul>\n");

            Iterator<String> prefxs = classNamePrefixes.keySet().iterator();

            out.write("<a name=\"ListedByPrefix\"/>\n");

            while (prefxs.hasNext()) {
                String prefix = prefxs.next();

                out.write("<table border=\"1\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\" summary=\"\">\n");
                out.write("<tr bgcolor=\"#CCCCFF\" class=\"TableHeadingColor\">\n");
                out.write("<th ALIGN=\"left\" colspan=\"2\"><font size=\"+2\">\n");

                out.write("<b>Prefix: "+prefix+"</b></font></th>\n");

                out.write("</tr>\n");

                StringBuffer str = new StringBuffer();
                Iterator<String> namesForPrefix = classNamePrefixes.get(prefix).iterator();
                while (namesForPrefix.hasNext()) {
                    String current = namesForPrefix.next();
                    String[] parts = current.split("\\.");
                    String lastName = parts[parts.length-1];

                    str.append("<a href=\"#"+current+"\">"+lastName+"</a>, ");
                }

                String str2 = str.toString().substring(0,str.length()-2);
                out.write("<tr bgcolor=\"white\" CLASS=\"TableRowColor\">\n");

                out.write("<td>"+str2+"</td></tr>\n");
                out.write("</table>\n&nbsp;<p>\n");
            }

            keys = packageInfo.keySet().iterator();

            out.write("<a name=\"ListedByPackage\"/>\n");

            while (keys.hasNext()) {
                String key = (String)keys.next();

                // System.out.println("writing for key: "+key);

                out.write("<table border=\"1\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\" summary=\"\">\n");
                out.write("<tr bgcolor=\"#CCCCFF\" class=\"TableHeadingColor\">\n");
                out.write("<th ALIGN=\"left\" colspan=\"2\"><font size=\"+2\">\n");

                out.write("<b>Package: "+key+"</b></font></th>\n");

                out.write("</tr>\n");

                Iterator<String> compKeys = (new TreeSet(packageInfo.get(key))).iterator();
                while (compKeys.hasNext()) {
                    String compKey = compKeys.next();

                    out.write("<tr bgcolor=\"white\" CLASS=\"TableRowColor\">\n");

                    out.write("<td><a href=\"#"+compKey+"\" title=\""+compKey+"\">"+comps.get(compKey).get("componentName")+"</a>\n");

                    out.write("<br/>\n");

                    out.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<I>class "+compKey+"</I>&nbsp;</td>\n");
                    out.write("</tr>\n");
                }
                out.write("</table>\n&nbsp;<p>\n");
            }

            out.write("<hr size=\"4\" noshade>\n");

            out.write("<a name=\"ComponentDetails\"/>\n");

            keys = packageInfo.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String)keys.next();

                out.write("<a name=\"packagename\"><!-- --></A>\n");
                out.write("<table border=\"1\" width=\"100%\" celppadding=\"3\" cellspacing=\"0\" summary=\"\">\n");
                out.write("<tr bgcolor=\"#CCCCFF\" class=\"TableHeadingColor\">\n");
                out.write("<th align=\"left\" colspan=\"1\"><font size=\"+2\">\n");
                out.write("<b>Package: "+key+"</b></font></th>\n");
                out.write("</tr>\n");
                out.write("</table>\n");

                Iterator<String> compKeys = (new TreeSet(packageInfo.get(key))).iterator();
                while (compKeys.hasNext()) {
                    String compKey = compKeys.next();
                    HashMap map = comps.get(compKey);

                    String bindingCheck = "";

                    out.write("<a name=\""+compKey+"\"><!-- --></a>\n");
                    out.write("<h3>"+comps.get(compKey).get("componentName")+"</h3>\n");

                    if (!comps.get(compKey).get("package").equals("NONE")) {
                        out.write("<p>From <a href=\""+comps.get(compKey).get("classDocURL")+"\">");
                        out.write(map.get("package")+"."+map.get("componentName"));
                        out.write("</a>:\n");
                        bindingCheck += "          warning: No package defined for class\n";

                        if (map.get("classComment") != null && ((String)map.get("classComment")).length() != 0)
                            out.write((String)map.get("classComment"));
                        else {
                            out.write("<i>Class documentation missing.</i>");
                            bindingCheck += "          warning: No class comment defined for class\n";
                        }
                        out.write("</p>\n");
                    }

                    HashMap<String,HashMap<String,String>> commentsMap = (HashMap<String,HashMap<String,String>>)map.get("comments");

                    /* In the block below, I write out the bindings from the commentsSet first, and then the bindings from the api set.
                       Therefore I only need to worry about the sort order in the commentsSet. -rrk */

                    Set<String> commentsSet = commentsMap.keySet();

                    ArrayList<String> apiBindings = (ArrayList<String>)map.get("apiBindings");

                    if (apiBindings.size() == 0 && commentsSet.size() == 0)
                        out.write("<i>No bindings for Component.</i>\n");
                    else {
                        out.write("<table border=\"1\">\n");
                        out.write("<tr><th><i>binding</i></th><th><i>comment</i></th></tr>\n");
                        out.write("<tr>\n");

                        TreeMap<String,String> orderingMap = new TreeMap<String,String>();
                        Iterator<String> comments = commentsSet.iterator();
                        while (comments.hasNext()) {
                            String foundName = comments.next();
                            HashMap<String,String> commentInfo = commentsMap.get(foundName);
                            orderingMap.put(commentInfo.get("order"), commentInfo.get("name"));
                        }

                        Iterator<String> ordering = orderingMap.keySet().iterator();

                        while (ordering.hasNext()) {
                            String bindingName = orderingMap.get(ordering.next());
                            out.write("<td>"+bindingName+"</td>\n");

                            String bindingComment = (String)((HashMap<String,String>)commentsMap.get(bindingName)).get("comment");

                            if (bindingComment == null || bindingComment.length() == 0) {
                                out.write("<td>&nbsp;</td>\n");
                                bindingCheck += "          Binding: \""+bindingName+"\": binding tag in javadoc but no/empty comment\n";
                            } else {
                                out.write("<td>"+bindingComment+"</td>\n");
                            }
                            out.write("</tr>\n");
                        }

                        Iterator<String> bindings = apiBindings.iterator();

                        while (bindings.hasNext()) {
                            String binding = bindings.next();
    
                            if (!commentsSet.contains(binding)) {
                                out.write("<tr><td>"+binding+"</td>\n<td>&nbsp;</td></tr>\n");
                                bindingCheck += "          Binding: \""+binding+"\": api file entry but no binding tag\n";
                            }
                        }

                        out.write("</table>\n");
                    }

                    check.write("    componentName: \""+map.get("componentName")+"\"\n");
                    check.write("          package: \""+map.get("package")+"\"\n");
                    check.write("          apiFile: \""+map.get("apiFile")+"\"\n");
                    check.write("       sourceFile: \""+map.get("sourceFile")+"\"\n");
                    check.write("         comments: \""+map.get("comments")+"\"\n");
                    check.write("      apiBindings: \""+map.get("apiBindings")+"\"\n");
                    check.write("     classComment: \""+map.get("classComment")+"\"\n");
                    check.write("      classDocURL: \""+map.get("classDocURL")+"\"\n");
                    check.write("        condition: \""+map.get("Ok")+"\"\n");
                    if (!bindingCheck.equals(""))
                        check.write(bindingCheck);
                    check.write("\n");

                    out.write("<hr/>\n\n");
                }
            }

            writeTail(out);

            out.close();

        } catch (java.io.IOException ioe) {
            System.err.println("Error writing to /tmp/foo.html"); System.exit(1);
        }

        return false;
    }

    /** Returns the first sentence of the javadoc for a component's
     *  class. It is a shame that we cannot leverage javadoc for this, but
     *  there are no hooks for this. We are looking for "end" strings from
     *  the ends array, below. So far, this contains ". ", ".<", and ".\" ".
     */
    static String findClassComment(List<String> comments) {

        // System.out.println("start: comments: "+comments);

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

    /**
     * Take a list of comment strings, eg ("/**", "Something", "", " * @binding some", "\*\/"),
     * and return a dictionary of binding names to binding comment structures.
     */
    static HashMap<String,HashMap<String,String>> findBindingComments(List<String> comments) {

        // System.out.println("start: comments = "+comments);

        ArrayList<String> found = new ArrayList<String>();

        boolean inBinding = false;

        // Start with the class comment block and turn this into an array of lines that
        // all start with a @binding tag.
        //
        // TODO: we do not get rid of tabs.... (or do we now, with trim())
        //
        for (int idx = 0; idx < comments.size(); idx++) {
            String line = comments.get(idx);
            line = line.trim();
            if (line.startsWith("*") && !line.startsWith("*/")) line = line.substring(1);
            line = line.trim();

            if (line.startsWith("@binding")) inBinding = true;

            if (line.indexOf("*/") >= 0) inBinding = false;
            if (line.length() == 0) inBinding = false; 
            if (line.startsWith("@") && !line.startsWith("@binding")) inBinding = false;

            if (inBinding) found.add(line);
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

            line = line.substring("@binding".length());
            line = line.trim();

            String name;
            if (line.indexOf(" ") > 0)
                name = line.substring(0,line.indexOf(" "));
            else
                name = line;

// XXX - why did adding this make me start to get unchecked warnings?
//
            Character c = name.charAt(name.length()-1);
            while (!Character.isJavaIdentifierPart(c)) {
                name = name.substring(0,name.length()-1);
                c = name.charAt(name.length()-1);
            }

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

        // System.out.println("finished: "+finished);
        return finished;
    }

    static String paddedNumber(int num, int width) {
        String str = ""+num;
        while (str.length() < width) str = "0"+str;
        return str;
    }

    static void test() {

        int result = 0;

        ArrayList<String> tester = new ArrayList<String>();
        HashMap<String,HashMap<String,String>> foundComment = null;
        HashMap<String,HashMap<String,String>> expectedComment = null;
        HashMap<String,String> oneComment = null;

        tester.add("/**");
        tester.add(" * Given an object displays a link to show information about the editing context of that object.*<br />");
        tester.add(" *");
        tester.add(" * @binding object");
        tester.add(" */");
        tester.add("");

        expectedComment = new HashMap<String,HashMap<String,String>>();

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "object");
        oneComment.put("comment", "");
        oneComment.put("order", "000");
        expectedComment.put(oneComment.get("name"), oneComment);

        foundComment = findBindingComments(tester);

        result += checkTest("test1", tester, expectedComment, foundComment);

/* XXX - If I put a * at the start of a line, or multiple *'s, does it break things? */

        tester = new ArrayList<String>();
        tester.add("/**");
        tester.add("  * AjaxSocialNetworkLink creates a link to the submission URL for ");
        tester.add("  * a social network around the social network's icon.");
        tester.add("  * ");
        tester.add("  * @author mschrag");
        tester.add("@binding name the name of the social network (@see er.ajax.AjaxSocialNetwork.socialNetworkNamed)");
        tester.add("  * @binding url the URL to submit");
        tester.add("  * @binding url2 the  ");
        tester.add("  *    some exta stuff");
        tester.add("");
        tester.add("  * @binding title the title to submit");
        tester.add("     ");
        tester.add("  * @binding alt the alt tag (defaults to the name of the network)");
        tester.add("  * @binding extra");
        tester.add(" * @binding action, optional action to call before opening the modal dialog.");
        tester.add("  * @binding target the target of the link");
        tester.add("  */");

        expectedComment = new HashMap<String,HashMap<String,String>>();

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "url");
        oneComment.put("comment", "the URL to submit");
        oneComment.put("order", "001");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "url2");
        oneComment.put("comment", "the some exta stuff");
        oneComment.put("order", "002");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "title");
        oneComment.put("comment", "the title to submit");
        oneComment.put("order", "003");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "alt");
        oneComment.put("comment", "the alt tag (defaults to the name of the network)");
        oneComment.put("order", "004");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "extra");
        oneComment.put("comment", "");
        oneComment.put("order", "005");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "target");
        oneComment.put("comment", "the target of the link");
        oneComment.put("order", "007");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "name");
        oneComment.put("comment", "the name of the social network (@see er.ajax.AjaxSocialNetwork.socialNetworkNamed)");
        oneComment.put("order", "000");
        expectedComment.put(oneComment.get("name"), oneComment);

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "action");
        oneComment.put("comment", "optional action to call before opening the modal dialog.");
        oneComment.put("order", "006");
        expectedComment.put(oneComment.get("name"), oneComment);

        foundComment = findBindingComments(tester);

        result += checkTest("test2", tester, expectedComment, foundComment);

        String expected = "AjaxSocialNetworkLink creates a link to the submission URL for a social network around the social network's icon.";
        String found = findClassComment(tester);

        result += checkTest("test3", tester, expected, found);

        tester = new ArrayList<String>();
        tester.add("/**");
        tester.add("* Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>\".");
        tester.add("* <br/>");
        tester.add("* Synopsis:<br/>");
        tester.add("* email=<i>anEmail</i>;");
        tester.add("* <br/>");
        tester.add("* Bindings:<br/>");
        tester.add("* <b>email</b> email to generate href");
        tester.add("* <br/>");
        tester.add("*/");

        expected = "Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>\".";
        found = findClassComment(tester);

        result += checkTest("test4", tester, expected, found);

        tester = new ArrayList<String>();
        tester.add("/**");
        tester.add("* Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>.\"");
        tester.add("* <br/>");
        tester.add("*/");

        expected = "Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>.\"";
        found = findClassComment(tester);

        result += checkTest("test5", tester, expected, found);

        tester = new ArrayList<String>();
        tester.add("/**");
        tester.add(" * @binding id the id of the update container");
        tester.add(" * @binding progressID the id of the AjaxProgress");

        expected = "";
        found = findClassComment(tester);

        result += checkTest("test6", tester, expected, found);

        tester = new ArrayList<String>();
        tester.add("/** I can check here also");
        tester.add(" * @binding id the id of the update container");
        tester.add(" * @binding progressID the id of the AjaxProgress");

        expected = "I can check here also";
        found = findClassComment(tester);

        result += checkTest("test7", tester, expected, found);

        tester = new ArrayList<String>();
        tester.add("/**");
        tester.add(" * XHTML version of WORadioButtonList");
        tester.add(" *");
        tester.add(" * @see WORadioButtonList");
        tester.add(" * @author mendis");
        tester.add(" *");
        tester.add(" */");

        expected = "XHTML version of WORadioButtonList";
        found = findClassComment(tester);

        result += checkTest("test8", tester, expected, found);

        if (result > 0) { System.exit(1); }
    }

    static int checkTest(String name, ArrayList<String> tester, Object expected, Object found) {

        if (name == null | name.length() == 0 || tester == null || expected == null || found == null) return 1;

        if (!found.equals(expected)) {
            System.err.println("ERROR:");
            System.err.println("\n"+name+":\n"+tester+"\n");
            System.err.println("expected: \""+expected+"\"\n");
            System.err.println("   found: \""+found+"\"\n");
            return 1;
        } else {
            System.out.println(name+": ok");
            return 0;
        }
    }

    static void writeHead(FileWriter out) {

       try {
            out.write("<html><head>\n");
            out.write("<meta name=\"ROBOTS\" content=\"NOINDEX\" />\n");
            out.write("<title>WOComponents</title>\n");
            out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" title=\"Style\">\n");
            out.write("<script type=\"text/javascript\">\n");
            out.write("function windowTitle()\n");
            out.write("{\n");
            out.write("    parent.document.titlea=\"WOComponents\";\n");
            out.write("}\n");
            out.write("</script>\n");
            out.write("<noscript></noscript>\n");

            out.write("<body bgcolor=\"white\" onload=\"windowTitle();\">\n");

            out.write("<!-- ========= START OF TOP NAVBAR ======= -->\n");
            out.write("<A NAME=\"navbar_top\"><!-- --></A>\n");
            out.write("<A HREF=\"#skip-navbar_top\" title=\"Skip navigation links\"></A>\n");
            out.write("<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\" SUMMARY=\"\">\n");
            out.write("<TR>\n");
            out.write("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">\n");
            out.write("<A NAME=\"navbar_top_firstrow\"><!-- --></A>\n");
            out.write("<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\" SUMMARY=\"\">\n");
            out.write("  <TR ALIGN=\"center\" VALIGN=\"top\">\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"overview-summary.html\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">Package</FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">Class</FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">Use</FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"overview-tree.html\"><FONT CLASS=\"NavBarFont1\"><B>Tree</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1Rev\"> &nbsp;<FONT CLASS=\"NavBarFont1Rev\"><B>Components</B></FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"index-all.html\"><FONT CLASS=\"NavBarFont1\"><B>Index</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"help-doc.html\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>\n");
            out.write("  </TR>\n");
            out.write("</TABLE>\n");
            out.write("</TD>\n");
            out.write("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>\n");
            out.write("<em>Project Wonder 5.0</em></EM>\n");
            out.write("</TD>\n");
            out.write("</TR>\n");
            out.write("\n");
            out.write("<TR>\n");
            out.write("<TD BGCOLOR=\"white\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">\n");
            out.write("&nbsp;PREV&nbsp;\n");
            out.write("&nbsp;NEXT</FONT></TD>\n");
            out.write("<TD BGCOLOR=\"white\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">\n");
            out.write("  <A HREF=\"index.html?overview-summary.html\" target=\"_top\"><B>FRAMES</B></A>  &nbsp;\n");
            out.write("&nbsp;<A HREF=\"overview-summary.html\" target=\"_top\"><B>NO FRAMES</B></A>  &nbsp;\n");
            out.write("&nbsp;<SCRIPT type=\"text/javascript\">\n");
            out.write("  <!--\n");
            out.write("  if(window==top) {\n");
            out.write("    document.writeln('<A HREF=\"allclasses-noframe.html\"><B>All Classes</B></A>');\n");
            out.write("  }\n");
            out.write("  //-->\n");
            out.write("</SCRIPT>\n");
            out.write("<NOSCRIPT>\n");
            out.write("  <A HREF=\"allclasses-noframe.html\"><B>All Classes</B></A>\n");
            out.write("</NOSCRIPT>\n");
            out.write("\n");
            out.write("\n");
            out.write("</FONT></TD>\n");
            out.write("</TR>\n");
            out.write("</TABLE>\n");
            out.write("<A NAME=\"skip-navbar_top\"></A>\n");
            out.write("<!-- ========= END OF TOP NAVBAR ========= -->\n");
            out.write("<HR/>\n\n");

            out.write("<center><h2><b>WOComponents</b></h2></center>\n");
            out.write("<hr size=\"4\" noshade>\n");

        } catch (java.io.IOException ioe) {
            System.err.println("ERROR: could not write to file: "+out);
        }
    }

    static void writeTail(FileWriter out) {

        try {
            out.write("<!-- ======= START OF BOTTOM NAVBAR ====== -->\n");
            out.write("<A NAME=\"navbar_bottom\"><!-- --></A>\n");
            out.write("<A HREF=\"#skip-navbar_bottom\" title=\"Skip navigation links\"></A>\n");
            out.write("<TABLE BORDER=\"0\" WIDTH=\"100%\" CELLPADDING=\"1\" CELLSPACING=\"0\" SUMMARY=\"\">\n");
            out.write("<TR>\n");
            out.write("<TD COLSPAN=2 BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">\n");
            out.write("<A NAME=\"navbar_bottom_firstrow\"><!-- --></A>\n");
            out.write("<TABLE BORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"3\" SUMMARY=\"\">\n");
            out.write("  <TR ALIGN=\"center\" VALIGN=\"top\">\n");
            out.write("  <TD BGCOLOR=\"#FFFFFF\" CLASS=\"NavBarCell1\">    <A HREF=\"overview-summary.html\"><FONT CLASS=\"NavBarFont1\"><B>Overview</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">Package</FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">Class</FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <FONT CLASS=\"NavBarFont1\">Use</FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"overview-tree.html\"><FONT CLASS=\"NavBarFont1\"><B>Tree</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\"> &nbsp;<FONT CLASS=\"NavBarFont1Rev\"><B>Components</B></FONT>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"index-all.html\"><FONT CLASS=\"NavBarFont1\"><B>Index</B></FONT></A>&nbsp;</TD>\n");
            out.write("  <TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"help-doc.html\"><FONT CLASS=\"NavBarFont1\"><B>Help</B></FONT></A>&nbsp;</TD>\n");
            out.write("  </TR>\n");
            out.write("</TABLE>\n");
            out.write("</TD>\n");
            out.write("<TD ALIGN=\"right\" VALIGN=\"top\" ROWSPAN=3><EM>\n");
            out.write("Last updated: Wed, Oct 21, 2009 &#149; 08:52 AM PDT</EM>\n");
            out.write("</TD>\n");
            out.write("</TR>\n");
            out.write("\n");
            out.write("<TR>\n");
            out.write("<TD BGCOLOR=\"white\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">\n");
            out.write("&nbsp;PREV&nbsp;\n");
            out.write("&nbsp;NEXT</FONT></TD>\n");
            out.write("<TD BGCOLOR=\"white\" CLASS=\"NavBarCell2\"><FONT SIZE=\"-2\">\n");
            out.write("  <A HREF=\"index.html?overview-summary.html\" target=\"_top\"><B>FRAMES</B></A>  &nbsp;\n");
            out.write("&nbsp;<A HREF=\"overview-summary.html\" target=\"_top\"><B>NO FRAMES</B></A>  &nbsp;\n");
            out.write("&nbsp;<SCRIPT type=\"text/javascript\">\n");
            out.write("  <!--\n");
            out.write("  if(window==top) {\n");
            out.write("    document.writeln('<A HREF=\"allclasses-noframe.html\"><B>All Classes</B></A>');\n");
            out.write("  }\n");
            out.write("  //-->\n");
            out.write("</SCRIPT>\n");
            out.write("<NOSCRIPT>\n");
            out.write("  <A HREF=\"allclasses-noframe.html\"><B>All Classes</B></A>\n");
            out.write("</NOSCRIPT>\n");
            out.write("\n");
            out.write("\n");
            out.write("</FONT></TD>\n");
            out.write("</TR>\n");
            out.write("</TABLE>\n");
            out.write("<A NAME=\"skip-navbar_bottom\"></A>\n");
            out.write("<!-- ======== END OF BOTTOM NAVBAR ======= -->\n");
            out.write("\n");
            
            out.write("</body></html>");
        } catch (java.io.IOException ioe) {
            System.err.println("ERROR writing to file: "+out);
        }
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter) {

        srcDirs = new ArrayList<String>();

        // System.out.println("options:");
        for (int idx = 0; idx < options.length; idx++) {
            String[] opt = options[idx];
            for (int jdx = 0; jdx < opt.length; jdx++) {
                if (jdx > 0 && options[idx][jdx-1].equals("-sourcepath")) {
                    int start = 0;
                    int end = options[idx][jdx].indexOf(":");
                    while (end > 0) {
                        String dir = options[idx][jdx].substring(start, end);
                        srcDirs.add(dir);
                        start = end + 1;
                        end = options[idx][jdx].indexOf(":", start);
                    }
                }
            }
        }

        java.util.HashMap<String,String> dirs = new java.util.HashMap<String,String>();

        for (int idx = 0; idx < srcDirs.size(); idx++) {
            //System.out.println("srcDirs["+idx+"] = \""+srcDirs.get(idx)+"\"");
            int found = srcDirs.get(idx).indexOf("/Sources/");
            if (found > 0)
                dirs.put(srcDirs.get(idx).substring(0,found)+File.separator+"Components"+File.separator, "ok");
        }

        compDirs = new ArrayList<String>(dirs.keySet());
        return true;
    }

    public static int optionLength(String option) {
        if (option.equals("-d")) return 2;
        return 1;
    }
}
