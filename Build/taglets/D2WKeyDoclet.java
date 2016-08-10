
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
 * Doclet for generating the "D2WKeys-able" page..... 
 */
public class D2WKeyDoclet extends com.sun.javadoc.Doclet {

    static ArrayList<String> srcDirs;
    static ArrayList<String> compDirs;

    static HashMap<String,HashMap<String,Object>> comps;

    @SuppressWarnings("unchecked")
    public static boolean start(RootDoc root) {

        // test();

        ClassDoc[] classes = root.classes();

        comps = PageGenerator.findSubClassesFromAvailable(classes, "com.webobjects.directtoweb.D2WComponent");

        PageGenerator.findSourceFiles(comps, srcDirs);

        PageGenerator.gatherAllComments(comps);

        PageGenerator.findClassComments(comps);

        PageGenerator.findClassDocURLs(comps);

        PageGenerator.findPackages(comps);

        PageGenerator.findTagComments(comps, "@d2wKey");

        // Check the condition of the bindings documentation.
        //
        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            HashMap<String,Object> map = comps.get(key);

            // What is there to do here? Not sure....
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

        //
        // Start writing out the html page
        //

        FileWriter out = null;
        FileWriter check = null;
 
        try {
            out = new FileWriter("d2wlist.html");
            check = new FileWriter("d2wlist.txt");

            PageGenerator.writeFileToWriter(System.getProperty("build.root")+"/Build/build/d2wlist-pageHead.html", out);

            out.write("<ul>\n");
            out.write("<li><a href=\"#ListedByPackage\">Packages/Classnames</a></li>\n");
            out.write("<li><a href=\"#ComponentDetails\">Component Details</a></li>\n");
            out.write("</ul>\n");

            keys = packageInfo.keySet().iterator();

            out.write("<a name=\"ListedByPackage\"/>\n");

            while (keys.hasNext()) {
                String key = (String)keys.next();

                System.out.println("writing for key: "+key);

                out.write(PageGenerator.TABLE_TOP_START);
                out.write("<b>Package: "+key+"</b></font></th>\n");
                out.write(PageGenerator.TABLE_TOP_END);

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

                // The "Package" header in the list of component detail entries
                //
                out.write(PageGenerator.TABLE_TOP_START);
                out.write("<b>Package: "+key+"</b></font></th>\n");
                out.write(PageGenerator.TABLE_TOP_END);
                out.write(PageGenerator.TABLE_END);

                Iterator<String> compKeys = (new TreeSet(packageInfo.get(key))).iterator();
                while (compKeys.hasNext()) {
                    String compKey = compKeys.next();
                    HashMap map = comps.get(compKey);

                    StringBuilder bindingCheck = new StringBuilder();

                    out.write("<a name=\""+compKey+"\"><!-- --></a>\n");
                    out.write("<h3>"+comps.get(compKey).get("componentName")+"</h3>\n");

                    out.write("<p>From <a href=\""+comps.get(compKey).get("classDocURL")+"\">");
                    out.write(map.get("package")+"."+map.get("componentName"));
                    out.write("</a>:\n");
                    bindingCheck.append("          warning: No package defined for class\n");

                    if (map.get("classComment") != null && ((String)map.get("classComment")).length() != 0)
                        out.write((String)map.get("classComment"));
                    else {
                        out.write("<i>Class documentation missing.</i>");
                        bindingCheck.append("          warning: No class comment defined for class\n");
                    }
                    out.write("</p>\n");

                    HashMap<String,HashMap<String,String>> commentsMap = (HashMap<String,HashMap<String,String>>)map.get("comments");

                    /* In the block below, I write out the bindings from the commentsSet first, and then the bindings from the api set.
                       Therefore I only need to worry about the sort order in the commentsSet. -rrk */

                    Set<String> commentsSet = commentsMap.keySet();

                    ArrayList<String> apiBindings = (ArrayList<String>)map.get("apiBindings");

                    if (commentsSet.size() == 0)
                        out.write("<i>No d2wKeys for Component.</i>\n");
                    else {

                        out.write("<table border=\"1\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\" summary=\"\">\n");
                        out.write("<tr bgcolor=\"#CCCCFF\" class=\"TableHeadingColor\"><td colspan=2><font size=\"+2\"><b>D2W Keys</b></font></td></tr>\n");

                        out.write("<tr bgcolor=\"white\" class=\"TableRowColor\">\n");

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
                            out.write("<td align=\"right\" valign=\"top\" width=\"1%\"><font size=\"-1\"><code>"+bindingName+"</code></font></td>\n");


                            String bindingComment = (String)((HashMap<String,String>)commentsMap.get(bindingName)).get("comment");

                            if (bindingComment == null || bindingComment.length() == 0) {
                                out.write("<td>&nbsp;</td>\n");
                                bindingCheck.append("          Binding: \"" + bindingName + "\": binding tag in javadoc but no/empty comment\n");
                            } else {
                                out.write("<td>"+bindingComment+"<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>\n");

                            }
                            out.write("</tr>\n");
                        }

                        out.write("</table>\n");
                    }

                    check.write("    componentName: \""+map.get("componentName")+"\"\n");
                    check.write("          package: \""+map.get("package")+"\"\n");
                    check.write("       sourceFile: \""+map.get("sourceFile")+"\"\n");
                    check.write("         comments: \""+map.get("comments")+"\"\n");
                    check.write("     classComment: \""+map.get("classComment")+"\"\n");
                    check.write("      classDocURL: \""+map.get("classDocURL")+"\"\n");
                    check.write("      allComments: \""+map.get("allComments")+"\"\n");
                    check.write("        condition: \""+map.get("Ok")+"\"\n");
                    if (bindingCheck.length() > 0)
                        check.write(bindingCheck.toString());
                    check.write("\n");

                    out.write("<hr/>\n\n");
                }
            }

            PageGenerator.writeFileToWriter(System.getProperty("build.root")+"/Build/build/d2wlist-pageTail.html", out);

            out.close();

        } catch (java.io.IOException ioe) {
            System.err.println("Error writing to /tmp/foo.html"); System.exit(1);
        }

        return false;
    }
 
    static void test() {

        int result = 0;
/*
        ArrayList<String> tester = new ArrayList<String>();
        HashMap<String,HashMap<String,String>> foundComment = null;
        HashMap<String,HashMap<String,String>> expectedComment = null;
        HashMap<String,String> oneComment = null;

        tester.add("/**");
        tester.add(" * Given an object displays a link to show information about the editing context of that object.*<br />");
        tester.add(" *");
        tester.add(" * @binding object");
        tester.add(" * /");
        tester.add("");

        expectedComment = new HashMap<String,HashMap<String,String>>();

        oneComment = new HashMap<String,String>();
        oneComment.put("name", "object");
        oneComment.put("comment", "");
        oneComment.put("order", "000");
        expectedComment.put(oneComment.get("name"), oneComment);

        foundComment = PageGenerator.findBindingComments(tester);

        result += checkTest("test1", tester, expectedComment, foundComment);
*/
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
