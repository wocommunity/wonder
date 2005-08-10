package er.extensions;

import java.io.*;
import java.util.*;

import com.sun.rsasign.*;
import com.webobjects.foundation.*;

public class ERXRuntimeUtilities {

    /** logging support */
    public static ERXLogger log = ERXLogger.getERXLogger(ERXRuntimeUtilities.class);

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param commands
     *            the commands to execute like "ls -la" or "cp /tmp/file1
     *            /tmp/file2" or "open /Applications/*.app"
     *            new String[]{"ls", "-la"}
     *            new String[]{"cp", "/tmp/file1", "/tmp/file2"}
     *            new String[]{"open", "/Applications/*.app"}
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
    public final static Result executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(String[] command, String[] envp, File dir) throws IOException {
        try {
            return execute(command, envp, dir, 0);
        } catch (ERXTimeoutException e) {
            // this will never happen so we can return null here.
            return null;
        }
    }

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param commands
     *            the commands to execute, this is an String array with two dimensions
     *            the following commands <br>
     *            "ls -la" or "cp /tmp/file1 /tmp/file2" or "open /Applications/*.app"<br>
     *            would be as String arrays<br>
     *            <pre>new String[]{
     *              new String[] {"ls", "-la"},
     *              new String[] {"cp", "/tmp/file1", "/tmp/file2"},
     *              new String[] {"open", "/Applications/*.app"}
     *            }
     *            </pre>
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
    public final static Result[] executeCommandLineCommandsWithEnvVarsInWorkingDir(String[][] commands, String[] envp, File dir)
            throws IOException {
        Result[] results = new Result[commands.length];

        for (int i = 0; i < commands.length; i++) {
            try {
                results[i] = execute(commands[i], envp, dir, 0);
            } catch (ERXTimeoutException e) {
                // will never happen
                return null;
            }
        }

        return results;
    }

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command. This method supports
     * timeout's. This is quite important because its -always- possible that a UNIX or WINDOWS
     * process does not return, even with simple shell scripts. This is due to whatever bugs
     * and hence every invocation of <code>Process.waitFor()</code> should be observed and stopped
     * if a certain amount of time is over.
     * 
     * @param commands
     *            the commands to execute, this is an String array with two dimensions
     *            the following commands <br>
     *            "ls -la" or "cp /tmp/file1 /tmp/file2" or "open /Applications/*.app"<br>
     *            would be as String arrays<br>
     *            <pre>new String[]{
     *              new String[] {"ls", "-la"},
     *              new String[] {"cp", "/tmp/file1", "/tmp/file2"},
     *              new String[] {"open", "/Applications/*.app"}
     *            }
     *            </pre>
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
     *            a <code>long</code> which can be either <code>0</code> indicating this
     *            method call waits until the process exits or any <code>long</code> number
     *            larger than <code>0</code> which means if the process does not exit after 
     *            <code>timeout</code> seconds then this method throws an ERXTimeoutException
     *            
     * 
     * @return the results from the processes that were executed
     * 
     * @exception IOException
     *                if something went wrong
     */
    public final static Result execute(String[] command, String[] envp, File dir, long timeout)
            throws IOException, ERXTimeoutException {
        File outputFile = null;

        Runtime rt = Runtime.getRuntime();
        Process p = null;
        int exitValue = 0;
        StringBuffer response = new StringBuffer();
        String input = "";
        String error = "";
        try {
            if (log.isDebugEnabled()) {
                log.debug("will execute command " + command);
            }
            if (dir == null && envp == null) {
                p = rt.exec(command);

            } else if (dir == null) {
                p = rt.exec(command, envp);

            } else if (envp == null) {
                throw new IllegalArgumentException("if dir != null then envp must also be != null");

            } else {
                p = rt.exec(command, envp, dir);
            }

            if (timeout > 0) {
                final Process pp = p;
                TimeoutTimerTask task = new TimeoutTimerTask(p);
                Timer timer = new Timer();
                timer.schedule(task, timeout);
                try {
                    p.waitFor();
                } catch (InterruptedException ex) {
                }
                timer.cancel();
                if (task.hasTimeout()) {
                    throw new ERXTimeoutException("process did't exit after " + timeout + " milliseconds");
                }
            } else {
                // wait for the result of the process
                try {
                    p.waitFor();
                } catch (InterruptedException ex) {
                }
            }
            response.append(ERXFileUtilities.stringFromInputStream(p.getInputStream()));

        } finally {

            try {
                if (p != null && exitValue == 0) {
                    InputStream is = p.getErrorStream();
                    if (is != null) error = ERXFileUtilities.stringFromInputStream(is);
    
                    if (log.isDebugEnabled()) {
                        log.debug("response = " + response);
                        log.debug("error = " + error);
                    }
                    freeProcessResources(p);
                }
            } catch (NullPointerException e) {
                // p was null
            }

            if (outputFile != null) outputFile.delete();
        }
        return new Result(response.toString(), error);

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
                if (p.getInputStream() != null) p.getInputStream().close();
            } catch (IOException e) {
                // do nothing here
            }
            if (p.getOutputStream() != null) try {
                p.getOutputStream().close();
            } catch (IOException e) {
                // do nothing here
            }
            if (p.getErrorStream() != null) try {
                p.getErrorStream().close();
            } catch (IOException e) {
                // do nothing here
            }
            p.destroy();
        }
    }

    public static class Result {

        public String response, error;

        public Result(String response, String error) {
            this.response = response;
            this.error = error;
        }
    }

    public static class ERXTimeoutException extends Exception {

        public ERXTimeoutException(String string) {
            super(string);
        }
    }

    public static class TimeoutTimerTask extends TimerTask {
        Process p;
        boolean hasTimeout = false;
        
        public TimeoutTimerTask(Process p) {
            this.p = p;
        }
        public boolean hasTimeout() {
            return hasTimeout;
        }
        public void run() {
            try {
                p.exitValue();
            } catch (IllegalThreadStateException e) {
                hasTimeout = true;
                p.destroy();
            }
        }   
    }
}
