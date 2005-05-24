package er.extensions;

import java.io.*;

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
    public final static Result[] executeCommandLineCommandsWithEnvVarsInWorkingDir(String[] commands, String[] envp, File dir)
            throws IOException {
        Result[] results = new Result[commands.length];

        for (int i = 0; i < commands.length; i++) {
            results[i] = executeCommandLineCommandWithEnvVarsInWorkingDir(commands[i], envp, dir);
        }

        return results;
    }

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param commands
     *            the commands to execute like "ls -la" or "cp /tmp/file1
     *            /tmp/file2" or "open /Applications/*.app"
     * @param envp
     *            a <code>String</code> array which represents the environment
     *            variables like
     *            <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>,
     *            can be null
     * @param dir
     *            a <code>File</code> object representing the working
     *            directory, can be null
     * @param useOutputFile
     *            defines if the process should write its output to a file which
     *            in turn is read from the filesystem instead of reading
     *            directly from the process output stream
     * 
     * @return the results from the processes that were executed
     * 
     * @exception IOException
     *                if something went wrong
     */
    public final static Result executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(String[] command, String[] envp, File dir,
            boolean writeToOutputFile) throws IOException {
        File outputFile = null;
        if (writeToOutputFile) {
            try {
                outputFile = File.createTempFile("Wonder", ".tmp");
            } catch (IOException e1) {
                throw new NSForwardException(e1, "could not create temp file");
            }
            String[] newEnvp = new String[envp.length + 2];
            System.arraycopy(newEnvp, 0, newEnvp, 0, envp.length);
            newEnvp[newEnvp.length - 2] = ">";
            newEnvp[newEnvp.length - 2] = outputFile.getAbsolutePath();
        }

        Runtime rt = Runtime.getRuntime();
        Process p = null;
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

            // wait for the result of identify
            try {
                p.waitFor();

            } catch (InterruptedException ex) {
            }

            if (writeToOutputFile) {
                response.append( ERXStringUtilities.stringWithContentsOfFile(outputFile) );
            } else {
                response.append( ERXFileUtilities.stringFromInputStream(p.getInputStream()));
            }

        } finally {
            if (p != null) {

                InputStream is = p.getErrorStream();
                if (is != null) error = ERXFileUtilities.stringFromInputStream(is);

                if (log.isDebugEnabled()) {
                    log.debug("response = " + response);
                    log.debug("error = " + error);
                }
                freeProcessResources(p);
            }
            if (outputFile != null) outputFile.delete();
        }
        return new Result(response.toString(), error);

    }

    /**
     * Excecutes the specified command line commands. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param commands
     *            the commands to execute like "ls -la" or "cp /tmp/file1
     *            /tmp/file2" or "open /Applications/*.app"
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
    public final static Result executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(String[] command, String[] envp, File dir)
            throws IOException {
        return executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(command, envp, dir, false);
    }

    /**
     * Excecutes the specified command line command. If envp is not null the
     * environment variables are set before executing the command.
     * 
     * @param command
     *            the command to execute like "ls -la" or "cp /tmp/file1
     *            /tmp/file2" or "open /Applications/*.app"
     * @param envp
     *            a <code>String</code> array which represents the environment
     *            variables like
     *            <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>,
     *            can be null
     * @param dir
     *            a <code>File</code> object representing the working
     *            directory, can be null
     * 
     * @return the result from the process that was executed
     * 
     * @exception IOException
     *                if something went wrong
     */
    public final static Result executeCommandLineCommandWithEnvVarsInWorkingDir(String command, String[] envp, File dir) throws IOException {
        String[] commands = new String[1];
        commands[0] = command;
        return executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(commands, envp, dir);
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
                if (p.getOutputStream() != null) p.getOutputStream().close();
                if (p.getErrorStream() != null) p.getErrorStream().close();
                p.destroy();
            } catch (IOException e) {
            }
        }
    }

    public static String escapePath(String path) {
        path = ERXStringUtilities.replaceStringByStringInString(" ", "\\ ", path);
        return path;
    }

    public static class Result {

        public String response, error;

        public Result(String response, String error) {
            this.response = response;
            this.error = error;
        }
    }
}
