/*
 * See COPYING for license information.
 */ 

package com.rackspacecloud.client.cloudfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilesObject
{
    private String container;
    private String name;
    private String md5sum = null;
    private long size = -1;
    private String mimeType = null;
    private String lastModified = null;
    private FilesClient client = null;

    private static final Logger log = LoggerFactory.getLogger(FilesObject.class);

    /**
     * Constructs a new FilesObject (from the file system)
     * 
     * @param obj        A file representing the object
     * @param mimeType   Its MIME type
     * @param container  The container it lives in
     * @throws NullPointerException      A null parameter was passed in
     * @throws NoSuchAlgorithmException  MD5 was not installed on the client.
     * @throws IOException               There was an I/O error talking to the server
     */
    FilesObject (File obj, String mimeType, FilesContainer container) throws NullPointerException, NoSuchAlgorithmException, IOException
    {
    	if (obj != null)
        {
            if (obj.exists())
            {
                if (!obj.isDirectory())
                {
                    setName (obj.getName());
                    setMd5sum(FilesClient.md5Sum(obj));
                    setSize(obj.length());
                    setMimeType(mimeType);
                    setClient(container.getClient ());
                    setContainer(container.getName());
                }
                else
                {
                	log.error("Can not create Directories as FSObjects create a FilesContainer for this object");
                	throw new NullPointerException("File Object was a directory !");
                }
            }
            else
            {
                log.error("File object must exist so we can create an MD5SUM for it !");
                throw new NullPointerException ("The file object provided does not exist.");
            }
        }
    	else
        {
            log.error("Not possible to create a FilesObject from a null File.");
            throw new NullPointerException ("File Object passed was null !");
        }
    }

    /**
     * Creates a new FilesObject with data from the server
     * 
     * @param name      The name of the object
     * @param container The name of it's container
     * @param client    The client it can be accessed through.
     */
    FilesObject(String name, String container, FilesClient client )
    {
        this.name = name;
        this.container = container;
        this.client = client;
    }

    /**
     * @return The object's name on the server
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set's the objects name (up to a maximum of 128 characters)
     * 
     * @param name The new name
     */
    public void setName(String name)
    {
        if (name.length() > FilesConstants.OBJECT_NAME_LENGTH)
        {
            log.warn("Object name larger than {} characters truncating from: {}", FilesConstants.OBJECT_NAME_LENGTH, name);
            this.name = name.substring(0, FilesConstants.OBJECT_NAME_LENGTH);
            log.warn("Object name truncated to: {}", name);
        }
        this.name = name;
    }

    /**
     * @return The MIME type of the object, pulled from the server
     * @throws HttpException if there was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public String getMimeType() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
        if (mimeType == null) {
        	getMetaData();
        }
        return mimeType;
    }

    /**
     * @param mimeType The new MIME type for this object
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * Get's the MD5 Checksum for this object 
     * 
     * @return The MD5 checksum, returned as a base 16 encoded string
     * @throws HttpException if there was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public String getMd5sum() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
        if (md5sum == null) {
        	getMetaData();
        }
        return md5sum;
    }

    /**
     * 
     * @param md5sum The MD5 sum of the file (as a hex-encoded string)
     */
    public void setMd5sum(String md5sum)
    {
        this.md5sum = md5sum;
    }

    /**
     * Returns the size of the object, in bytes
     * 
     * @return The size of the object in bytes
     * @throws HttpException if there was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public long getSize() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
        if (size == -1) {
        	getMetaData();
        }
        return size;
    }

    /**
     * Set the size of the object
     * 
     * @param size The size, in bytes
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    /**
     * Download the object and write it to a local file
     * 
     * @param localFile The file
     * @return The number of bytes written
     * @throws FileNotFoundException Could not find the local file (does the path to it exist?)
     * @throws HttpException if there was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public long writeObjectToFile (File localFile) throws FileNotFoundException, HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
    	long bytesWritten = 0;
    	FileOutputStream out = new FileOutputStream(localFile);
    	InputStream in =  getObjectAsStream();
        byte[] data = new byte[1024];
        
        int ret = in.read (data, 0, data.length);
        if (ret == -1)
        {
        	out.write(data, 0, data.length);
            out.flush();
            out.close();
            in.close ();
        	return data.length;
        }

        while (ret != -1)
        {
        	bytesWritten += ret;
            out.write(data, 0, ret);
            ret = in.read (data, 0, data.length);
        }
        out.flush();
        out.close();
        in.close ();

    	return bytesWritten;
    }

    /**
     * Returns an inputStream with the contents of the object
     * 
     * @return An inputStream that will return the contents of the object
     * @throws HttpException There was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public InputStream getObjectAsStream() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
    	return client.getObjectAsStream(container, name);
    }

    /**
     * Download the contents of the object
     * 
     * @return The content of the object
     * @throws HttpException if there was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     * @throws FilesNotFoundException The container does not exist
     */
    public byte[] getObject() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException, FilesNotFoundException
    {
    	return client.getObject(container, name);
    }

    /**
     * Return any metadata associated with this object
     * 
     * @return The metadata
     * @throws HttpException if there was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public FilesObjectMetaData getMetaData() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
    	FilesObjectMetaData metaData = client.getObjectMetaData(container, name);
        if (metaData != null)
        {
            setMd5sum(metaData.getETag());
            setSize(Long.parseLong (metaData.getContentLength()));
            setMimeType(metaData.getMimeType());
            setLastModified(metaData.getLastModified());
        }
        return metaData;
    }

    /**
     * @param client The new client for this object
     */
    void setClient(FilesClient client)
    {
        this.client = client;
    }

    /**
     * @param container The new container
     */
    void setContainer (String container)
    {
        this.container = container;
    }
    
    public static FilesObject uploadObject(File obj, String mimeType, FilesContainer container)  throws IOException, FilesException {
		return uploadObject(obj, mimeType, container, null);
    }

    public static FilesObject uploadObject(File obj, String mimeType, FilesContainer container, IFilesTransferCallback callback) throws IOException, FilesException {
    	FilesObject result = null;
    	try {
    		result = new FilesObject(obj, mimeType, container);
    		FilesClient client = container.getClient();
    		client.storeObjectAs(container.getName(), obj, mimeType, obj.getName(), callback);
    	}
    	catch (NoSuchAlgorithmException nsae) {
    		// This should never happen
    		log.error("Install doesn't have MD5, can't upload files", nsae);
    	}
    	catch (HttpException ex) {
    		throw new FilesException("Error in network operation", ex);
    	}
    	
		return result;
    }

    /**
     * Returns the size as a human readable string, rounding to the nearest KB/MB/GB
     * 
     * @return The size of the object as a human readable string.
     * @throws HttpException There was an error communicating with the server
     * @throws IOException There was an I/O exception communicating with the server or writing the file.
     * @throws FilesAuthorizationException The Client's Login was invalid
     * @throws FilesInvalidNameException The container or object name was not valid
     */
    public String getSizeString () throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
    {
        long kb = 1024;
        long mb = kb*1024;
        long gb = mb*1024;

        //Make sure the size is correct.
        getMetaData();

            //KB
        if (size > gb)
            return (size/gb) + " GB";
        else if (size > mb)
            return (size/mb)+" MB";
        else if (size > kb)
            return (size/kb) +" KB";
        else
            return getSize() + " Bytes";

    }

	/**
	 * @return the lastModified
	 * @throws HttpException if there was an error communicating with the server
	 * @throws IOException There was an I/O exception communicating with the server or writing the file.
	 * @throws FilesAuthorizationException The Client's Login was invalid
	 * @throws FilesInvalidNameException The container or object name was not valid
	 */
	public String getLastModified() throws HttpException, IOException, FilesAuthorizationException, FilesInvalidNameException
	{
		if (lastModified == null) { 
			getMetaData();
		}
		return lastModified;
	}

	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	/**
	 * 
	 * @return True if the "object" is really a directory.
	 */
	public boolean isDirectory() {
		return size == 0 && "application/directory".equals(mimeType);
	}
	
	/**
	 * 
	 * @return The CDN url for the object (if its container has been CDN enabled), null if 
	 *         the container hasn't been CDN enabled.
	 * @throws HttpException if there was an error communicating with the server
	 * @throws IOException There was an I/O exception communicating with the server or writing the file.
	 * @throws FilesException There was an error talking to the CloudFiles Server
	 */
	public String getCDNURL () throws FilesException, IOException, HttpException  {
		try {
			FilesCDNContainer c = client.getCDNContainerInfo(container);
			return c.getCdnURL() + "/" + name;
		}
		catch (FilesNotFoundException fnfe) {
			// Not enabled
			return null;
		}
		
	}
}
