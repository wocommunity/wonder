
/* FSAdaptorChannel - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package er.fsadaptor;

import java.io.*;
import java.net.URL;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

public final class FSAdaptorChannel extends EOAdaptorChannel {
    public static boolean debug = "YES".equals(System.getProperty("FSAdaptorDebuggingEnabled"));

    private static final String DefaultModelName = "FS.eomodeld";

    private static final String[] TableNames = { "FSItem", "FSFile", "FSDirectory" };

    private boolean _isOpen = false;

    private NSArray _attributes = null;

    private final NSMutableArray _files = new NSMutableArray();

    public FSAdaptorChannel(EOAdaptorContext aContext) {
        super(aContext);
    }

    private NSMutableArray files() {
        return _files;
    }

    public NSArray attributesToFetch() {
        return _attributes;
    }

    public void cancelFetch() {
        files().removeAllObjects();
    }

    public void closeChannel() {
        _isOpen = false;
    }

    public int deleteRowsDescribedByQualifier(EOQualifier aQualifier, EOEntity anEntity) {
        if (aQualifier != null) {
            if (anEntity != null) {
                NSArray someFiles = FSQualifierHandler.filesWithQualifier(aQualifier, rootDirectory(anEntity));
                if (someFiles != null) {
                    someFiles = filteredArrayWithEntity(someFiles, anEntity);
                    if (someFiles != null) {
                        int count = someFiles.count();
                        int counter = 0;
                        for (int index = 0; index < count; index++) {
                            File aFile = (File) someFiles.objectAtIndex(index);
                            if (aFile.delete() == true)
                                counter++;
                        }
                        return counter;
                    }
                }
                return 0;
            }
            throw new IllegalArgumentException("FSAdaptorChannel.deleteRowsDescribedByQualifier: null entity.");
        }
        throw new IllegalArgumentException("FSAdaptorChannel.deleteRowsDescribedByQualifier: null qualifier.");
    }

    public NSArray describeResults() {
        return _attributes;
    }

    public void evaluateExpression(EOSQLExpression anExpression) {
        throw new UnsupportedOperationException("FSAdaptorChannel.evaluateExpression");
    }

    public void executeStoredProcedure(EOStoredProcedure aStoredProcedure, NSDictionary someValues) {
        throw new UnsupportedOperationException("FSAdaptorChannel.executeStoredProcedure");
    }

    public NSMutableDictionary fetchRow() {
        File aFile = (File) files().lastObject();
        if (aFile != null) {
            files().removeLastObject();
            return dictionaryForFileWithAttributes(aFile, attributesToFetch());
        }
        return null;
    }

    public void insertRow(NSDictionary aRow, EOEntity anEntity) {
        if (aRow != null) {
            if (anEntity != null) {
                String aPath = (String) aRow.objectForKey("absolutePath");
                if (aPath != null) {
                    File aFile = new File(aPath);
                    try {
                        if (anEntity.externalName().equals("FSDirectory") == true)
                            aFile.mkdirs();
                        else
                            aFile.createNewFile();
                    } catch (Exception anException) {
                        throw new RuntimeException("FSAdaptorChannel.insertRow: " + anException);
                    }
                    return;
                }
                throw new IllegalArgumentException("FSAdaptorChannel.insertRow: null absolutePath.");
            }
            throw new IllegalArgumentException("FSAdaptorChannel.insertRow: null entity.");
        }
        throw new IllegalArgumentException("FSAdaptorChannel.insertRow: null row.");
    }

    public boolean isFetchInProgress() {
        if (files().count() > 0)
            return true;
        return false;
    }

    public boolean isOpen() {
        return _isOpen;
    }

    public void openChannel() {
        _isOpen = true;
    }

    public NSDictionary returnValuesForLastStoredProcedureInvocation() {
        throw new UnsupportedOperationException("FSAdaptorChannel.returnValuesForLastStoredProcedureInvocation");
    }

    public String rootDirectory(EOEntity entity) {
        String root = (String) entity.model().connectionDictionary().objectForKey("rootDirectory");
        if (root == null)
            root = "";
        return root;
    }

    public void selectAttributes(NSArray someAttributes, EOFetchSpecification aFetchSpecification, boolean shouldLock, EOEntity anEntity) {
        if (anEntity == null)
            throw new IllegalArgumentException("FSAdaptorChannel.selectAttributes: null entity.");
        if (someAttributes == null)
            throw new IllegalArgumentException("FSAdaptorChannel.selectAttributes: null attributes.");

        setAttributesToFetch(someAttributes);

        EOQualifier qualifier = null;
        String entityName = anEntity.name();

        if (aFetchSpecification != null)
            qualifier = aFetchSpecification.qualifier();

        if (debug)
            System.out.println("*****selectAttributes: " + entityName + "--" + aFetchSpecification.entityName() + "--" + aFetchSpecification);
        // if(true) throw new RuntimeException();
        NSArray someFiles = (FSQualifierHandler.filesWithQualifier(qualifier, rootDirectory(anEntity)));

        if (someFiles != null) {
            NSArray someSortOrderings = aFetchSpecification.sortOrderings();
            if (someSortOrderings != null)
                someFiles = (EOSortOrdering.sortedArrayUsingKeyOrderArray(someFiles, someSortOrderings));
            someFiles = filteredArrayWithEntity(someFiles, anEntity);
            if (someFiles != null)
                files().addObjectsFromArray(someFiles);
        }
    }

    public void setAttributesToFetch(NSArray someAttributes) {
        if (someAttributes != null)
            _attributes = someAttributes;
        else
            throw new IllegalArgumentException("FSAdaptorChannel.setAttributesToFetch: null attributes.");
    }

    public int updateValuesInRowsDescribedByQualifier(NSDictionary aRow, EOQualifier aQualifier, EOEntity anEntity) {
        if (aRow != null) {
            if (aQualifier != null) {
                if (anEntity != null) {
                    NSArray someFiles = FSQualifierHandler.filesWithQualifier(aQualifier, rootDirectory(anEntity));
                    if (someFiles != null) {
                        someFiles = filteredArrayWithEntity(someFiles, anEntity);
                        if (someFiles != null) {
                            int count = someFiles.count();
                            for (int index = 0; index < count; index++) {
                                File aFile = (File) someFiles.objectAtIndex(index);
                                NSArray someKeys = aRow.allKeys();
                                int keyCount = someKeys.count();

                                for (int keyIndex = 0; keyIndex < count; keyIndex++) {
                                    Object aKey = someKeys.objectAtIndex(keyIndex);
                                    EOAttribute anAttribute = anEntity.attributeNamed(aKey.toString());
                                    if (anAttribute != null) {
                                        Object aValue = aRow.objectForKey(aKey);

                                        NSKeyValueCoding.DefaultImplementation.takeValueForKey(aFile, aValue, anAttribute.columnName());
                                    }
                                }
                            }
                            return count;
                        }
                    }
                    return 0;
                }
                throw new IllegalArgumentException("FSAdaptorChannel.updateValuesInRowsDescribedByQualifier: null entity.");
            }
            throw new IllegalArgumentException("FSAdaptorChannel.updateValuesInRowsDescribedByQualifier: null qualifier.");
        }
        throw new IllegalArgumentException("FSAdaptorChannel.updateValuesInRowsDescribedByQualifier: null row.");
    }

    private String defaultModelPath() {
        Class aClass = this.getClass();
        String aClassName = aClass.getName();
        String aResourceName = "/" + aClassName.replace('.', '/') + ".class";
        URL anURL = aClass.getResource(aResourceName);
        if (anURL != null) {
            String aPath = anURL.getFile();
            String aPrefix = "file:/";
            String aSeparator = "!";
            int anIndex = aPath.indexOf(aPrefix);
            if (anIndex != -1)
                aPath = aPath.substring(anIndex - 1 + aPrefix.length(), aPath.length() - 1);
            anIndex = aPath.indexOf(aSeparator);
            if (anIndex != -1)
                aPath = aPath.substring(0, anIndex);
            File aFile = new File(aPath);
            File aModelFile = new File(new File(aFile.getParent()).getParent() + "/FS", "FS.eomodeld");
            if (debug)
                System.out.println(aFile);
            if (debug)
                System.out.println(aModelFile);
            return aModelFile.getAbsolutePath();
        }
        return null;
    }

    public NSArray describeTableNames() {
        return new NSArray(TableNames);
    }

    public EOModel describeModelWithTableNames(NSArray anArray) {
        return new EOModel(defaultModelPath());
    }

    private NSArray filteredArrayWithEntity(NSArray anArray, EOEntity anEntity) {
        if (anArray != null) {
            if (anEntity != null) {
                String anEntityName = anEntity.externalName();
                if (debug)
                    System.out.println("filteredArrayWithEntity: " + anEntity.name() + "/" + anEntityName + " --- " + anArray);
                if (!anEntityName.equals("FSItem")) {
                    Boolean isDirectory = Boolean.FALSE;
                    if (anEntityName.equals("FSDirectory") == true)
                        isDirectory = Boolean.TRUE;
                    anArray = (EOQualifier.filteredArrayWithQualifier(anArray, new EOKeyValueQualifier("isDirectory", (EOQualifier.QualifierOperatorEqual), isDirectory)));
                }
                if (anArray != null && anArray.count() > 0)
                    return anArray;
                return null;
            }
            throw new IllegalArgumentException("FSAdaptorChannel.filteredArrayWithEntity: null entity.");
        }
        throw new IllegalArgumentException("FSAdaptorChannel.filteredArrayWithEntity: null array.");
    }

    private NSMutableDictionary dictionaryForFileWithAttributes(File aFile, NSArray someAttributes) {
        if (aFile != null) {
            if (someAttributes != null) {
                NSMutableDictionary aDictionary = new NSMutableDictionary();
                int count = someAttributes.count();
                for (int index = 0; index < count; index++) {
                    EOAttribute anAttribute = (EOAttribute) someAttributes.objectAtIndex(index);
                    String columnName = anAttribute.columnName();
                    Object aValue = null;
                    if ("content".equals(columnName)) {
                        try {
                            String path = aFile.getAbsolutePath();
                            InputStream in = new FileInputStream(path);

                            if (null == in)
                                throw new RuntimeException("The file '" + path + "' can not be opened.");
                            int length = in.available();
                            if (length == 0) {
                                aValue = "";
                            }
                            byte buffer[] = new byte[length];
                            in.read(buffer);
                            in.close();
                            aValue = new String(buffer);
                        } catch (IOException ex) {
                            System.err.println("dictionaryForFileWithAttributes : (" + aFile.getName() + ") " + ex);
                        }
                    } else if ("realFile".equals(columnName)) {
                        aValue = aFile;
                    } else {
                        aValue = NSKeyValueCoding.DefaultImplementation.valueForKey(aFile, columnName);
                    }
                    if (aValue == null)
                        aValue = NSKeyValueCoding.NullValue;
                    aDictionary.setObjectForKey(aValue, anAttribute.name());
                }
                return aDictionary;
            }
            throw new IllegalArgumentException("FSAdaptorChannel.dictionaryForFileWithAttributes: null attributes.");
        }
        throw new IllegalArgumentException("FSAdaptorChannel.dictionaryForFileWithAttributes: null file.");
    }
}
