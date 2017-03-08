package er.extensions.foundation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

import er.extensions.appserver.ERXWOContext;

/**
 * Collection of utilities dealing with threads and processes.
 * 
 *
 * @author ak
 * @author david
 */
public class ERXRuntimeUtilities {
    private static final Logger log = LoggerFactory.getLogger(ERXRuntimeUtilities.class);
    
    /**
     * Hack to create a bundle after the app is loaded. Useful for the insistence of EOF on JavaXXXAdaptor bundles. 
     * @param name
     * @return a new bundle under the system temp directory
     */
    public static NSBundle createBundleIfNeeded(String name) {
    	File sysTempDir = new File(System.getProperty("java.io.tmpdir", "/tmp"));
    	
    	File newTempDir;
        final int maxAttempts = 5;
        int attemptCount = 0;
        do {
            attemptCount++;
            if(attemptCount > maxAttempts)
            {
            	throw NSForwardException._runtimeExceptionForThrowable(new IOException(
                        "The highly improbable has occurred! Failed to " +
                        "create a unique temporary directory after " +
                        maxAttempts + " attempts."));
            }
            // create unique dir in tmp to work with
            String dirName = name + UUID.randomUUID().toString();
            newTempDir = new File(sysTempDir, dirName);
            
        } while (newTempDir.exists());
        
        if (newTempDir.mkdirs()) {
        	// create basic framework bundle structure
        	File fwkResourcesDir = new File(new File(newTempDir , name + ".framework"), "Resources");
        	File fwkJavaDir = new File(fwkResourcesDir, "Java");
        	fwkJavaDir.mkdirs();
        	
        	try {
    			ERXFileUtilities.stringToFile("{Has_WOComponents=NO;}", new File(fwkResourcesDir, "Info.plist"));
    		}
    		catch (IOException e) {
    			throw NSForwardException._runtimeExceptionForThrowable(e);
    		}
    		return loadBundleIfNeeded(fwkJavaDir);
        }
        throw NSForwardException._runtimeExceptionForThrowable(new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath()));
    }
    
    /**
     * Load an application, framework or jar bundle if not already loaded.
     * 
     * @param bundleFile - the directory or archive (e.g., jar, war) of the bundle to load
     * @return the bundle found at the given uri.
     * @throws NSForwardException if bundle loading fails
     */
    public static NSBundle loadBundleIfNeeded(File bundleFile) {
    	try {
    		String canonicalPath = bundleFile.getCanonicalPath();
    		boolean isJar = bundleFile.isFile() && canonicalPath.endsWith(".jar");
			return NSBundle._bundleWithPathShouldCreateIsJar(canonicalPath, true, isJar);
		}
		catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
    }
    
    /**
     * Returns a dictionary with useful stuff.
     * @param e
     */
    public static NSMutableDictionary<String, Object> informationForException(Exception e) {
		NSMutableDictionary<String, Object> extraInfo = new NSMutableDictionary<>();
		if (e instanceof EOGeneralAdaptorException) {
			// AK NOTE: you might have sensitive info in your failed ops...
			NSDictionary dict = ((EOGeneralAdaptorException) e).userInfo();
			if (dict != null) {
				Object value;
				// this one is a little bit heavyweight...
				// value = NSPropertyListSerialization.stringFromPropertyList(dict);
				value = dict.objectForKey(EODatabaseContext.FailedDatabaseOperationKey);
				if (value != null) {
					extraInfo.setObjectForKey(value.toString(), EODatabaseContext.FailedDatabaseOperationKey);
				}
				value = dict.objectForKey(EOAdaptorChannel.AdaptorFailureKey);
				if (value != null) {
					extraInfo.setObjectForKey(value.toString(), EOAdaptorChannel.AdaptorFailureKey);
				}
				value = dict.objectForKey(EOAdaptorChannel.FailedAdaptorOperationKey);
				if (value != null) {
					extraInfo.setObjectForKey(value.toString(), EOAdaptorChannel.FailedAdaptorOperationKey);
				}
				if (e instanceof JDBCAdaptorException) {
					value = ((JDBCAdaptorException) e).sqlException();
					if (value != null) {
						extraInfo.setObjectForKey(value.toString(), "SQLException");
					}
				}
			}
		}
		return extraInfo;
    }

    public static NSMutableDictionary<String, Object> informationForBundles() {
    	NSMutableDictionary<String, Object> extraInfo = new NSMutableDictionary<>();
    	NSMutableDictionary<String, Object> bundleVersions = new NSMutableDictionary<String, Object>();
    	for (Enumeration bundles = NSBundle._allBundlesReally().objectEnumerator(); bundles.hasMoreElements();) {
    		NSBundle bundle = (NSBundle) bundles.nextElement();
    		String version = ERXProperties.versionStringForFrameworkNamed(bundle.name());
    		if(version == null) {
    			version = "No version provided";
    		}
    		bundleVersions.setObjectForKey(version, bundle.name());
    	}
    	extraInfo.setObjectForKey(bundleVersions, "Bundles");
    	return extraInfo;
    }

    public static NSMutableDictionary<String, Object> informationForContext(WOContext context) {
    	NSMutableDictionary<String, Object> extraInfo = new NSMutableDictionary<>();
    	if (context != null) {
    		if(context.page() != null) {
    			extraInfo.setObjectForKey(context.page().name(), "CurrentPage");
    			if (context.component() != null) {
    				extraInfo.setObjectForKey(context.component().name(), "CurrentComponent");
    				if (context.component().parent() != null) {
    					extraInfo.setObjectForKey(ERXWOContext.componentPath(context), "CurrentComponentHierarchy");
    				}
    			}
				// If this is a D2W component, get its D2W-related information from ERDirectToWeb.
				NSSelector d2wSelector = new NSSelector("d2wContext");
				if (d2wSelector.implementedByObject(context.page())) {
					try {
						Class erDirectToWebClazz = Class.forName("er.directtoweb.ERDirectToWeb");
						NSSelector infoSelector = new NSSelector("informationForContext", new Class [] {WOContext.class});
						NSDictionary d2wExtraInfo = (NSDictionary)infoSelector.invoke(erDirectToWebClazz, context);
						extraInfo.addEntriesFromDictionary(d2wExtraInfo);
					} catch (Exception e) {
					}
				}
			}
    		if(context.request() != null) {
				extraInfo.setObjectForKey(context.request().uri(), "URL");
				if(context.request().headers() != null) {
					NSMutableDictionary<String, Object> headers = new NSMutableDictionary<>();
					for (Object key : context.request().headerKeys()) {
						String value = context.request().headerForKey(key);
						if(value != null) {
							headers.setObjectForKey(value, key.toString());
						}
					}
					extraInfo.setObjectForKey(headers, "Headers");
				}
			}
			if (context.hasSession()) {
				if(context.session().statistics() != null) {
					extraInfo.setObjectForKey(context.session().statistics(), "PreviousPageList");
				}
				extraInfo.setObjectForKey(context.session(), "Session");
			}
		}
		return extraInfo;
    }
    
    /**
     * Retrieves the actual cause of an error by unwrapping them as far as possible, 
     * i.e. NSForwardException.originalThrowable(), InvocationTargetException.getTargetException() 
     * or Exception.getCause() are regarded as actual causes.
     */

    public static Throwable originalThrowable(Throwable t) {
    	if (t instanceof InvocationTargetException) {
    		return originalThrowable(((InvocationTargetException)t).getTargetException());
    	} 
    	if (t instanceof NSForwardException) {
    		return originalThrowable(((NSForwardException)t).originalException());
    	}
       	if (t instanceof JDBCAdaptorException) {
       		JDBCAdaptorException ex = (JDBCAdaptorException)t; 
    		if(ex.sqlException() != null) {
    			return originalThrowable(ex.sqlException());
    		}
    	} 
       	if (t instanceof SQLException) {
    		SQLException ex = (SQLException)t; 
    		if(ex.getNextException() != null) {
    			return originalThrowable(ex.getNextException());
    		}
    	} 
    	if (t instanceof Exception) {
    		Exception ex = (Exception)t; 
    		if(ex.getCause() != null) {
    			return originalThrowable(ex.getCause());
    		}
    	}
    	return t;
    }

    
    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param command
     *            the commands to execute like "ls -la" or "cp /tmp/file1
     *            /tmp/file2" or "open /Applications/*.app" new String[]{"ls",
     *            "-la"} new String[]{"cp", "/tmp/file1", "/tmp/file2"} new
     *            String[]{"open", "/Applications/*.app"}
     * 
     * @param envp
     *            a <code>String</code> array which represents the environment
     *            variables like
     *            <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>,
     *            can be null
     * @param dir
     *            a <code>File</code> object representing the working
     *            directory, can be null
     * 
     * @return the results from the processes that were executed
     * 
     * @exception IOException
     *                if something went wrong
     */
    public final static Result executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(
            String[] command, String[] envp, File dir) throws IOException {
        try {
            return execute(command, envp, dir, 0);
        } catch (TimeoutException e) {
            // this will never happen so we can return null here.
            return null;
        }
    }

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param commands
     *            the commands to execute, this is an String array with two
     *            dimensions the following commands <br>
     *            "ls -la" or "cp /tmp/file1 /tmp/file2" or "open
     *            /Applications/*.app"<br>
     *            would be as String arrays<br>
     * 
     * <pre>
     *     new String[] { 
     *      	new String[] { &quot;ls&quot;, &quot;-la&quot; },
     *      	new String[] { &quot;cp&quot;, &quot;/tmp/file1&quot;, &quot;/tmp/file2&quot; },
     *      	new String[] { &quot;open&quot;, &quot;/Applications/*.app&quot; } 
     *     }
     * </pre>
     * 
     * @param envp
     *            a <code>String</code> array which represents the environment
     *            variables like
     *            <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>,
     *            can be null
     * @param dir
     *            a <code>File</code> object representing the working
     *            directory, can be null
     * 
     * @return the results from the processes that were executed
     * 
     * @exception IOException
     *                if something went wrong
     */
    public final static Result[] executeCommandLineCommandsWithEnvVarsInWorkingDir(
            String[][] commands, String[] envp, File dir) throws IOException {
        Result[] results = new Result[commands.length];

        for (int i = 0; i < commands.length; i++) {
            try {
                results[i] = execute(commands[i], envp, dir, 0);
            } catch (TimeoutException e) {
                // will never happen
                return null;
            }
        }

        return results;
    }

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command. This method
     * supports timeout's. This is quite important because its -always- possible
     * that a UNIX or WINDOWS process does not return, even with simple shell
     * scripts. This is due to whatever bugs and hence every invocation of
     * <code>Process.waitFor()</code> should be observed and stopped if a
     * certain amount of time is over.
     * 
     * @param command
     *            the commands to execute, this is an String array with two
     *            dimensions the following commands <br>
     *            "ls -la" or "cp /tmp/file1 /tmp/file2" or "open
     *            /Applications/*.app"<br>
     *            would be as String arrays<br>
     * 
     * <pre>
     * new String[] { new String[] { &quot;ls&quot;, &quot;-la&quot; },
     *      new String[] { &quot;cp&quot;, &quot;/tmp/file1&quot;, &quot;/tmp/file2&quot; },
     *      new String[] { &quot;open&quot;, &quot;/Applications/*.app&quot; } }
     * </pre>
     * 
     * @param envp
     *            a <code>String</code> array which represents the environment
     *            variables like
     *            <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>,
     *            can be null
     * @param dir
     *            a <code>File</code> object representing the working
     *            directory, can be null
     * 
     * @param timeout
     *            a <code>long</code> which can be either <code>0</code>
     *            indicating this method call waits until the process exits or
     *            any <code>long</code> number larger than <code>0</code>
     *            which means if the process does not exit after
     *            <code>timeout</code> milliseconds then this method throws an
     *            ERXTimeoutException
     * 
     * 
     * @return the results from the processes that were executed
     * 
     * @exception IOException
     *                if something went wrong
     */
    public final static Result execute(String[] command, String[] envp,
            File dir, long timeout) throws IOException, TimeoutException {
        int exitValue = -1;
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        StreamReader isr = null;
        StreamReader esr = null;
        Result result;
		try {
            if (log.isDebugEnabled()) {
                log.debug("Will execute command {}", new NSArray<>(command).componentsJoinedByString(" "));
            }
            if (dir == null && envp == null) {
                p = rt.exec(command);

            } else if (dir == null) {
                p = rt.exec(command, envp);

            } else if (envp == null) {
                throw new IllegalArgumentException(
                        "if dir != null then envp must also be != null");

            } else {
                p = rt.exec(command, envp, dir);
            }
            // DT: we must read from input and error stream in separate threads
            // because if the buffer from these streams are full the process
            // will block!
            isr = new StreamReader(p.getInputStream());
            esr = new StreamReader(p.getErrorStream());

            if (timeout > 0) {
                TimeoutTimerTask task = new TimeoutTimerTask(p);
                Timer timer = new Timer();
                timer.schedule(task, timeout);
                boolean wasStopped = false;
                try {
                    p.waitFor();
                    exitValue = p.exitValue();
                } catch (InterruptedException ex) {
                    wasStopped = true;
                }
                timer.cancel();
                if (task.didTimeout() || wasStopped) {
                    throw new TimeoutException("process didn't exit after " + timeout + " milliseconds");
                }
            } else {
                // wait for the result of the process
                try {
                    p.waitFor();
                    exitValue = p.exitValue();
                } catch (InterruptedException ex) {
                }
            }
        } finally {
        	// Getting stream results before freeing process resources to prevent a case
        	// when fast process is destroyed before stream readers read from buffers.
        	if (isr != null) {
        		if (esr != null) {
                	result = new Result(exitValue, isr.getResult(), esr.getResult());
        		}
        		else {
                	result = new Result(exitValue, isr.getResult(), null);
        		}
        	}
        	else if (esr != null) {
            	result = new Result(exitValue, null, esr.getResult());
        	}
        	else {
            	result = new Result(exitValue, null, null);
        	}

        	// Checking exceptions after getting results to ensure that stream readers
        	// had already read their buffers by the time of check.
        	if (isr != null && isr.getException() != null) {
                log.error("input stream reader got exception,\n\tcommand = {}\n\tresult = {}",
                        ERXStringUtilities.toString(command, " "), isr.getResultAsString(), isr.getException());
            }
            if (esr != null && esr.getException() != null) {
                log.error("error stream reader got exception,\n\tcommand = {}\n\tresult = {}",
                        ERXStringUtilities.toString(command, " "), esr.getResultAsString(), esr.getException());
            }

            freeProcessResources(p);
        }
        return result;

    }

    /**
     * Frees all of a resources associated with a given process and then
     * destroys it.
     * 
     * @param p
     *            process to destroy
     */
    public static void freeProcessResources(Process p) {
        if (p != null) {
            try {
                if (p.getInputStream() != null)
                    p.getInputStream().close();
            } catch (IOException e) {
                // do nothing here
            }
            if (p.getOutputStream() != null)
                try {
                    p.getOutputStream().close();
                } catch (IOException e) {
                    // do nothing here
                }
            if (p.getErrorStream() != null)
                try {
                    p.getErrorStream().close();
                } catch (IOException e) {
                    // do nothing here
                }
            p.destroy();
        }
    }

    public static class StreamReader {
        private byte[] _result = null;
        private boolean _finished = false;
        private IOException _iox;
        
        public StreamReader(final InputStream is) {

            Runnable r = new Runnable() {

                public void run() {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    try {
                        int read = -1;
                        byte[] buf = new byte[1024 * 50];
                        while ((read = is.read(buf)) != -1) {
                            bout.write(buf, 0, read);
                        }
                        _result = bout.toByteArray();
                    } catch (IOException e) {
                        _iox = e;
                        _result =  bout.toByteArray();
                    } finally {
                        synchronized (StreamReader.this) {
                            _finished = true;
                            StreamReader.this.notifyAll();
                        }
                     }
                }

            };
            Thread t = new Thread(r);
            t.start();
        }
        public byte[] getResult() {
            synchronized (this) {
                if(!_finished) {
                    try {
                        StreamReader.this.wait();
                    } catch (InterruptedException e) {
                        throw NSForwardException._runtimeExceptionForThrowable(e);
                    }
                }
            }
            return _result;
        }
        public boolean isFinished() {
            return _finished;
        }
        public IOException getException() {
            return _iox;
        }
        public String getResultAsString() {
            return getResult() == null ? null : new String(getResult());
        }
    }

    public static class Result {

        private byte[] _response, _error;
        private int _exitValue;
        public Result(int exitValue, byte[] response, byte[] error) {
            _exitValue = exitValue;
            _response = response;
            _error = error;
        }
        
        public byte[] getResponse() {
            return _response;
        }
        public byte[] getError() {
            return _error;
        }
        public int getExitValue() {
            return _exitValue;
        }
        public String getResponseAsString() {
            return getResponse() == null ? null : new String(getResponse());
        }
        public String getErrorAsString() {
            return getError() == null ? null : new String(getError());
        }
    }

    public static class TimeoutException extends Exception {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

        public TimeoutException(String string) {
            super(string);
        }
    }

    public static class TimeoutTimerTask extends TimerTask {
        private Process _p;
        private boolean _didTimeout = false;

        public TimeoutTimerTask(Process p) {
            _p = p;
        }

        public boolean didTimeout() {
            return _didTimeout;
        }

        @Override
		public void run() {
            try {
                _p.exitValue();
            } catch (IllegalThreadStateException e) {
                _didTimeout = true;
                _p.destroy();
            }
        }
    }


    private static NSMutableDictionary<Thread, String> flags;

    /**
	 * When you have an inner loop and you want to be able to bail out on a stop
	 * request, call this method and you will get interrupted when another thread wants you to.
	 */
	public static void checkThreadInterrupt() {
		if(flags == null) {
			return;
		}
		synchronized (flags) {
			Thread currentThread = Thread.currentThread();
			if (flags.containsKey(currentThread)) {
				String message = clearThreadInterrupt(currentThread);
				throw NSForwardException._runtimeExceptionForThrowable(new InterruptedException(message));
			}
		}
	}

	/**
	 * Call this to get the thread in question interrupted on the next call to checkThreadInterrupt().
	 * @param thread
	 * @param message
	 */
	public static synchronized void addThreadInterrupt(Thread thread, String message) {
		if(flags == null) {
			flags = new NSMutableDictionary<>();
		}
		synchronized (flags) {
			if (!flags.containsKey(thread)) {
				log.debug("Adding thread interrupt request: {}", message, new RuntimeException());
				flags.setObjectForKey(message, thread);
			}
		}
	}

	/**
	 * Clear the interrupt flag for the thread.
	 * @param thread
	 */
	public static synchronized String clearThreadInterrupt(Thread thread) {
		if(flags == null) {
			return null;
		}
		synchronized (flags) {
			return flags.removeObjectForKey(thread);
		}
	}

}
