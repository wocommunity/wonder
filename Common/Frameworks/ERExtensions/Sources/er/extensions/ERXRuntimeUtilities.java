package er.extensions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSelector;
import com.webobjects.jdbcadaptor.JDBCAdaptorException;

public class ERXRuntimeUtilities {

    /** logging support */
    public static Logger log = Logger
            .getLogger(ERXRuntimeUtilities.class);
    
    /**
     * Returns a dictionary with useful stuff.
     * @param e
     */
    public static NSMutableDictionary informationForException(Exception e) {
		NSMutableDictionary extraInfo = new NSMutableDictionary();
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

    public static NSMutableDictionary informationForBundles() {
    	NSMutableDictionary extraInfo = new NSMutableDictionary();
    	NSMutableDictionary bundleVersions = new NSMutableDictionary();
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

    public static NSMutableDictionary informationForContext(WOContext context) {
		NSMutableDictionary extraInfo = new NSMutableDictionary();
		if (context != null && context.page() != null) {
			extraInfo.setObjectForKey(context.page().name(), "CurrentPage");
			if (context.component() != null) {
				extraInfo.setObjectForKey(context.component().name(), "CurrentComponent");
				if (context.component().parent() != null) {
					extraInfo.setObjectForKey(ERXWOContext.componentPath(context), "CurrentComponentHierarchy");
				}
			}
			if(context.request() != null) {
				extraInfo.setObjectForKey(context.request().uri(), "URL");
				if(context.request().headers() != null) {
					NSMutableDictionary<String, Object> headers = new NSMutableDictionary<String, Object>();
					for (Object key : context.request().headerKeys()) {
						headers.setObjectForKey(context.request().headerForKey(key), key.toString());
					}
					extraInfo.setObjectForKey(headers, "Headers");
				}
			}
			NSSelector d2wSelector = new NSSelector("d2wContext");
			if (d2wSelector.implementedByObject(context.page())) {
				try {
					NSKeyValueCoding c = (NSKeyValueCoding) d2wSelector.invoke(context.page());
					if (c != null) {
						String pageConfiguration = (String) c.valueForKey("pageConfiguration");
						if (pageConfiguration != null) {
							extraInfo.setObjectForKey(pageConfiguration, "D2W-PageConfiguration");
						}
						String propertyKey = (String) c.valueForKey("propertyKey");
						if (propertyKey != null) {
							extraInfo.setObjectForKey(propertyKey, "D2W-PropertyKey");
						}
						NSArray displayPropertyKeys = (NSArray) c.valueForKey("displayPropertyKeys");
						if (displayPropertyKeys != null) {
							extraInfo.setObjectForKey(displayPropertyKeys, "D2W-DisplayPropertyKeys");
						}
					}
				}
				catch (Exception ex) {
				}
			}
			if (context.hasSession() && context.session().statistics() != null) {
				extraInfo.setObjectForKey(context.session().statistics(), "PreviousPageList");
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
    	Throwable throwable = null;
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
     * @param commands
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
     * @param commands
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
     *            <code>timeout</code> seconds then this method throws an
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
        File outputFile = null;
        int exitValue = -1;
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        StreamReader isr = null;
        StreamReader esr = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Will execute command " +  new NSArray(command).componentsJoinedByString(" "));
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
            if (isr.getException() != null) {
                log.error("input stream reader got exception,\n      "+
                        "command = "+ERXStringUtilities.toString(command, " ")+
                        "result = "+isr.getResultAsString(), isr.getException());
            }
            if (esr.getException() != null) {
                log.error("error stream reader got exception,\n      "+
                        "command = "+ERXStringUtilities.toString(command, " ")+
                        "result = "+esr.getResultAsString(), esr.getException());
            }

        } finally {
            freeProcessResources(p);

            if (outputFile != null)
                outputFile.delete();
        }
        return new Result(exitValue, isr.getResult(), esr.getResult());

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

        public void run() {
            try {
                _p.exitValue();
            } catch (IllegalThreadStateException e) {
                _didTimeout = true;
                _p.destroy();
            }
        }
    }
}
