package er.indexing.eof;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

import com.webobjects.eocontrol.EOEditingContext;

public class ERIDirectory extends _ERIDirectory {

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERIDirectory.class);

    public static final ERIDirectoryClazz clazz = new ERIDirectoryClazz();
    
    public static class EOFIndexOutput extends IndexOutput {

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void flush() throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public long getFilePointer() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long length() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void seek(long l) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void writeByte(byte byte0) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void writeBytes(byte[] abyte0, int i, int j) throws IOException {
            // TODO Auto-generated method stub
            
        }
    }
    
    public static class EOFIndexInput extends IndexInput {

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public long getFilePointer() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long length() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public byte readByte() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void readBytes(byte[] abyte0, int i, int j) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void seek(long l) throws IOException {
            // TODO Auto-generated method stub
            
        }
    }
    
    public static class EOFDirectory extends Directory {

        @Override
        public void close() throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public IndexOutput createOutput(String s) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void deleteFile(String s) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public boolean fileExists(String s) throws IOException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public long fileLength(String s) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long fileModified(String s) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public String[] list() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IndexInput openInput(String s) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void renameFile(String s, String s1) throws IOException {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void touchFile(String s) throws IOException {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    public static class ERIDirectoryClazz extends _ERIDirectory._ERIDirectoryClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIDirectory.Key {}

    /**
     * Initializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
