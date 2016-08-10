package er.indexing.storage;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEOControlUtilities;

public class ERIDirectory extends _ERIDirectory {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERIDirectory.class);

	public static final ERIDirectoryClazz clazz = new ERIDirectoryClazz();

	private class LockingDirectory extends Directory {

		private Directory _wrapped;

		LockingDirectory(Directory wrapped) {
			_wrapped = wrapped;
			setLockFactory(_wrapped.getLockFactory());
		}

		@Override
		public void close() throws IOException {
			editingContext().lock();
			try {
				_wrapped.close();
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public IndexOutput createOutput(String name) throws IOException {
			editingContext().lock();
			try {
				return _wrapped.createOutput(name);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public void deleteFile(String name) throws IOException {
			editingContext().lock();
			try {
				_wrapped.deleteFile(name);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public boolean fileExists(String name) throws IOException {
			editingContext().lock();
			try {
				return _wrapped.fileExists(name);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public long fileLength(String name) throws IOException {
			editingContext().lock();
			try {
				return _wrapped.fileLength(name);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public long fileModified(String name) throws IOException {
			editingContext().lock();
			try {
				return _wrapped.fileModified(name);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public String[] list() throws IOException {
			editingContext().lock();
			try {
				return _wrapped.list();
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public IndexInput openInput(String name) throws IOException {
			editingContext().lock();
			try {
				return _wrapped.openInput(name);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public void renameFile(String from, String to) throws IOException {
			editingContext().lock();
			try {
				_wrapped.renameFile(from, to);
			} finally {
				editingContext().unlock();
			}
		}

		@Override
		public void touchFile(String name) throws IOException {
			editingContext().lock();
			try {
				_wrapped.touchFile(name);
			} finally {
				editingContext().unlock();
			}
		}

	}

	private class EOFDirectory extends Directory {

		private class EOFLockFactory extends LockFactory {

			private class EOFLock extends Lock {

				private String _name;

				private EOFLock(String name) {
					_name = name;
				}

				@Override
				public boolean isLocked() {
					editingContext().lock();
					try {
						return fileForName(name()) != null;
					} finally {
						editingContext().unlock();
					}
				}

				@Override
				public boolean obtain() throws IOException {
				    editingContext().lock();
				    try {
				        return createFile(name()) != null;
				    } finally {
				        editingContext().unlock();
				    }
				}

				@Override
				public void release() throws IOException {
                    editingContext().lock();
                    try {
                        deleteFile(name());
                    } finally {
                        editingContext().unlock();
                    }
				}

				public String name() {
					return _name;
				}
			}

			private NSMutableDictionary<String, EOFLock> locks = new NSMutableDictionary();

			@Override
			public void clearLock(String s) throws IOException {
				synchronized (locks) {
					locks.removeObjectForKey(s);
				}
			}

			@Override
			public Lock makeLock(String s) {
				synchronized (locks) {
					EOFLock lock = new EOFLock(s);
					locks.setObjectForKey(lock, s);

					return lock;
				}
			}

		}

		public EOFDirectory() {
			setLockFactory(new EOFLockFactory());
		}

		@Override
		public void close() throws IOException {
			editingContext().saveChanges();
		}

		@Override
		public IndexOutput createOutput(String s) throws IOException {
			log.debug("createOutput: {}", s);
			ERIFile file = fileForName(s);
			if (file == null) {
				file = createFile(s);
			}
			return fileForName(s).createOutput();
		}

		public ERIFile createFile(String s) {
			ERIFile file;
			file = ERIFile.clazz.createAndInsertObject(editingContext());
			file.setName(s);
			file.setDirectory(ERIDirectory.this);
			addToFiles(file);
			editingContext().saveChanges();
			return file;
		}

		@Override
		public void deleteFile(String s) throws IOException {
			log.debug("deleteFile: {}", s);
			ERIFile file = fileForName(s);
			if (file != null) {
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
			return ((NSArray<String>) files().valueForKeyPath(ERIFile.Key.NAME)).toArray(new String[0]);
		}

		@Override
		public IndexInput openInput(String s) throws IOException {
			ERIFile file = fileForName(s);
			if (file == null) {
				throw new IOException("File not found: " + s);
			}
			try {
				return file.openInput();
			} catch (Exception ex) {
				ERXEOControlUtilities.refaultObject(ERIDirectory.this);
				ERXEOControlUtilities.clearSnapshotForRelationshipNamed(ERIDirectory.this, Key.FILES);
				throw new IOException("File not found: " + s);
			}
		}

		@Override
		public void renameFile(String s, String s1) throws IOException {
			fileForName(s).setName(s1);
			editingContext().saveChanges();
		}

		@Override
		public void touchFile(String s) throws IOException {
			ERIFile file = fileForName(s);
			if (file == null) {
				file = createFile(s);
			}
			file.touch();
			editingContext().saveChanges();
		}
	}

	public static class ERIDirectoryClazz extends _ERIDirectory._ERIDirectoryClazz {

		public Directory directoryForName(EOEditingContext ec, String store) {
			ERIDirectory directory = objectMatchingKeyAndValue(ec, Key.NAME, store);
			if (directory == null) {
				directory = createAndInsertObject(ec);
				directory.setName(store);
				ec.saveChanges();
			}
			return directory.directory();
		}

	}

	public interface Key extends _ERIDirectory.Key {
	}

	@Override
	public void init(EOEditingContext ec) {
		super.init(ec);
	}

	public ERIFile fileForName(String name) {
		for (ERIFile file : files()) {
			if (file.name().equals(name)) {
				return file;
			}
		}
		return null;
	}

	private Directory _directory;

	public Directory directory() {

		if (_directory == null) {
			_directory = new LockingDirectory(new EOFDirectory());
		}
		return _directory;
	}
}
