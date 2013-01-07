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
public class ComponentDoclet extends com.sun.javadoc.Doclet {
    private static ArrayList<String> srcDirs;
    private static ArrayList<String> compDirs;

    private static HashMap<String,HashMap<String,Object>> comps;

    private static String[] prefixes = new String[] { "Selenium", "ERDAjax", "PayPal", "ERD2W", "ERDIV", "ERIUI", "ERNEU",
                                              "ERPDF", "ERXJS", "Ajax", "SEEO", "UJAC", "D2W", "ERC", "ERD", "ERO", "ERP", "ERX",
                                              "GSV", "WOL", "YUI", "ER", "GC", "IM", "JS", "SC", "SE", "WO", "WR", "WX" };

    /**
     * Generate javadoc. This needs some explanations, comments and a bit of unwinding. It seems
     * over-complicated to me and I wrote it.
     *
     * @param root
     * @return <code>true</code> on success
     */
    @SuppressWarnings("unchecked")
    public static boolean start(RootDoc root) {
        // test();

        ClassDoc[] classes = root.classes();

        comps = PageGenerator.findSubClassesFromAvailable(classes, "com.webobjects.appserver.WOElement");
        //System.out.println("comps(01): "+comps);

        PageGenerator.findSourceFiles(comps, srcDirs);
        //System.out.println("comps(02): "+comps);

        PageGenerator.gatherAllComments(comps);
        //System.out.println("comps(03): "+comps);

        PageGenerator.findClassComments(comps);
        //System.out.println("comps(04): "+comps);

//        PageGenerator.findApiFiles(comps, compDirs);

        PageGenerator.findPackages(comps);

        PageGenerator.findClassDocURLs(comps);

        PageGenerator.findTagComments(comps, "@binding");
//
//        gatherBindingsFromApi(comps);
//
//        // Check the condition of the bindings documentation.
//        //
//        Iterator<String> keys = comps.keySet().iterator();
//        while (keys.hasNext()) {
//            String key = keys.next().toString();
//
//            HashMap<String,Object> map = comps.get(key);
//
//            if (!map.containsKey("apiBindings") && !map.containsKey("comments")) { comps.get(key).put("Ok", "YES"); }
//
//            if (map.containsKey("apiBindings") && !map.containsKey("comments")) { comps.get(key).put("Ok", "NO"); }
//            if (!map.containsKey("apiBindings") && map.containsKey("comments")) { comps.get(key).put("Ok", "NO"); }
//
//            if (map.containsKey("apiBindings") && map.containsKey("comments")) {
//
//                ArrayList<String> apiBindings = (ArrayList<String>)comps.get(key).get("apiBindings");
//                java.util.Set jdBindings = ((HashMap<String,String>)comps.get(key).get("comments")).keySet();
//
//                if (apiBindings == null && jdBindings == null) comps.get(key).put("Ok", "YES");
//
//                if (apiBindings == null && jdBindings != null) comps.get(key).put("Ok", "NO");
//                if (apiBindings != null && jdBindings == null) comps.get(key).put("Ok", "NO");
//
//                if (apiBindings != null && jdBindings != null) {
//
//                    if (apiBindings.size() == 0 && jdBindings.size() == 0) { comps.get(key).put("Ok", "YES"); }
//
//                    if (apiBindings.size() != jdBindings.size()) { comps.get(key).put("Ok", "NO"); }
//
//                    if (apiBindings.size() != 0 && apiBindings.size() == jdBindings.size()) {
//                        java.util.HashSet apiSet = new java.util.HashSet(apiBindings);
//                        comps.get(key).put("Ok", (jdBindings.equals(apiSet)) ? "YES" : "NO");
//                    }
//                }
//            }
//        }
//
        TreeMap<String,ArrayList<String>> packageInfo = new TreeMap<String,ArrayList<String>>();

        Iterator<String> keys = comps.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next().toString();

            ArrayList<String> aPackage = packageInfo.get(PageGenerator.packageNameFromClassName(key));
            if (aPackage == null) {
                packageInfo.put(PageGenerator.packageNameFromClassName(key), new ArrayList<String>());
                aPackage = packageInfo.get(PageGenerator.packageNameFromClassName(key));
            }
            aPackage.add(key);
        }

        // System.out.println("packageInfo: "+packageInfo);

        FileWriter out = null;
        FileWriter check = null;
 
        try {
            out = new FileWriter("components.html");
            check = new FileWriter("components.txt");

            PageGenerator.writeFileToWriter(System.getProperty("build.root")+"/Build/build/components-pageHead.html", out);

            out.write("<ul>\n");
            //out.write("<li><a href=\"#ListedByPrefix\">Prefixes</a></li>\n");
            out.write("<li><a href=\"#ListedByPackage\">Packages/Classnames</a></li>\n");
            out.write("<li><a href=\"#ComponentDetails\">Component Details</a></li>\n");
            out.write("</ul>\n");

//            Iterator<String> prefxs = classNamePrefixes.keySet().iterator();
//
//            out.write("<a name=\"ListedByPrefix\"/>\n");

//            while (prefxs.hasNext()) {
//                String prefix = prefxs.next();
//
//                out.write(PageGenerator.TABLE_TOP_START);
//                out.write("<b>Prefix: "+prefix+"</b></font></th>\n");
//                out.write(PageGenerator.TABLE_TOP_END);
//
//                StringBuffer str = new StringBuffer();
//                Iterator<String> namesForPrefix = classNamePrefixes.get(prefix).iterator();
//                while (namesForPrefix.hasNext()) {
//                    String current = namesForPrefix.next();
//                    String[] parts = current.split("\\.");
//                    String lastName = parts[parts.length-1];
//
//                    str.append("<a href=\"#"+current+"\">"+lastName+"</a>, ");
//                }
//
//                String str2 = str.toString().substring(0,str.length()-2);
//                out.write("<tr bgcolor=\"white\" CLASS=\"TableRowColor\">\n");
//
//                out.write("<td>"+str2+"</td></tr>\n");
//                out.write("</table>\n&nbsp;<p>\n");
//            }

            keys = packageInfo.keySet().iterator();

            out.write("<a name=\"ListedByPackage\"/>\n");

            while (keys.hasNext()) {
                String key = (String)keys.next();

                // System.out.println("writing for key: "+key);

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

            out.write("<a name=\"ComponentDetails\"/>\n<h1>Component Details</h1>\n");

            keys = packageInfo.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String)keys.next();

                out.write("<a name=\"packagename\"><!-- --></A>\n");

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

                    if (!PageGenerator.packageNameFromClassName(compKey).equals("NONE")) {
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
                    }

                    HashMap<String,HashMap<String,String>> commentsMap = (HashMap<String,HashMap<String,String>>)map.get("comments");

                    /* In the block below, I write out the bindings from the commentsSet first, and then the bindings from the api set.
                       Therefore I only need to worry about the sort order in the commentsSet. -rrk */

                    Set<String> commentsSet = commentsMap.keySet();

                    //ArrayList<String> apiBindings = (ArrayList<String>)map.get("apiBindings");
                    ArrayList<String> apiBindings = new ArrayList<String>();

                    if (apiBindings.size() == 0 && commentsSet.size() == 0)
                        out.write("<i>No bindings for Component.</i>\n");
                    else {
                        out.write("<table border=\"1\" width=\"100%\" cellpadding=\"3\" cellspacing=\"0\" summary=\"\">\n");
                        out.write("<tr bgcolor=\"#CCCCFF\" class=\"TableHeadingColor\"><td colspan=2><font size=\"+2\"><b>Bindings</b></font></td></tr>\n");

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

                        Iterator<String> bindings = apiBindings.iterator();

                        while (bindings.hasNext()) {
                            String binding = bindings.next();
    
                            if (!commentsSet.contains(binding)) {
                                out.write("<td align=\"right\" valign=\"top\" width=\"1%\"><font size=\"-1\"><code>"+binding+"</code></font></td>\n");
                                out.write("<td><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>\n");

                                bindingCheck.append("          Binding: \"" + binding + "\": api file entry but no binding tag\n");
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
                    if (bindingCheck.length() > 0)
                        check.write(bindingCheck.toString());
                    check.write("\n");

                    out.write("<hr/>\n\n");
                }
            }

            PageGenerator.writeFileToWriter(System.getProperty("build.root")+"/Build/build/components-pageTail.html", out);

            out.close();

        } catch (java.io.IOException ioe) {
            System.err.println("Error writing to /tmp/foo.html");
            System.exit(1);
        }

        return false;
    }
 
    static void gatherBindingsFromApi(HashMap<String,HashMap<String,Object>> comps) {
        // Gather the bindings for the component from the api file.
        //
        Iterator<String> keys = comps.keySet().iterator();
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
    }

    static void test() {
//        int result = 0;
//
//        ArrayList<String> tester = new ArrayList<String>();
//        HashMap<String,HashMap<String,String>> foundComment = null;
//        HashMap<String,HashMap<String,String>> expectedComment = null;
//        HashMap<String,String> oneComment = null;
//
//        tester.add("/**");
//        tester.add(" * Given an object displays a link to show information about the editing context of that object.*<br />");
//        tester.add(" *");
//        tester.add(" * @binding object");
//        tester.add(" */");
//        tester.add("");
//
//        expectedComment = new HashMap<String,HashMap<String,String>>();
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "object");
//        oneComment.put("comment", "");
//        oneComment.put("order", "000");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        //foundComment = PageGenerator.findTagComments(tester, "@binding");
//
//        //result += checkTest("test1", tester, expectedComment, foundComment);
//
///* XXX - If I put a * at the start of a line, or multiple *'s, does it break things? */
//
//        tester = new ArrayList<String>();
//        tester.add("/**");
//        tester.add("  * AjaxSocialNetworkLink creates a link to the submission URL for ");
//        tester.add("  * a social network around the social network's icon.");
//        tester.add("  * ");
//        tester.add("  * @author mschrag");
//        tester.add("@binding name the name of the social network (@see er.ajax.AjaxSocialNetwork.socialNetworkNamed)");
//        tester.add("  * @binding url the URL to submit");
//        tester.add("  * @binding url2 the  ");
//        tester.add("  *    some exta stuff");
//        tester.add("");
//        tester.add("  * @binding title the title to submit");
//        tester.add("     ");
//        tester.add("  * @binding alt the alt tag (defaults to the name of the network)");
//        tester.add("  * @binding extra");
//        tester.add(" * @binding action, optional action to call before opening the modal dialog.");
//        tester.add("  * @binding target the target of the link");
//        tester.add("  */");
//
//        expectedComment = new HashMap<String,HashMap<String,String>>();
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "url");
//        oneComment.put("comment", "the URL to submit");
//        oneComment.put("order", "001");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "url2");
//        oneComment.put("comment", "the some exta stuff");
//        oneComment.put("order", "002");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "title");
//        oneComment.put("comment", "the title to submit");
//        oneComment.put("order", "003");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "alt");
//        oneComment.put("comment", "the alt tag (defaults to the name of the network)");
//        oneComment.put("order", "004");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "extra");
//        oneComment.put("comment", "");
//        oneComment.put("order", "005");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "target");
//        oneComment.put("comment", "the target of the link");
//        oneComment.put("order", "007");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "name");
//        oneComment.put("comment", "the name of the social network (@see er.ajax.AjaxSocialNetwork.socialNetworkNamed)");
//        oneComment.put("order", "000");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        oneComment = new HashMap<String,String>();
//        oneComment.put("name", "action");
//        oneComment.put("comment", "optional action to call before opening the modal dialog.");
//        oneComment.put("order", "006");
//        expectedComment.put(oneComment.get("name"), oneComment);
//
//        //foundComment = PageGenerator.findTagComments(tester, "@binding");
//
//        //result += checkTest("test2", tester, expectedComment, foundComment);
//
//        String expected = "AjaxSocialNetworkLink creates a link to the submission URL for a social network around the social network's icon.";
//        String found = PageGenerator.findClassComment(tester);
//
//        result += checkTest("test3", tester, expected, found);
//
//        tester = new ArrayList<String>();
//        tester.add("/**");
//        tester.add("* Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>\".");
//        tester.add("* <br/>");
//        tester.add("* Synopsis:<br/>");
//        tester.add("* email=<i>anEmail</i>;");
//        tester.add("* <br/>");
//        tester.add("* Bindings:<br/>");
//        tester.add("* <b>email</b> email to generate href");
//        tester.add("* <br/>");
//        tester.add("*/");
//
//        expected = "Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>\".";
//        found = PageGenerator.findClassComment(tester);
//
//        result += checkTest("test4", tester, expected, found);
//
//        tester = new ArrayList<String>();
//        tester.add("/**");
//        tester.add("* Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>.\"");
//        tester.add("* <br/>");
//        tester.add("*/");
//
//        expected = "Component that generates a mailto href of the form: \"<a href=mailto:foo@bar.com>foo@bar.com</a>.\"";
//        found = PageGenerator.findClassComment(tester);
//
//        result += checkTest("test5", tester, expected, found);
//
//        tester = new ArrayList<String>();
//        tester.add("/**");
//        tester.add(" * @binding id the id of the update container");
//        tester.add(" * @binding progressID the id of the AjaxProgress");
//
//        expected = "";
//        found = PageGenerator.findClassComment(tester);
//
//        result += checkTest("test6", tester, expected, found);
//
//        tester = new ArrayList<String>();
//        tester.add("/** I can check here also");
//        tester.add(" * @binding id the id of the update container");
//        tester.add(" * @binding progressID the id of the AjaxProgress");
//
//        expected = "I can check here also";
//        found = PageGenerator.findClassComment(tester);
//
//        result += checkTest("test7", tester, expected, found);
//
//        tester = new ArrayList<String>();
//        tester.add("/**");
//        tester.add(" * XHTML version of WORadioButtonList");
//        tester.add(" *");
//        tester.add(" * @see WORadioButtonList");
//        tester.add(" * @author mendis");
//        tester.add(" *");
//        tester.add(" */");
//
//        expected = "XHTML version of WORadioButtonList";
//        found = PageGenerator.findClassComment(tester);
//
//        result += checkTest("test8", tester, expected, found);
//
//        if (result > 0) { System.exit(1); }
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
            out.write("<title>Components with D2W Keys</title>\n");
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
            out.write("<em>Project Wonder 6.0</em></EM>\n");
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
