package er.extensions.foundation;

import java.io.File;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Allows you to store a large amount of files and folders without the hassles
 * of directory size limitations. The files are stored by an abstract 
 * "key" which is by default a {@link ERXRandomGUID}.<br />
 * It uses a factory to create the folder structure under the root
 * directory. With the default factory there will be directories for the first 
 * two characters and under these again directories for the next two characters 
 * of the GUID. <br />
 * 
 * @author ak (original version by Dominik Westner)
 *
 */
public class ERXFileRepository {
	
	protected File _root;
	protected final Factory _factory;
	
	/**
	 * Private constructor, as we use the factory methods to create instances.
	 * @param root
	 * @param factory 
	 */
	protected ERXFileRepository(File root, Factory factory) {
		_root = root;
		_factory = factory;
	}
	
	/**
	 * Returns a new file in the repository. As you can also create  directories
	 * it does not really create the file, only the path to the file. You can
	 * use file.getName() to get at the GUID which is the key to the file. 
	 * @return the new file
	 */
	public File createFile() {
		return getFile(ERXRandomGUID.newGid());
	}
	
	/**
	 * Returns a file for a given id
	 * @param id the id of the file
	 * @return a file for the given id
	 */
	public File getFile(String id) {
		return new File(_factory.getDir(getRoot(), id), id);
	}
	
	/**
	 * Returns true if the file is present in the repository and also exists.
	 * @param id the id of the file
	 * @return true if the file exists in the repository
	 */
	public boolean hasFile(String id) {
		return getFile(id).exists();
	}
	
	/**
	 * Returns the root directory for the repository.
	 * @return the root directory for the repository
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
	 * @param name the name under which the respository is registered
	 * @return the repository that is registered under the given name or null
	 * @deprecated use {@link #repository(String)} instead
	 */
	@Deprecated
	public static ERXFileRepository respository(String name) {
		return (ERXFileRepository) _repositories.objectForKey(name);
	}

	/**
	 * Returns the repository that is registered under the given name.
	 * @param name the name under which the repository is registered
	 * @return the repository that is registered under the given name or null
	 */
	public static ERXFileRepository repository(String name) {
		return (ERXFileRepository) _repositories.objectForKey(name);
	}

	/**
	 * Adds a repository under the given name and the given root directory that uses the default factory.
	 * @param name the name under which the repository should be added
	 * @param root the root directory of the repository
	 * @return the new repository
	 */
	public static ERXFileRepository addRepository(String name, File root) {
		return addRepository(name, new ERXFileRepository(root, new DefaultFactory()));
	}

	/**
	 * Adds a repository under the given name and the given root directory that uses the given factory.
	 * @param name the name under which the repository should be added
	 * @param root the root directory of the repository
	 * @param factory the factory to determine the directory from the id for this repository
	 * @return the new repository
	 */
	public static ERXFileRepository addRepository(String name, File root, Factory factory) {
		return addRepository(name, new ERXFileRepository(root, factory));
	}

	/**
	 * Adds an existing repository under the given name.
	 * @param name the name under which the repository should be added
	 * @param repository the repository to register
	 * @return the repository
	 */
	public static ERXFileRepository addRepository(String name, ERXFileRepository repository) {
		_repositories.setObjectForKey(repository, name);
		return repository;
	}
	
	public static interface Factory {
		public File getDir(File root, String id);
	}

	/** Default implementation of the Factory interface. */
	public static class DefaultFactory implements Factory {
		/**
		 * Returns a parent dir for a given ID. Will create the
		 * directory if it doesn't exist.
		 * @param root
		 * @param id
		 */
		public File getDir(File root, String id) {
			File dir = new File(root, id.substring(0, 2) + File.separator + id.substring(2, 4));
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return dir;
		}
	}
}
