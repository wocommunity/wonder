package er.indexing.storage;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSMutableRange;
import com.webobjects.foundation.NSTimestamp;

public class ERIFile extends _ERIFile {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERIFile.class);

    public static final ERIFileClazz clazz = new ERIFileClazz();
    
    public static class ERIFileClazz extends _ERIFile._ERIFileClazz {
        /* more clazz methods here */
    }

    public interface Key extends _ERIFile.Key {}
    
    private class EOFIndexOutput extends IndexOutput {

        long filePointer = 0;
        long fileLength = 0;
        NSMutableData data;
        boolean dirty = false;
        
        public EOFIndexOutput(NSData contentData) {
            data = new NSMutableData(contentData);
            fileLength = data.length();
        }

        private NSMutableData data() {
            return data;
        }

        @Override
        public void close() throws IOException {
            flush();
        }

        @Override
        public void flush() throws IOException {
            if (dirty) {
				if (length() < data().length()) {
					data().setLength((int) length());
				}
				editingContext().lock();
				try {
					setContentData(data());
					editingContext().saveChanges();
				} finally {
					editingContext().unlock();
				}
			}
            dirty = false;
        }

        @Override
        public long getFilePointer() {
            return filePointer;
        }

        @Override
        public long length() {
            return fileLength;
        }

        @Override
        public void seek(long l) throws IOException {
            assureLength(l);
            filePointer = l;
        }

        private void assureLength(long len) {
            if(length() < len) {
                if(data().length() < len) {
                    data().setLength((int) len + 128000);
                }
                fileLength = len;
                dirty = true;
            }
        }
        
        @Override
        public void writeByte(byte byte0) throws IOException {
            assureLength(filePointer+1);
            NSMutableRange range = new NSMutableRange((int)filePointer, 1);
            byte[] bytes = data().bytesNoCopy(range);
            bytes[(int) filePointer] = byte0;
            filePointer += 1;
            dirty = true;
        }

        @Override
        public void writeBytes(byte[] abyte0, int offset, int len) throws IOException {
            assureLength(filePointer+len);
            NSMutableRange range = new NSMutableRange((int)filePointer, len);
            byte[] bytes = data().bytesNoCopy(range);
            System.arraycopy(abyte0, offset, bytes, (int)filePointer, len);
            filePointer += len;
            dirty = true;
        }
    }
    
    private class EOFIndexInput extends IndexInput {

        long filePointer = 0;
        NSData data;
        
        public EOFIndexInput(NSData contentData) {
            data = contentData;
        }

        private NSData data() {
            return data;
        }

        @Override
        public void close() throws IOException {
            filePointer = 0;
        }

        @Override
        public long getFilePointer() {
            return filePointer;
        }

        @Override
        public long length() {
            return data().length();
        }
        
        private void assureLength(long len) throws IOException {
            if(len > length()) {
                throw new IOException("Not enough data: " + len + " vs " + length());
            }
        }

        @Override
        public byte readByte() throws IOException {
            assureLength(filePointer+1);
            return data().bytes((int)filePointer++, 1)[0];
        }

        @Override
        public void readBytes(byte[] abyte0, int offset, int len) throws IOException {
            assureLength(filePointer+len);
            System.arraycopy(data().bytesNoCopy(new NSMutableRange((int)filePointer, len)), (int)filePointer, abyte0, offset, len);
            filePointer += len;
        }

        @Override
        public void seek(long l) throws IOException {
            assureLength(l);
            filePointer = l;
        }
    }
    
    @Override
    public void didInsert() {
        super.didInsert();
        log.debug("Did create: {}", name());
    }
    @Override
    public void didUpdate() {
        super.didUpdate();
        log.debug("Did update: {}->{}", name(), length());
    }
    @Override
    public void didDelete(EOEditingContext ec) {
        super.didUpdate();
        log.debug("Did delete: {}->{}", name(), length());
    }
    
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
        ERIFileContent content = ERIFileContent.clazz.createAndInsertObject(ec);
        setContent(content);
        setContentData(new NSData());
    }
    
    private void setContentData(NSData data) {
        setLastModified(new NSTimestamp());
        setLength((long)data.length());
        content().setContent(data);
    }

    private NSData contentData() {
        return content().content();
    }

    public void touch() {
        setLastModified(new NSTimestamp());
        editingContext().saveChanges();
    }

    public IndexInput openInput() {
        return new EOFIndexInput(contentData());
    }

    public long timestamp() {
        return lastModified().getTime();
    }

    public IndexOutput createOutput() {
        return new EOFIndexOutput(contentData());
    }
}
