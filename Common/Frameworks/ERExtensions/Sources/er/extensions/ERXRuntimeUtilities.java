package er.extensions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class ERXRuntimeUtilities {



    /** Excecutes the specified command line command. If envp is not null the  environment variables are set
    * before executing the command.
    *
    * @param commands the commands to execute like "ls -la" or "cp /tmp/file1 /tmp/file2" or "open /Applications/*.app"
     * @param envp a <code>String</code> array which represents the environment variables like
     * <code>String[] envp = new String[]{"PATH=/usr/bin:/bin", "CVS_RSH=ssh"}</code>, can be null
     * @param dir a <code>File</code> object representing the working directory, can be null
     *
     * @return the result from the process that was executed
     *
     * @exception IOException if something went wrong
     */
public final static String executeCommandLineCommandsWithEnvVarsInWorkingDir(String[] commands, String[] envp, File dir) throws IOException {
    Runtime rt = Runtime.getRuntime();

    Process p;
    if (dir == null && envp == null) {
        p = rt.exec(commands);
    } else if (dir == null) {
        p = rt.exec(commands, envp);
    } else if (envp == null) {
        throw new IllegalArgumentException("if dir != null then envp must also be != null");
    } else {
        p = rt.exec(commands, envp, dir);
    }

    //wait for the result of identify
    try {
        p.waitFor();
    } catch (InterruptedException ex) {
    }

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
}
}