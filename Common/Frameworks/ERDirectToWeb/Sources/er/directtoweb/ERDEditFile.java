//
// ERD2WUpload.java: Class file for WO Component 'ERD2WUpload'
// Project ERExtras
//
// Created by ak on Mon Jul 08 2002
//
package er.directtoweb;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXSimpleTemplateParser;
import er.extensions.ERXValidationException;
import er.extensions.ERXValidationFactory;
import er.extensions.ERXValueUtilities;

/**
 * Allows you to handle a file name on the server as a property.
 * You can upload and delete the contents.
 * @author ak
 *
 */
public class ERDEditFile extends ERDCustomEditComponent {
    static final Logger log = Logger.getLogger(ERDEditFile.class);
    // Instance variables for the name and contents of the upload
    public String fileName;
    public String uploadDirectory;
    public NSData fileContents;
    public NSDictionary extraBindings;
    public boolean shouldRaise = false;
    public boolean didUpload = false;
    
    public ERDEditFile(WOContext context) {
        super(context);
    }

    void extract(InputStream in, String[] files) throws IOException {
        ZipInputStream zis = new ZipInputStream(in);
        ZipEntry e;

        while ((e = zis.getNextEntry()) != null) {
            if (files == null)
                extractFile(zis, e);
            else {
                String name = e.getName();
                for (int i = 0; i < files.length; i++) {
                    String file = files[i].replace(File.separatorChar, '/');
                    if (name.startsWith(file)) {
                        extractFile(zis, e);
                        break;
                    }
                }
            }
        }
    }

    void extractFile(ZipInputStream zis, ZipEntry e) throws IOException {
        String name = e.getName();
        File f = new File(uploadDirectory() + File.separatorChar + e.getName().replace('/', File.separatorChar));

        if (e.isDirectory()) {
            if (!f.exists() && !f.mkdirs() || !f.isDirectory())
                throw new IOException("Can't create " + f.getPath());
            if (log.isDebugEnabled())
                log.debug("Processing: " + name);
        } else {
            if (f.getParent() != null) {
                File d = new File(f.getParent());
                if (!d.exists() && !d.mkdirs() || !d.isDirectory())
                    throw new IOException("Can't create " + d.getPath());
            }
            OutputStream os = new FileOutputStream(f);
            byte[] b = new byte[512];
            int len;
            while ((len = zis.read(b, 0, b.length)) != -1)
                os.write(b, 0, len);
            zis.closeEntry();
            os.close();
            if (log.isDebugEnabled()) {
                if (e.getMethod() == 8)
                    log.debug("Extracted: " + name);
                else
                    log.debug("Inflated: " + name);
            }
        }
    }
    
    public String uploadDirectory() {
        if(uploadDirectory == null) {
            uploadDirectory = (String)valueForBinding("uploadDirectory");
            if(uploadDirectory == null) {
                uploadDirectory = "/tmp";
            }
            uploadDirectory = ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(uploadDirectory, null, this, object());
        }
        return uploadDirectory;
    }
    
    public WOActionResults invokeAction(WORequest worequest,
                                        WOContext wocontext) {
        WOActionResults results = super.invokeAction(worequest, wocontext);
        return results;
    }


    public void takeValuesFromRequest(WORequest q, WOContext c) throws NSValidation.ValidationException {
        super.takeValuesFromRequest(q,c);
        uploadFile();
        try {
            if(key().charAt(0) != '#')
                object().validateTakeValueForKeyPath(objectKeyPathValue(),key());
        } catch(Throwable e) {
            validationFailedWithException (e, objectKeyPathValue(), key());
        }
    }
    
/*    public void awake() {
        super.awake();
        if(!synchronizesVariablesWithBindings())
            reset();
    }
*/
    public void sleep() {
        if(!synchronizesVariablesWithBindings())
            reset();
        super.sleep();
    }

    
    public void reset() {
        fileName = null;
        fileContents = null;
        uploadDirectory = null;
        extraBindings = null;
        shouldRaise = false;
        didUpload = false;
    }
    
    public boolean isStateless() {
        return false;
    }

    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public String fileName() {
        if(fileName == null) {
            fileName = (String)valueForBinding("fileName");
        }
        if(fileName != null) {
            fileName = fileName.replace('\\', '/');
            fileName = fileName.replace(':', '/');
            fileName =  NSPathUtilities.lastPathComponent(fileName);
        }
        return fileName;
    }

    public String realPath() {
        String fileName = fileName();
        if(fileName == null || fileName().length() == 0) return null;
        File tmpPath = new File(uploadDirectory());
        tmpPath.mkdirs();
        String filePath = ( tmpPath.exists() ) ? tmpPath.getAbsolutePath() : System.getProperty( "user.dir" );

        // Create the output path for the file on the application server
        return new String( filePath + File.separator + lastPartOfFileName());
    }

    public NSData fileContentz() {
        if(fileContents == null) {
            // fileContents = (NSData)valueForBinding("fileContentz");
        }
        return fileContents;
    }
    
    public void setFileContentz(NSData data) {
        fileContents = data;
    }

    
    public boolean haveData() {
        if (fileContentz() != null && fileContentz().length() > 0 ) {
            return true;
        }
        return false;
    }

    public void setObjectKeyPathValue(Object value) {
        if(key().charAt(0) != '#')
            super.setObjectKeyPathValue(value);
    }

    public Object objectKeyPathValue() {
        if(key().charAt(0) != '#')
            return super.objectKeyPathValue();
        return null;
    }
    /*
*/
    public void deleteFile() {
        // Create the output path for the file on the application server
        if (fileExists()) {
            File file = new File(uploadDirectory() + File.separator + objectKeyPathValue());
            file.delete();

            log.debug( "Deleted file from '" + file + "'" );
        } else {
            log.debug( "No File Deleted" );
        }
        setObjectKeyPathValue(null);
    }

    public boolean shouldUnpack() {
        return ERXValueUtilities.booleanValueWithDefault(valueForBinding("shouldUnpack"), false);
    }
    
    public boolean fileExists() {
        if(uploadDirectory() != null && objectKeyPathValue() != null)
            return (new File(uploadDirectory() + File.separator + objectKeyPathValue())).exists();
        return false;
    }

    public String lastPartOfFileName() {
        if(fileName() != null) {
            return NSPathUtilities.lastPathComponent(fileName());
        }
        return null;
    }

    public void uploadFile() {
        // Create the output path for the file on the application server
        if(didUpload)
            return;
        if(fileContentz() == null || fileContentz().length() <= 0 ) {
            return;
        }
        String outputFilePath = realPath();

        if (outputFilePath != null) {
 
            log.info("fileName: " + outputFilePath);

            if(new File(outputFilePath).exists()) {
                ERXValidationException ex = ERXValidationFactory.defaultFactory().createException(object(), key(), fileName(), "fileexists");
                validationFailedWithException(ex, fileName(), key());
            } else {
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);
                    fileContentz().writeToStream(fileOutputStream);
                    fileOutputStream.close();
                    log.debug( "Wrote file to '" + outputFilePath + "'" + shouldUnpack());
                    if(shouldUnpack()) {
                        try {
                            extract(new BufferedInputStream(new FileInputStream(outputFilePath)), null);
                        } catch(IOException ex) {
                            log.warn("Unpacking error: " + ex);
                        }
                    }

                    setObjectKeyPathValue(lastPartOfFileName());
                    didUpload = true;
                } catch (IOException e) {
                    log.error("Error writing file: " + e);
                }
            }

        } else {
            log.debug( "No File Uploaded" );
        }
    }

    public WOComponent deleteAction()  {
        deleteFile();
        return context().page();
    }

    public WOComponent uploadAction()  {
        uploadFile();
        return context().page();
    }

}
