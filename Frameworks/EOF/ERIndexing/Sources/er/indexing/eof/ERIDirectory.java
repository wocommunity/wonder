package er.indexing.eof;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXMutableDictionary;

public class ERIDirectory extends _ERIDirectory {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIDirectory.class);

    public static final ERIDirectoryClazz clazz = new ERIDirectoryClazz();
   
    private class EOFDirectory extends Directory {

        @Override
        public void close() throws IOException {
            editingContext().saveChanges();
        }

        @Override
        public IndexOutput createOutput(String s) throws IOException {
            return fileForName(s).createOutput();
        }

        @Override
        public void deleteFile(String s) throws IOException {
            ERIFile file = fileForName(s);
            if(file != null) {
                file.delete();
                editingContext().saveChanges();
            }
        }

        @Override
        public boolean fileExists(String s) throws IOException {
            return fileForName(s) != null;
        }

        @Override
        public long fileLength(String s) throws IOException {
            return fileForName(s).length();
        }

        @Override
        public long fileModified(String s) throws IOException {
            return fileForName(s).timestamp();
        }

        @Override
        public String[] list() throws IOException {
            return ((NSArray<String>)files().valueForKeyPath(ERIFile.Key.NAME)).objects();
        }

        @Override
        public IndexInput openInput(String s) throws IOException {
            return fileForName(s).openInput();
        }

        @Override
        public void renameFile(String s, String s1) throws IOException {
            fileForName(s).setName(s1);
            editingContext().saveChanges();
        }

        @Override
        public void touchFile(String s) throws IOException {
            fileForName(s).touch();
            editingContext().saveChanges();
        }
        
    }
    
    public static class ERIDirectoryClazz extends _ERIDirectory._ERIDirectoryClazz {

        private NSMutableDictionary<String,ERIDirectory> directories = ERXMutableDictionary.synchronizedDictionary();
        
        public Directory directoryForName(String store) {
            ERIDirectory result = directories.objectForKey(store);
            if(result == null) {
                ERXEC ec = (ERXEC) ERXEC.newEditingContext();

                ec.setUseAutoLock(true);
                ERIDirectory directory = objectMatchingKeyAndValue(ec, Key.NAME, store);
                if(directory == null) {
                    directory = createAndInsertObject(ec);
                    directory.setName(store);
                    ec.saveChanges();
                }
                result = directory;
                directories.setObjectForKey(directory, store);
            }
            return result.directory();
        }

    }

    public interface Key extends _ERIDirectory.Key {}

    public void init(EOEditingContext ec) {
        super.init(ec);
    }

    public ERIFile fileForName(String name) {
        for (ERIFile file : files()) {
            if(file.name().equals(name)) {
                return file;
            }
        }
        return null;
    }

    private Directory _directory;
    
    public Directory directory() {

        if(_directory == null) {
            _directory = new EOFDirectory();
        }
        return _directory;
    }
}
