package er.extensions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class ERXRuntimeUtilities {



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
    public final static String[] executeCommandLineCommandsWithEnvVarsInWorkingDir(String[] commands, String[] envp, File dir) throws IOException {
        String[] results = new String[commands.length];

        for (int i = 0; i < commands.length; i++) {
            results[i] = executeCommandLineCommandWithEnvVarsInWorkingDir(commands[i], envp, dir);
        }

        return results;
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
    public final static String executeCommandLineCommandWithEnvVarsInWorkingDir(String command, String[] envp, File dir) throws IOException {

        Runtime rt = Runtime.getRuntime();
        Process p = null;
        try {
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
            StringBuffer sb = new StringBuffer();

            while ((len = is.read(b)) != -1) {
                byte[] res = new byte[len];
                System.arraycopy(b, 0, res, 0, len);
                String s = new String(res);
                sb.append(s);
            }
            return sb.toString();
        } finally {
            freeProcessResources(p);
        }

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

    
}
