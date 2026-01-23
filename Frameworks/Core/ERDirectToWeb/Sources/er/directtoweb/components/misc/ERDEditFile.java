package er.directtoweb.components.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSValidation;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.foundation.ERXSimpleTemplateParser;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.localization.ERXLocalizer;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

/**
 * Allows you to handle a file name on the server as a property.
 * You can upload and delete the contents.
 * @author ak
 *
 */
public class ERDEditFile extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(ERDEditFile.class);
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
    
    private String localizedValueForBinding(String binding) {
        String result = (String)valueForBinding(binding);
        if(result == null) {
            result = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault("ERDEditFile." + binding);
        }
        return result;
    }
    
    public String selectMessage() {
        String result = localizedValueForBinding("selectMessage");
        return result;
    }
    
    public String uploadMessage() {
        String result = localizedValueForBinding("uploadMessage");
        return result;
    }
    
    public String selectButton() {
        String result = localizedValueForBinding("selectButton");
        return result;
    }
    
    public String uploadButton() {
        String result = localizedValueForBinding("uploadButton");
        return result;
    }
    
    public String deleteButton() {
        String result = localizedValueForBinding("deleteButton");
        return result;
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

    @Override
    public WOActionResults invokeAction(WORequest worequest,
                                        WOContext wocontext) {
        WOActionResults results = super.invokeAction(worequest, wocontext);
        return results;
    }

    @Override
    public void takeValuesFromRequest(WORequest q, WOContext c) throws NSValidation.ValidationException {
        super.takeValuesFromRequest(q,c);
        uploadFile();
        try {
            // allow for uploads that don't end up in the object
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
    @Override
    public void sleep() {
        if(!synchronizesVariablesWithBindings())
            reset();
        super.sleep();
    }

    @Override
    public void reset() {
        fileName = null;
        fileContents = null;
        uploadDirectory = null;
        extraBindings = null;
        shouldRaise = false;
        didUpload = false;
    }
    
    @Override
    public boolean isStateless() {
        return false;
    }

    @Override
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
        return filePath + File.separator + lastPartOfFileName();
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

    @Override
    public void setObjectKeyPathValue(Object value) {
        if(key().charAt(0) != '#')
            super.setObjectKeyPathValue(value);
    }

    @Override
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
