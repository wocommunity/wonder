package er.extensions;


/**
 * ExceptionConverter changes a checked exception into an unchecked (Runtime) exception.  It preserves the stack trace of the original exception so that the location of the error can be pinpointed.
 *<br>
 *<b>Example Usage</b>
 *<br>
 *<code> 
 *try {
 *  f();
 *} catch(IOException ex) {
 *  System.out.println("Printing out plain ol' IOException:");
 *  System.out.println("---");
 *  ex.printStackTrace();
 *  System.out.println("---");
 *  throw new ExceptionConverter(ex);
 *}
 *</code>
 *<br>
 *<br>
 *
 * This material from The Java(tm) Specialists' Newsletter by Maximum Solutions (South Africa) - The Java(tm) Specialists.<br>
 * Please contact <a href="mailto:h.kabutz@computer.org>Dr. Heinz M. Kabutz (h.kabutz@computer.org)</a> for more information.
 *<br>
 * Back issues are available on-line at http://www.smotricz.com/kabutz.  This code is taken from <a href="http://www.smotricz.com/kabutz/Issue033.html">Issue 33</a>
 */
public class ERXExceptionConverter extends RuntimeException 
{

    /** we keep a handle to the wrapped exception */
    private final Exception ex;

    public ERXExceptionConverter(Exception exception)
    {
        this.ex = exception;
    }



    /** and allow the user of ExceptionConverter to get a handle to it. */
    public Exception getException()
    {
        return ex;
    }



    /** We print the message of the checked exception */
    public String getMessage()
    {
        return ex.getMessage();
    }



    /** and make sure we also produce a localized version */
    public String getLocalizedMessage()
    {
        return ex.getLocalizedMessage();
    }



    /** The toString() is changed to be prefixed with ExceptionConverter */
    public String toString()
    {
        return "ExceptionConverter: " + ex;
    }



    /** we have to override this as well */
    public void printStackTrace()
    {
        printStackTrace(System.err);
    }



    /** here we prefix, with s.print(), not s.println(), the stack trace with "ExceptionConverter:" */
    public void printStackTrace(java.io.PrintStream s)
    {
        synchronized (s)
        {
            s.print("ExceptionConverter: ");
            ex.printStackTrace(s);
        }
    }



    /** Again, we prefix the stack trace with "ExceptionConverter:" */
    public void printStackTrace(java.io.PrintWriter s)
    {
        synchronized (s)
        {
            s.print("ExceptionConverter: ");
            ex.printStackTrace(s);
        }
    }



    /** requests to fill in the stack trace we will have to ignore (I think)  We can't throw an exception here, as this method is called by the constructor of Throwable */
    public Throwable fillInStackTrace() 
    {
        return this;
    }

}

