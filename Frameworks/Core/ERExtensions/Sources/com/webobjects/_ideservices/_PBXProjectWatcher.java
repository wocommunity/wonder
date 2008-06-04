package com.webobjects._ideservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation._NSStringUtilities;

/**
 * Our own implementation for the _PBXProjectWatcher. When
 * put in the classpath before the Apple implementation, it
 * checks for project files, parses them and read the resources from there.
 * This is orders of magnitudes faster than sending the XML over a socket 
 * over to XCode. And works on Windows, too.
 *
 * @author ak
 */
public class _PBXProjectWatcher {

    private static Logger log = Logger.getLogger(_PBXProjectWatcher.class);

    private static Logger fileLog = Logger.getLogger(_PBXProjectWatcher.class.getName() + ".Files");

    private static NSMutableArray _nsProjectSearchPaths;

    private static NSMutableDictionary _pbxArchivesByPath = new NSMutableDictionary();

    private static NSMutableDictionary _pathsByName = new NSMutableDictionary();

    private static int _PBPort;

    private static String _PBHostname;

    private static volatile boolean _printRapidTurnaroundMessage = true;

    private static boolean _communicationDisabled = false;

    static {
        System.out.println("Installed NSProjectSearchPath fix");
    }

    /**
     * Holds the parsed project data.
     * 
     *
     * @author ak
     */
    private static class PBXArchive {

        private static Logger log = Logger.getLogger(PBXArchive.class);

        private NSArray _targets;

        private NSDictionary _objects;

        private String _path;

        private NSMutableDictionary _filesPerTarget;

        public PBXArchive(String path) throws IOException {
            _filesPerTarget = new NSMutableDictionary();
            log.debug("Loading: " + path);
            String contents = stringWithContentsOfFile(new File(path, "project.pbxproj"));
            try {
                _objects = (NSDictionary) NSPropertyListSerialization.propertyListFromString(contents);
                _objects = (NSDictionary) _objects.objectForKey("objects");
                NSDictionary project = objectOfTypeInPBXArchive("PBXProject", _objects);
                _targets = (NSArray) project.objectForKey("targets");
                _path = NSPathUtilities.stringByDeletingLastPathComponent(path);
            } catch (Exception ex) {
                throw NSForwardException._runtimeExceptionForThrowable(ex);
            }
        }

        public NSArray objectsOfTypeInPBXArchive(String type, NSDictionary objects) {
            NSMutableArray result = new NSMutableArray();
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                NSDictionary o = (NSDictionary) e.nextElement();
                String isA = (String) o.objectForKey("isa");
                if (safeEquals(type, isA)) {
                    result.addObject(o);
                }
            }

            return result;
        }

        public NSDictionary objectOfTypeInPBXArchive(String type, NSDictionary objects) {
            NSDictionary result = null;
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();) {
                NSDictionary o = (NSDictionary) e.nextElement();
                String isA = (String) o.objectForKey("isa");
                if (safeEquals(type, isA)) {
                    result = o;
                    break;
                }
            }

            return result;
        }

        public NSArray targetIDs() {
            return _targets;
        }

        public NSDictionary objectWithID(String ID) {
            NSDictionary result = (NSDictionary) _objects.objectForKey(ID);
            return result;
        }

        public String nameForTargetWithID(String ID) {
            NSDictionary target = (NSDictionary) _objects.objectForKey(ID);
            return (String) (target == null ? null : target.objectForKey("name"));
        }

        public void addPathsForChildrenWithIDToArray(NSArray IDs, NSMutableArray array) {
            if (IDs != null) {
                for (Enumeration e = IDs.objectEnumerator(); e.hasMoreElements();) {
                    NSDictionary child = objectWithID((String) e.nextElement());
                    if (child != null) {
                        String path = (String) child.objectForKey("path");
                        if (path != null && path.length() > 0)
                            array.addObject(path);
                        addPathsForChildrenWithIDToArray((NSArray) child.objectForKey("children"), array);
                    }
                }

            }
        }

        public NSArray filesInTargetWithID(String targetID) {
            NSMutableArray result = (NSMutableArray) _filesPerTarget.objectForKey(targetID);
            if (result == null) {
                NSDictionary target = (NSDictionary) _objects.objectForKey(targetID);
                if (target != null) {
                    result = new NSMutableArray();
                    NSArray buildPhasesID = (NSArray) target.objectForKey("buildPhases");
                    if (buildPhasesID != null) {
                        for (Enumeration e = buildPhasesID.objectEnumerator(); e.hasMoreElements();) {
                            NSDictionary buildPhase = objectWithID((String) e.nextElement());
                            NSArray files = (NSArray) buildPhase.objectForKey("files");
                            if (files != null) {
                                NSArray ids;
                                for (Enumeration e2 = files.objectEnumerator(); e2.hasMoreElements(); addPathsForChildrenWithIDToArray(
                                        ids, result)) {
                                    NSDictionary buildFile = objectWithID((String) e2.nextElement());
                                    ids = new NSArray((String) buildFile.objectForKey("fileRef"));
                                }

                            }
                        }

                    }
                }
                _filesPerTarget.setObjectForKey(result, targetID);
                if (log.isDebugEnabled())
                    log.debug("filesInTargetWithID " + targetID + "=" + result);
            }
            return result;
        }

        public NSArray filesOfTypeInTargetWithID(NSArray types, String targetID) {
            NSMutableArray result = new NSMutableArray();
            for (Enumeration e = filesInTargetWithID(targetID).objectEnumerator(); e.hasMoreElements();) {
                String file = (String) e.nextElement();
                int lastDotIndex = file.lastIndexOf('.');
                if (lastDotIndex != -1 && file.length() >= lastDotIndex
                        && types.containsObject(file.substring(lastDotIndex + 1)))
                    result.addObject(NSPathUtilities.stringByAppendingPathComponent(_path, file));
            }

            return result;
        }
    }

    /**
     * Returns the NSProjectSearchPath as Files
     * @return
     */
    private static NSArray nsProjectSearchPaths() {
        if (_nsProjectSearchPaths == null) {
            String searchPathsAsString = System.getProperty("NSProjectSearchPath");
            NSArray searchPaths = (NSArray) NSPropertyListSerialization.propertyListFromString(searchPathsAsString);
            _nsProjectSearchPaths = new NSMutableArray();
            if (searchPaths != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found NSProjectSearchPath: " + searchPaths);
                }
                for (Enumeration e = searchPaths.objectEnumerator(); e.hasMoreElements(); ) {
                    _nsProjectSearchPaths.addObject(new File((String) e.nextElement()));
                }
            }
        }
        return _nsProjectSearchPaths;
    }

    /**
     * Returns true if the NSProjectSearchPath is not empty.
     * @return
     */
    private static boolean hasSearchPaths() {
        return nsProjectSearchPaths().count() > 0;
    }

    /**
     * Returns a PBXArchive for the given path.
     * @param path
     * @return
     * @throws IOException
     */
    private static final PBXArchive pbxArchiveAtPath(String path) throws IOException {
        PBXArchive result = (PBXArchive) _pbxArchivesByPath.objectForKey(path);
        if (result == null) {
            result = new PBXArchive(path);
            _pbxArchivesByPath.setObjectForKey(result, path);
        }
        return result;
    }

    /**
     * Returns the path for the project with the given name.
     * @param name
     * @return
     */
    private static final NSArray pathsForProjectNamed(final String name) {
        NSArray result = (NSArray) _pathsByName.objectForKey(name);
        if (result == null) {
            result = NSArray.EmptyArray;
            Enumeration e = nsProjectSearchPaths().objectEnumerator();
            while (e.hasMoreElements()) {
                File path = (File) e.nextElement();
                if (!path.exists() || !path.isDirectory())
                    continue;
                File files[] = path.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String otherName) {
                        return safeEquals(otherName, name);
                    }
                });
                if (files.length <= 0)
                    continue;
                File parent = files[0];
                File project = new File(parent.getPath() + File.separator + name + ".xcodeproj");
                if (!project.exists()) {
                    project = new File(parent.getPath() + File.separator + name + ".pbproj");
                }
                if(project.exists()) {
                    result = new NSArray(project.getPath());
                    break;
               }
             }
            if (result.count() > 0) {
                log.info("Found " + name + " in " + result);
                _pathsByName.setObjectForKey(result, name);
            }
        }
        return result;
    }

    public static NSArray openProjectsAppropriateForFile(String path) {
        NSArray result = NSArray.EmptyArray;
        if (!hasSearchPaths()) {
            StringBuffer sb = new StringBuffer(4096);
            sb.append("<openProjectsAppropriateForFile>");
            sb.append("<path>" + path + "</path>");
            sb.append("</openProjectsAppropriateForFile>");
            String reply = _sendXMLToPB(new String(sb));
            if (reply.length() > 0) {
                result = (NSArray) com.webobjects.foundation.NSPropertyListSerialization.propertyListFromString(reply);
                if (result.count() == 0) {
                    String last = _NSStringUtilities.lastComponentInString(path, '/');
                    if (last.indexOf(".woa") > 0 || last.indexOf(".framework") > 0) {
                        String what = _NSStringUtilities.stringByDeletingLastComponent(last, '.');
                        String key = "projects." + what;
                        String source = System.getProperty(key);
                        if (source != null) {
                            result = new NSArray(source);
                        }
                    }
                }
            }
        } else {
            try {
                String last = _NSStringUtilities.lastComponentInString(path, '/');
                if (last.indexOf(".woa") > 0 || last.indexOf(".framework") > 0) {
                    String what = _NSStringUtilities.stringByDeletingLastComponent(last, '.');
                    result = pathsForProjectNamed(what);
                    if (fileLog.isDebugEnabled())
                        fileLog.debug("openProjectsAppropriateForFile " + path + ": " + result);
                }
            } catch (Throwable e) {
                log.error("While trying to compute openProjectsAppropriateForFile for " + path + ", caught " + e + "\n\n", e);
            }
        }
        return result;
    }

    public static NSArray targetsInProject(String path) {
        NSArray result = null;
        if (!hasSearchPaths()) {
            StringBuffer sb = new StringBuffer(4096);
            sb.append("<targetsInProject>");
            sb.append("<cookie>" + path + "</cookie>");
            sb.append("</targetsInProject>");
            String reply = _sendXMLToPB(sb.toString());
            if (reply.length() > 16 && reply.lastIndexOf("</string>") > 0) {
                reply = reply.substring(16, reply.lastIndexOf("</string>"));
                result = NSArray.componentsSeparatedByString(reply, "</string>\n<string>");
            } else {
                result = new NSArray();
            }
        } else {
            try {
                PBXArchive p = pbxArchiveAtPath(path);
                result = p == null ? null : p.targetIDs();
            } catch (Throwable e) {
                log.error(e, e);
            }
        }
        return result;
    }

    public static NSArray filesOfTypesInTargetOfProject(NSArray types, String target, String path) {
        NSArray result = null;
        if (!hasSearchPaths()) {
            StringBuffer sb = new StringBuffer(4096);
            sb.append("<filesOfTypesInTargetOfProject>");
            sb.append("<cookie>" + path + "</cookie>");
            sb.append("<target>" + target + "</target>");
            sb.append("<typesArray>" + _xmlStringArray(types) + "</typesArray>");
            sb.append("</filesOfTypesInTargetOfProject>");
            String reply = _sendXMLToPB(sb.toString());
            if (reply.length() > 0)
                result = (NSArray) com.webobjects.foundation.NSPropertyListSerialization.propertyListFromString(reply);
            else
                result = NSArray.EmptyArray;
        } else {
            try {
                PBXArchive p = pbxArchiveAtPath(path);
                result = p == null ? null : p.filesOfTypeInTargetWithID(types, target);
                if (fileLog.isDebugEnabled() && result.toString().indexOf("d2w") > 0) {
                    fileLog.debug("filesOfTypesInTargetOfProject " + types + ", " + target + ", " + path);
                    fileLog.debug("=" + result);
                }
            } catch (Throwable e) {
                log.error(e, e);
            }
        }
        return result;
    }

    public static String nameOfTargetInProject(String targetID, String path) {
        String result = null;
        if (!hasSearchPaths()) {
            StringBuffer sb = new StringBuffer(4096);
            sb.append("<nameOfTarget>");
            sb.append("<targetCookie>" + targetID + "</targetCookie >");
            sb.append("<projectCookie>" + path + "</projectCookie >");
            sb.append("</nameOfTarget>");
            result = _sendXMLToPB(new String(sb));
        } else {
            try {
                PBXArchive p = pbxArchiveAtPath(path);
                if (p != null)
                    result = p.nameForTargetWithID(targetID);
            } catch (Throwable e) {
                log.error(e, e);
            }
        }
        return result;
    }

    public static NSArray targetsInProjectContainingFile(String project, String file) {
        StringBuffer sb = new StringBuffer(4096);
        sb.append("<targetsInProjectContainingFile>");
        sb.append("<cookie>" + project + "</cookie>");
        sb.append("<path>" + file + "</path>");
        sb.append("</targetsInProjectContainingFile>");
        String reply = _sendXMLToPB(new String(sb));
        NSArray result;
        if (reply.length() > 0)
            result = (NSArray) com.webobjects.foundation.NSPropertyListSerialization.propertyListFromString(reply);
        else
            result = new NSArray();
        return result;
    }

    public static String nameOfProject(String project) {
        if (!hasSearchPaths()) {
            StringBuffer sb = new StringBuffer(4096);
            sb.append("<nameOfProject>");
            sb.append("<projectCookie>" + project + "</projectCookie>");
            sb.append("</nameOfProject>");
            String reply = _sendXMLToPB(new String(sb));
            return reply;
        } else {
            String name = NSPathUtilities.lastPathComponent(project);
            int lastDotIndex = name.lastIndexOf('.');
            return name.substring(0, lastDotIndex);
        }
    }

    public static void addFilesToProjectNearFilePreferredInsertionGroupNameAddToTargetsCopyIntoGroupFolderCreateGroupsRecursively(
            NSArray files, String project, String file, String groupName, NSArray targets, boolean copyIntoGroupFolder,
            boolean createGroupsRecursively) {
        StringBuffer sb = new StringBuffer(4096);
        sb.append("<addFilesToProject>");
        sb.append("<addFiles>" + _xmlStringArray(files) + "</addFiles>");
        sb.append("<toProject>" + project + "</toProject>");
        sb.append("<nearFile>" + file + "</nearFile>");
        sb.append("<preferredInsertionGroupName>" + groupName + "</preferredInsertionGroupName>");
        sb.append("<addToTargets>" + _xmlStringArray(targets) + "</addToTargets>");
        sb.append("<copyIntoGroupFolder>" + _xmlBoolean(copyIntoGroupFolder) + "</copyIntoGroupFolder>");
        sb.append("<createGroupsRecursively>" + _xmlBoolean(createGroupsRecursively) + "</createGroupsRecursively>");
        sb.append("</addFilesToProject>");
        String reply = _sendXMLToPB(new String(sb));
    }

    public static void openFile(String file, int line, String message) {
        StringBuffer sb = new StringBuffer(4096);
        sb.append("<OpenFile><filename>");
        sb.append(file);
        sb.append("</filename><linenumber>");
        sb.append(line);
        sb.append("</linenumber><message>");
        sb.append(message);
        sb.append("</message></OpenFile>");
        String reply = _sendXMLToPB(sb.toString());
    }

    public static void addGroup(String name, String path, String project, String nearFile) {
        StringBuffer sb = new StringBuffer(4096);
        sb.append("<addGroup>");
        sb.append("<name>" + name + "</name >");
        if (path != null)
            sb.append("<path>" + path + "</path >");
        sb.append("<projectCookie>" + project + "</projectCookie >");
        if (nearFile != null)
            sb.append("<nearFile>" + nearFile + "</nearFile >");
        sb.append("</addGroup>");
        String reply = _sendXMLToPB(sb.toString());
    }

    public static void addGroupToPreferredInsertionGroup(String group, String path, String project,
            String nearFile, String preferredInsertionGroup) {
        StringBuffer sb = new StringBuffer(4096);
        sb.append("<addGroupToPreferredInsertionGroup>");
        sb.append("<name>" + group + "</name >");
        if (path != null)
            sb.append("<path>" + path + "</path >");
        sb.append("<projectCookie>" + project + "</projectCookie >");
        if (nearFile != null)
            sb.append("<nearFile>" + nearFile + "</nearFile >");
        if (preferredInsertionGroup != null)
            sb.append("<preferredInsertionGroup>" + preferredInsertionGroup + "</preferredInsertionGroup >");
        sb.append("</addGroupToPreferredInsertionGroup>");
        String reply = _sendXMLToPB(sb.toString());
    }


    private static final String _xmlStringArray(NSArray nsarray) {
        StringBuffer sb = new StringBuffer(4096);
        sb.append("<array>");
        for (int i = 0; i < nsarray.count(); i++) {
            String string = (String) nsarray.objectAtIndex(i);
            sb.append("<string>" + string + "</string>");
        }

        sb.append("</array>");
        return sb.toString();
    }

    private static final String _xmlBoolean(boolean bool) {
        if (bool)
            return "YES";
        else
            return "NO";
    }

    private static final String _sendXMLToPB(String string) {
        String reply = "";
        if (_communicationDisabled)
            return "";
        try {
            Socket socket = new Socket(_PBHostname, _PBPort);
            if (socket == null)
                return "Cound not create socket to PB";
            OutputStream os = socket.getOutputStream();
            os.write(string.getBytes());
            os.flush();
            try {
                int buffSize = 7000;
                byte buffer[] = new byte[buffSize];
                InputStream is = socket.getInputStream();
                int tries = 0;
                int maxTries;
                for (maxTries = 50; is.available() == 0 && tries < maxTries; tries++) {
                    Thread.sleep(0L);
                }

                if (tries == maxTries) {
                    _communicationDisabled = true;
                    log.error("Couldn't contact ProjectBuilder to send XML command " + string, new RuntimeException());
                }
                while (is.available() > 0) {
                    int readBytes = is.read(buffer, 0, buffSize >= is.available() ? is.available() : buffSize);
                    reply += new String(buffer, 0, readBytes);
                }
            } catch (Exception ex) {
                _communicationDisabled = true;
                log.error("Error sending xml command to ProjectBuilder. XML: " + string, ex);
                reply = "";
            }
            socket.close();
        } catch (Exception ex) {
            if (System.getProperty("os.name").startsWith("Mac")) {
                if (_printRapidTurnaroundMessage) {
                    _printRapidTurnaroundMessage = false;
                    log.error("Cannot use rapid turnaround.  Please start Project Builder and open the project for this application.");
                }
            }
            reply = "";
        }
        return reply;
    }

    /**
     * A safe comparison method that first checks to see
     * if either of the objects are null before comparing
     * them with the <code>equals</code> method.<br/>
     * <br/>
     * Note that if both objects are null then they will
     * be considered equal.
     * @param v1 first object
     * @param v2 second object
     * @return true if they are equal, false if not
     */
    private static boolean safeEquals(Object v1, Object v2) {
        return v1==v2 || (v1!=null && v2!=null && v1.equals(v2));
    }
    
    /**
     * Returns the byte array for a given file.
     * @param f file to get the bytes from
     * @throws IOException if things go wrong
     * @return byte array of the file.
     */
    private static byte[] bytesFromFile(File f) throws IOException {
        if (f == null) throw new IllegalArgumentException("null file");
        return bytesFromFile(f, (int)f.length());
    }
    
    /**
     * Returns an array of the first n bytes for a given file.
     * @param f file to get the bytes from
     * @param n number of bytes to read from input file
     * @throws IOException if things go wrong
     * @return byte array of the first n bytes from the file.
     */
    private static byte[] bytesFromFile(File f, int n) throws IOException {
        if (f == null) throw new IllegalArgumentException("null file");
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[n];
        int bytesRead = 0;
        while (bytesRead < n)
            bytesRead += fis.read(data, bytesRead, n - bytesRead);
        fis.close();
        return data;
    }
    
    /**
     * Reads the contents of a file given by a path
     * into a string.
     * @param path to the file in the file system
     * @return the contents of the file in a string
     */
    private static String stringWithContentsOfFile(File file) {
        if(file != null) {
            try {
                return new String(bytesFromFile(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
