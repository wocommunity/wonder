package er.extensions;

import java.io.File;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Allows you to store a large amount of files and folders without the hassles
 * of directory size limitations. The files are stored by an abstract 
 * "key" which is by default a {@link ERXRandomGUID}. Under the root
 * directory, there will be directories for the first two characters 
 * and under these again directories for the next two characters of the 
 * GUID. <br />
 * 
 * @author ak (original version by Dominik Westner)
 *
 */
public class ERXFileRepository {
	
	protected File _root;
	
	/**
	 * Private constructor, as we use the factory methods to create instances.
	 * @param root
	 */
	protected ERXFileRepository(File root) {
		_root = root;
	}
	
	/**
	 * Returns a parent dir for a given ID. Will create the
	 * directory if it doesn't exist.
	 * @param id
	 */
	private File getDir(String id) {
		File dir = new File(getRoot(), id.substring(0, 2) + File.separator + id.substring(2, 4));
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	/**
	 * Returns a new file in the repository. As you can also create  directories
	 * it does not really create the file, only the path to the file. You can
	 * use file.getName() to get at the GUID which is the key to the file. 
	 */
	public File createFile() {
		return getFile(ERXRandomGUID.newGid());
	}
	
	/**
	 * Returns a file for a 
	 * @param id
	 */
	public File getFile(String id) {
		return new File(getDir(id), id);
	}
	
	/**
	 * Returns true if the file is present in the repository and also exists.
	 * @param id
	 */
	public boolean hasFile(String id) {
		return getFile(id).exists();
	}
	
	/**
	 * Returns the root directory for the repository.
	 */
	public File getRoot() {
		return _root;
	}
	
	/**
	 * Holds the repositories.
	 */
	private static final NSMutableDictionary _repositories = ERXMutableDictionary.synchronizedDictionary();

	/**
	 * Returns the repository that is registered under the given name.
	 * @param name
	 */
	public static ERXFileRepository respository(String name) {
		return (ERXFileRepository) _repositories.objectForKey(name);
	}

	/**
	 * Adds a repository under the given name and the given root directory.
	 * @param name
	 * @param root
	 */
	public static ERXFileRepository addRepository(String name, File root) {
		return addRepository(name, new ERXFileRepository(root));
	}

	/**
	 * Adds a repository under the given name and the given root directory.
	 * @param name
	 * @param repository
	 */
	public static ERXFileRepository addRepository(String name, ERXFileRepository repository) {
		_repositories.setObjectForKey(repository, "name");
		return repository;
	}
}
