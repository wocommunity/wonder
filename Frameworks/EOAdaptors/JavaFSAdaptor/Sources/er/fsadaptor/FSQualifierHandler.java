package er.fsadaptor;

import java.io.File;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

public abstract class FSQualifierHandler {
    public static boolean debug = "YES".equals(System.getProperty("FSAdaptorDebuggingEnabled"));

    private static final String[] HandlerNames = { "er.fsadaptor.FSQualifierHandler$And", "er.fsadaptor.FSQualifierHandler$Or", "er.fsadaptor.FSQualifierHandler$Not",
            "er.fsadaptor.FSQualifierHandler$KeyValue" };

    private static FSQualifierHandler[] _handlers = null;

    private static String rootDirectory = "";

    public static class FileProxy extends java.io.File {
        public FileProxy(String path) {
            super(path);
        }

        @Override
        public String getParent() {
            return getRealParent().substring(rootDirectory.length());
        }

        public String getRealParent() {
            return super.getParent();
        }
    }

    private static File fileWithPath(String path) {
        if (rootDirectory != null && rootDirectory.length() > 0)
            path = rootDirectory + File.separator + path;
        if (debug)
            System.out.println("FSQualifierHandler.fileWithPath: " + path);
        return new File(path);
    }

    private static final class KeyValue extends FSQualifierHandler {
        private static final String[] PathKeys = { "absolutePath", "canonicalPath", "path" };

        private static final int PathKeysCount = PathKeys.length;

        private static final String ParentPathKey = "parent";

        protected KeyValue() {
            /* empty */
        }

        @Override
        protected Class type() {
            return EOKeyValueQualifier.class;
        }

        private void addFileWithQualifierToSet(EOKeyValueQualifier aQualifier, NSMutableSet<File> aSet) {
            if (aQualifier != null) {
                if (aSet != null) {
                    String aKey = aQualifier.key();
                    for (int index = 0; index < PathKeysCount; index++) {
                        if (aKey.equals(PathKeys[index])) {
                            String aPath = aQualifier.value().toString();
                            File aFile = fileWithPath(aPath);
                            if (aFile.exists())
                                aSet.addObject(aFile);
                            if (debug)
                                System.out.println("EOKeyValueQualifier.addFileWithQualifierToSet: " + aFile);
                            break;
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("FSQualifierHandler.KeyValue.addFileWithQualifierToSet: null set.");
            }
            throw new IllegalArgumentException("FSQualifierHandler.KeyValue.addFileWithQualifierToSet: null qualifier.");
        }

        private void addParentFilesWithQualifierToSet(EOKeyValueQualifier aQualifier, NSMutableSet<File> aSet) {
            if (aQualifier != null) {
                if (aSet != null) {
                    String aKey = aQualifier.key();
                    if (aKey.equals(ParentPathKey)) {
                        String aPath = aQualifier.value().toString();
                        File aFile = fileWithPath(aPath);
                        if (aFile.exists() && aFile.isDirectory()) {
                            File[] someFiles = aFile.listFiles();
                            if (someFiles != null && someFiles.length > 0) {
                                NSArray<File> files = new NSArray<File>(someFiles);
                                aSet.addObjectsFromArray(files);
                                if (debug)
                                    System.out.println("EOKeyValueQualifier.addParentFilesWithQualifierToSet: " + files);
                            }
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("FSQualifierHandler.KeyValue.addParentFilesWithQualifierToSet: null set.");
            }
            throw new IllegalArgumentException("FSQualifierHandler.KeyValue.addParentFilesWithQualifierToSet: null qualifier.");
        }

        @Override
        protected void addFilesMatchingQualifierToSet(EOQualifier aQualifier, NSMutableSet<File> aSet) {
            if (aQualifier != null) {
                if (aSet != null) {
                    addFileWithQualifierToSet((EOKeyValueQualifier) aQualifier, aSet);
                    addParentFilesWithQualifierToSet(((EOKeyValueQualifier) aQualifier), aSet);
                    return;
                }
                throw new IllegalArgumentException("FSQualifierHandler.KeyValue.addFilesMatchingQualifierToSet: null set.");
            }
            throw new IllegalArgumentException("FSQualifierHandler.KeyValue.addFilesMatchingQualifierToSet: null qualifier.");
        }
    }

    private static final class Not extends FSQualifierHandler {
        protected Not() {
            /* empty */
        }

        @Override
        protected Class type() {
            return EONotQualifier.class;
        }

        @Override
        protected void addFilesMatchingQualifierToSet(EOQualifier aQualifier, NSMutableSet<File> aSet) {
            if (aQualifier != null) {
                if (aSet != null) {
                    FSQualifierHandler.addFilesWithQualifierToSet(((EONotQualifier) aQualifier).qualifier(), aSet);
                    return;
                }
                throw new IllegalArgumentException("FSQualifierHandler.Not.addFilesMatchingQualifierToSet: null set.");
            }
            throw new IllegalArgumentException("FSQualifierHandler.Not.addFilesMatchingQualifierToSet: null qualifier.");
        }
    }

    private static final class Or extends FSQualifierHandler {
        protected Or() {
            /* empty */
        }

        @Override
        protected Class type() {
            return EOOrQualifier.class;
        }

        @Override
        protected void addFilesMatchingQualifierToSet(EOQualifier aQualifier, NSMutableSet<File> aSet) {
            if (aQualifier != null) {
                if (aSet != null) {
                    NSArray<EOQualifier> someQualifiers = ((EOOrQualifier) aQualifier).qualifiers();
                    if (someQualifiers != null) {
                        int count = someQualifiers.count();
                        for (int index = 0; index < count; index++) {
                            EOQualifier anotherQualifier = someQualifiers.objectAtIndex(index);
                            FSQualifierHandler.addFilesWithQualifierToSet(anotherQualifier, aSet);
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("FSQualifierHandler.Or.addFilesMatchingQualifierToSet: null set.");
            }
            throw new IllegalArgumentException("FSQualifierHandler.Or.addFilesMatchingQualifierToSet: null qualifier.");
        }
    }

    private static final class And extends FSQualifierHandler {
        protected And() {
            /* empty */
        }

        @Override
        protected Class type() {
            return EOAndQualifier.class;
        }

        @Override
        protected void addFilesMatchingQualifierToSet(EOQualifier aQualifier, NSMutableSet<File> aSet) {
            if (aQualifier != null) {
                if (aSet != null) {
                    NSArray<EOQualifier> someQualifiers = ((EOAndQualifier) aQualifier).qualifiers();
                    if (someQualifiers != null) {
                        int count = someQualifiers.count();
                        for (int index = 0; index < count; index++) {
                            EOQualifier anotherQualifier = someQualifiers.objectAtIndex(index);
                            FSQualifierHandler.addFilesWithQualifierToSet(anotherQualifier, aSet);
                        }
                    }
                    return;
                }
                throw new IllegalArgumentException("FSQualifierHandler.And.addFilesMatchingQualifierToSet: null set.");
            }
            throw new IllegalArgumentException("FSQualifierHandler.And.addFilesMatchingQualifierToSet: null qualifier.");
        }
    }

    protected FSQualifierHandler() {
        /* empty */
    }

    private static FSQualifierHandler[] handlers() {
        return _handlers;
    }

    static NSArray<File> filesWithQualifier(EOQualifier aQualifier, String root) {
        if (aQualifier != null) {
            rootDirectory = root;
            if (debug)
                System.out.println("FSQualifierHandler.rootDirectory: " + rootDirectory);
            NSMutableSet<File> aSet = new NSMutableSet<File>();
            addFilesWithQualifierToSet(aQualifier, aSet);
            if (aSet.count() > 0) {
                NSArray<File> anArray = EOQualifier.filteredArrayWithQualifier(aSet.allObjects(), aQualifier);
                if (anArray != null && anArray.count() > 0) {
                    if (debug)
                        System.out.println("FSQualifierHandler.filesWithQualifier: " + anArray);
                    return anArray;
                }
            }
            return null;
        }
        if (root != null) {
            File aFile = fileWithPath("");
            if (aFile.exists() && aFile.isDirectory()) {
                return new NSArray<File>(aFile.listFiles());
            }
            return null;
        }
        throw new IllegalArgumentException("FSQualifierHandler.filesWithQualifier: null qualifier.");
    }

    private static void addFilesWithQualifierToSet(EOQualifier aQualifier, NSMutableSet<File> aSet) {
        if (aQualifier != null) {
            if (aSet != null) {
                FSQualifierHandler[] someHandlers = handlers();
                int count = someHandlers.length;
                for (int index = 0; index < count; index++) {
                    FSQualifierHandler anHandler = someHandlers[index];
                    if (anHandler.canHandleQualifier(aQualifier)) {
                        anHandler.addFilesMatchingQualifierToSet(aQualifier, aSet);
                        break;
                    }
                }
                return;
            }
            throw new IllegalArgumentException("FSQualifierHandler.addFilesWithQualifierToSet: null set.");
        }
        throw new IllegalArgumentException("FSQualifierHandler.addFilesWithQualifierToSet: null qualifier.");
    }

    boolean canHandleQualifier(EOQualifier aQualifier) {
        if (aQualifier != null) {
            Class aType = type();
            if (aType != null)
                return aType.isAssignableFrom(aQualifier.getClass());
            throw new IllegalStateException("FSQualifierHandler.canHandleQualifier: null type.");
        }
        throw new IllegalArgumentException("FSQualifierHandler.canHandleQualifier: null qualifier.");
    }

    protected abstract Class type();

    protected abstract void addFilesMatchingQualifierToSet(EOQualifier eoqualifier, NSMutableSet<File> nsmutableset);

    static {
        String[] someHandlerNames = HandlerNames;
        int count = someHandlerNames.length;
        _handlers = new FSQualifierHandler[count];
        for (int index = 0; index < count; index++) {
            String aClassName = someHandlerNames[index];
            try {
                Class aClass = Class.forName(aClassName);
                FSQualifierHandler anHandler = (FSQualifierHandler) aClass.newInstance();
                _handlers[index] = anHandler;
            } catch (Exception anException) {
                throw new RuntimeException("SZDocumentDecoder.handlers: " + anException);
            }
        }
    }
}
