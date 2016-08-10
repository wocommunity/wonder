package er.fsadaptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public final class FSAdaptorChannel extends EOAdaptorChannel {
    public static boolean debug = "YES".equals(System.getProperty("FSAdaptorDebuggingEnabled"));

    private static final String DefaultModelName = "FS.eomodeld";

    private static final String[] TableNames = { "FSItem", "FSFile", "FSDirectory" };

    private boolean _isOpen = false;

    private NSArray<EOAttribute> _attributes = null;

    private final NSMutableArray<File> _files = new NSMutableArray<File>();

    public FSAdaptorChannel(EOAdaptorContext aContext) {
        super(aContext);
    }

    private NSMutableArray<File> files() {
        return _files;
    }

    @Override
    public NSArray<EOAttribute> attributesToFetch() {
        return _attributes;
    }

    @Override
    public void cancelFetch() {
        files().removeAllObjects();
    }

    @Override
    public void closeChannel() {
        _isOpen = false;
    }

    @Override
    public int deleteRowsDescribedByQualifier(EOQualifier aQualifier, EOEntity anEntity) {
        if (aQualifier != null) {
            if (anEntity != null) {
                NSArray<File> someFiles = FSQualifierHandler.filesWithQualifier(aQualifier, rootDirectory(anEntity));
                if (someFiles != null) {
                    someFiles = filteredArrayWithEntity(someFiles, anEntity);
                    if (someFiles != null) {
                        int count = someFiles.count();
                        int counter = 0;
                        for (int index = 0; index < count; index++) {
                            File aFile = someFiles.objectAtIndex(index);
                            if (aFile.delete())
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

    @Override
    public NSArray<EOAttribute> describeResults() {
        return _attributes;
    }

    @Override
    public void evaluateExpression(EOSQLExpression anExpression) {
        throw new UnsupportedOperationException("FSAdaptorChannel.evaluateExpression");
    }

    @Override
    public void executeStoredProcedure(EOStoredProcedure aStoredProcedure, NSDictionary someValues) {
        throw new UnsupportedOperationException("FSAdaptorChannel.executeStoredProcedure");
    }

    @Override
    public NSMutableDictionary<String, Object> fetchRow() {
        File aFile = files().lastObject();
        if (aFile != null) {
            files().removeLastObject();
            return dictionaryForFileWithAttributes(aFile, attributesToFetch());
        }
        return null;
    }

    @Override
    public void insertRow(NSDictionary<String, Object> aRow, EOEntity anEntity) {
        if (aRow != null) {
            if (anEntity != null) {
                String aPath = (String) aRow.objectForKey("absolutePath");
                if (aPath != null) {
                    File aFile = new File(aPath);
                    try {
                        if (anEntity.externalName().equals("FSDirectory"))
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

    @Override
    public boolean isFetchInProgress() {
        if (files().count() > 0)
            return true;
        return false;
    }

    @Override
    public boolean isOpen() {
        return _isOpen;
    }

    @Override
    public void openChannel() {
        _isOpen = true;
    }

    @Override
    public NSDictionary returnValuesForLastStoredProcedureInvocation() {
        throw new UnsupportedOperationException("FSAdaptorChannel.returnValuesForLastStoredProcedureInvocation");
    }

    public String rootDirectory(EOEntity entity) {
        String root = (String) entity.model().connectionDictionary().objectForKey("rootDirectory");
        if (root == null)
            root = "";
        return root;
    }

    @Override
    public void selectAttributes(NSArray<EOAttribute> someAttributes, EOFetchSpecification aFetchSpecification, boolean shouldLock, EOEntity anEntity) {
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
        NSArray<File> someFiles = FSQualifierHandler.filesWithQualifier(qualifier, rootDirectory(anEntity));

        if (someFiles != null) {
            NSArray<EOSortOrdering> someSortOrderings = aFetchSpecification.sortOrderings();
            if (someSortOrderings != null)
                someFiles = EOSortOrdering.sortedArrayUsingKeyOrderArray(someFiles, someSortOrderings);
            someFiles = filteredArrayWithEntity(someFiles, anEntity);
            if (someFiles != null)
                files().addObjectsFromArray(someFiles);
        }
    }

    @Override
    public void setAttributesToFetch(NSArray<EOAttribute> someAttributes) {
        if (someAttributes != null)
            _attributes = someAttributes;
        else
            throw new IllegalArgumentException("FSAdaptorChannel.setAttributesToFetch: null attributes.");
    }

    @Override
    public int updateValuesInRowsDescribedByQualifier(NSDictionary aRow, EOQualifier aQualifier, EOEntity anEntity) {
        if (aRow != null) {
            if (aQualifier != null) {
                if (anEntity != null) {
                    NSArray<File> someFiles = FSQualifierHandler.filesWithQualifier(aQualifier, rootDirectory(anEntity));
                    if (someFiles != null) {
                        someFiles = filteredArrayWithEntity(someFiles, anEntity);
                        if (someFiles != null) {
                            int count = someFiles.count();
                            for (int index = 0; index < count; index++) {
                                File aFile = someFiles.objectAtIndex(index);
                                NSArray someKeys = aRow.allKeys();
                                int keyCount = someKeys.count();

                                for (int keyIndex = 0; keyIndex < keyCount; keyIndex++) {
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

    private URL defaultModelUrl() {
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
            File aModelFile = new File(aFile.getParentFile().getParent() + "/FS", DefaultModelName);
            if (debug) {
                System.out.println(aFile);
                System.out.println(aModelFile);
            }
            try {
                return aModelFile.toURI().toURL();
            } catch (MalformedURLException e) {
                System.out.println(e);
            }
        }
        return null;
    }

    @Override
    public NSArray describeTableNames() {
        return new NSArray(TableNames);
    }

    @Override
    public EOModel describeModelWithTableNames(NSArray tableNames) {
        return new EOModel(defaultModelUrl());
    }

    private NSArray filteredArrayWithEntity(NSArray anArray, EOEntity anEntity) {
        if (anArray != null) {
            if (anEntity != null) {
                String anEntityName = anEntity.externalName();
                if (debug)
                    System.out.println("filteredArrayWithEntity: " + anEntity.name() + "/" + anEntityName + " --- " + anArray);
                if (!anEntityName.equals("FSItem")) {
                    Boolean isDirectory = Boolean.FALSE;
                    if (anEntityName.equals("FSDirectory"))
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

    private NSMutableDictionary<String, Object> dictionaryForFileWithAttributes(File aFile, NSArray<EOAttribute> someAttributes) {
        if (aFile != null) {
            if (someAttributes != null) {
                NSMutableDictionary<String, Object> aDictionary = new NSMutableDictionary<String, Object>();
                for (EOAttribute anAttribute : someAttributes) {
                    String columnName = anAttribute.columnName();
                    Object aValue = null;
                    if ("content".equals(columnName)) {
                        InputStream in = null;
                        try {
                            String path = aFile.getAbsolutePath();
                            in = new FileInputStream(path);
                            int length = in.available();
                            if (length == 0) {
                                aValue = "";
                            } else {
                                byte buffer[] = new byte[length];
                                in.read(buffer);
                                aValue = new String(buffer);
                            }
                        } catch (IOException ex) {
                            System.err.println("dictionaryForFileWithAttributes : (" + aFile.getName() + ") " + ex);
                        } finally {
                            if (in != null) {
                                try {
                                    in.close();
                                } catch (IOException e) {
                                    // ignore
                                }
                            }
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
