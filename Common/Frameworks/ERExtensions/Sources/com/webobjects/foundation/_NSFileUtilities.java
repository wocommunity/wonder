package com.webobjects.foundation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import er.extensions.ERXProperties;

/**
 * Utilities for manipulating files. WO 5.4.3.1's _NSFileUtilies and this should be kept in sync.
 * 
 * @since 5.4.3.1
 */
public class _NSFileUtilities {

    /**
     * Frees all of the resources associated with a given
     * process and then destroys the process.
     * @param p process to destroy
     */
    protected static void freeProcessResources(Process p) {
        if (p!=null) {
            try {
                if (p.getInputStream()!=null) p.getInputStream().close();
                if (p.getOutputStream()!=null) p.getOutputStream().close();
                if (p.getErrorStream()!=null) p.getErrorStream().close();
                p.destroy();
            } catch (IOException e) {}
        }
    }

    //	===========================================================================
    //	Class Constants
    //	---------------------------------------------------------------------------

    /** logging support */
    public static final Logger log = Logger.getLogger(_NSFileUtilities.class);

    // Attempts to resolve a named link to its canonical path, using default settings.
    public static File resolveLink(String path, String linkName) {
        int retry = ERXProperties.intForKeyWithDefault("NSFileUtilities.resolveLinkRetryPeriod", 5000);
        int timeout = ERXProperties.intForKeyWithDefault("NSFileUtilities.resolveLinkTimeoutPeriod", 60000);
        return _NSFileUtilities.resolveLink(path, linkName, retry, timeout);
    }

    /**
     * Attempts to resolve a named link to its canonical path. This sometimes fails (a named link will simply resolve
     * to itself, possibly when its being updated), so this utility function will attempt to retry resolving the link
     * up to a certain timeout period. Any IOException thrown will also be caught and handled as a RuntimeException.
     * This function will bypass such behavior though if the path does not end with the specified name, since an
     * absolute path may have been passed in (which should always resolve to itself anyway).
     * @param path      The path (usually a current link to an index directory)
     * @param linkName  The link name (usually "current")
     * @param retry     The period in milliseconds between successive retries to resolve the canonical path
     * @param timeout   The total period of time to retry for in milliseconds, before a RuntimeException will be thrown
     * @return a new File object based on the canonical path encapsulated in the path
     */
    public static File resolveLink(String path, String linkName, int retry, int timeout) {
        File resolvedPath;

        // <rdar://problem/6429201> Issues with File.getCanonicalFile()
        boolean debuggingEnabled = ERXProperties.booleanForKeyWithDefault("NSFileUtilities.debugMissingCurrentLinks", false);

        if (debuggingEnabled) {
            log.info("Resolving link (" + linkName + ") for: " + path);
        }

        try {
            File f = new File(path);
            boolean isNamedLink = f.getName().toLowerCase().equals(linkName.toLowerCase());
            long timeoutPoint = System.currentTimeMillis() + timeout;

            if (debuggingEnabled) {
                log.info("Testing isNamedLink: " + f.getName() + " == " + linkName + " ==> " + (isNamedLink ? "true" : "false"));
            }

            // Retry loop
            while (true) {

                if (debuggingEnabled) {
                    // Run stat command
                    String output = "";
                    String[] cmd = new String[3];
                    cmd[0] = "stat";
                    cmd[1] = "-F";
                    cmd[2] = f.getPath();
                    Process task = null;
                    try {
                        task = Runtime.getRuntime().exec(cmd);
                        while (true) {
                            try { task.waitFor(); break; }
                            catch (InterruptedException e) {}
                        }
                        BufferedReader out = new BufferedReader(new InputStreamReader(task.getInputStream()));
                        output = out.readLine();
                        if (task.exitValue() != 0) {
                            BufferedReader err = new BufferedReader(new InputStreamReader(task.getErrorStream()));
                            output += "ERROR: " + err.readLine();
                        }
                    } catch (Throwable t) {
                        log.info("Failed to run stat with exception", t);
                    } finally {
                        _NSFileUtilities.freeProcessResources(task);
                    }
                    log.info("debugMissingCurrentLinks: stat output: " + output);
                }

                // Get the canonical file
                resolvedPath = f.getCanonicalFile();
                if (debuggingEnabled) {
                    log.info("Fetched resolved path: " + resolvedPath);
                }
                if (!isNamedLink || !resolvedPath.getName().toLowerCase().equals(linkName.toLowerCase())) {
                    if (debuggingEnabled) {
                        log.info("resolveLink succeeded");
                    }
                    break; // getCanonicalFile succeeded!
                }

                // Resolve link failed
                if (debuggingEnabled) {
                    log.info("resolveLink failed");
                }

                // Check the timeout period
                if (System.currentTimeMillis() + retry >= timeoutPoint) {
                    break; // timeout period reached
                }

                // Wait for retry interval
                Thread.sleep(retry);
            }
        } catch (Exception e) {
            // Something failed, probably IOException occurred...
            throw new RuntimeException("Failed to safely resolve current path: " + path, e);
        }

        return resolvedPath;
    }
}
