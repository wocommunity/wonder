package er.extensions;

import java.io.*;


public class ERXRuntimeUtilities {

    /** logging support */
    public static ERXLogger log = ERXLogger.getERXLogger(ERXRuntimeUtilities.class);
    

    /** Excecutes the specified command line commands. If envp is not null the  environment variables are set
    * before executing the command.
    *
    * @param commands the commands to execute like "ls -la" or "cp /tmp/file1 /tmp/file2" or "open /Applications/*.app"
     * @param envp a <code>String</code> array which represents the environment variables like
     * <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>, can be null
     * @param dir a <code>File</code> object representing the working directory, can be null
     *
     * @return the results from the processes that were executed
     *
     * @exception IOException if something went wrong
     */
    public final static Result[] executeCommandLineCommandsWithEnvVarsInWorkingDir(String[] commands, String[] envp, File dir) throws IOException {
        Result[] results = new Result[commands.length];

        for (int i = 0; i < commands.length; i++) {
            results[i] = executeCommandLineCommandWithEnvVarsInWorkingDir(commands[i], envp, dir);
        }

        return results;
    }

    /** Excecutes the specified command line commands. If envp is not null the  environment variables are set
     * before executing the command.
     *
     * @param commands the commands to execute like "ls -la" or "cp /tmp/file1 /tmp/file2" or "open /Applications/*.app"
     * @param envp a <code>String</code> array which represents the environment variables like
     * <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>, can be null
     * @param dir a <code>File</code> object representing the working directory, can be null
     *
     * @return the results from the processes that were executed
     *
     * @exception IOException if something went wrong
     */
    public final static Result executeCommandLineCommandWithArgumentsWithEnvVarsInWorkingDir(String[] command, String[] envp, File dir) throws IOException {

    	
    	Runtime rt = Runtime.getRuntime();
    	Process p = null;
    	StringBuffer response = new StringBuffer();
    	String input = "";
    	String error = "";
    	try {
    		if (log.isDebugEnabled()) {
    			log.debug("will execute command "+command);
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

    		//wait for the result of identify
    		try {
    			p.waitFor();

    		} catch (InterruptedException ex) {}

    		byte[] b = new byte[100];

    		int len = 0;
    		InputStream is = p.getInputStream();

    		while ((len = is.read(b)) != -1) {
    			byte[] res = new byte[len];
    			System.arraycopy(b, 0, res, 0, len);
    			String s = new String(res);
    			response.append(s);
    		}
    		
    	} finally {
    		if (p != null) {
    			InputStream is = p.getInputStream();
    			if (is!=null) input = ERXFileUtilities.stringFromInputStream(is);
    			is = p.getErrorStream();
    			if (is!=null) error = ERXFileUtilities.stringFromInputStream(is);

    			if (log.isDebugEnabled()) {
    				log.debug("input = "+input);
    				log.debug("error = "+error);
    			}
    			freeProcessResources(p);
    		}
    	}
    	return new Result(response.toString(), input, error);

    }

    /** Excecutes the specified command line command. If envp is not null the  environment variables are set
    * before executing the command.
    *
    * @param command the command to execute like "ls -la" or "cp /tmp/file1 /tmp/file2" or "open /Applications/*.app"
     * @param envp a <code>String</code> array which represents the environment variables like
     * <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>, can be null
     * @param dir a <code>File</code> object representing the working directory, can be null
     *
     * @return the result from the process that was executed
     *
     * @exception IOException if something went wrong
     */
    public final static Result executeCommandLineCommandWithEnvVarsInWorkingDir(String command, String[] envp, File dir) throws IOException {

        Runtime rt = Runtime.getRuntime();
        Process p = null;
        StringBuffer response = new StringBuffer();
        String input = "";
        String error = "";
        try {
            if (log.isDebugEnabled()) {
                log.debug("will execute command "+command);
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

            //wait for the result of identify
            try {
                p.waitFor();

            } catch (InterruptedException ex) {}

            byte[] b = new byte[100];

            int len = 0;
            InputStream is = p.getInputStream();

            while ((len = is.read(b)) != -1) {
                byte[] res = new byte[len];
                System.arraycopy(b, 0, res, 0, len);
                String s = new String(res);
                response.append(s);
            }
            
        } finally {
            if (p != null) {
                InputStream is = p.getInputStream();
                if (is!=null) input = ERXFileUtilities.stringFromInputStream(is);
                is = p.getErrorStream();
                if (is!=null) error = ERXFileUtilities.stringFromInputStream(is);

                if (log.isDebugEnabled()) {
                    log.debug("input = "+input);
                    log.debug("error = "+error);
                }
                freeProcessResources(p);
            }
        }
        return new Result(response.toString(), input, error);

    }

    /**
    * Frees all of a resources associated with a given
     * process and then destroys it.
     * @param p process to destroy
     */
    public static void freeProcessResources(Process p) {
        if (p!=null) {
            try {
                if (p.getInputStream()!=null) p.getInputStream().close();
                if (p.getOutputStream()!=null) p.getOutputStream().close();
                if (p.getErrorStream()!=null) p.getErrorStream().close();
                p.destroy();
            } catch (IOException e) {}
        }
    }

    public static String escapePath(String path) {
        path = ERXStringUtilities.replaceStringByStringInString(" ", "\\ ", path);
        return path;
    }

    public static class Result {
        public String response, input, error;
        
        public Result(String response, String input, String error) {
            this.response = response;
            this.input = input;
            this.error = error;
        }
    }
}
