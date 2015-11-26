package er.extensions.foundation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.localization.ERXLocalizer;

/**
 * Provides a set of utilities for displaying and managing exceptions.
 * 
 * @author mschrag
 */
public class ERXExceptionUtilities {
	public static final Logger log = Logger.getLogger(ERXExceptionUtilities.class);

	/**
	 * Implemented by any exception that you explicitly want to not appear in
	 * stack dumps.
	 * 
	 * @author mschrag
	 */
	public static interface WeDontNeedAStackTraceException {
	}

	/**
	 * Wraps a root cause, but does not render a stack trace to the given
	 * writer. This is used to intercept old code that handles exceptions in
	 * undesirable ways.
	 * 
	 * @author mschrag
	 */
	public static class HideStackTraceException extends NSForwardException {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public HideStackTraceException(Throwable cause) {
			super(cause);
		}

		@Override
		public void printStackTrace(PrintWriter s) {
			s.println("[stack trace already printed]");
		}
	}

	/**
	 * Returns the cause of an exception.  This should be modified to be pluggable.
	 * 
	 * @param t the original exception
	 * @return the cause of the exception or null of there isn't one
	 */
	protected static Throwable getCause(Throwable t) {
		Throwable cause = null;
		if (t != null) {
			cause = t.getCause();
			if (cause == null) {
				try {
					// Check for OGNL root causes
					Class ognlExceptionClass = Class.forName("ognl.OgnlException");
					if (ognlExceptionClass.isAssignableFrom(t.getClass())) {
						Method reasonMethod = ognlExceptionClass.getDeclaredMethod("getReason");
						cause = (Throwable) reasonMethod.invoke(t);
					}
				}
				catch (Throwable e) {
					// IGNORE
				}
			}
		}
		if (t == cause) {
			cause = null;
		}
		return cause;
	}

	/**
	 * Returns a paragraph form of the given throwable.
	 * 
	 * @param t
	 *            the throwable to convert to paragraph form
	 * @return the paragraph string
	 */
	public static String toParagraph(Throwable t) {
		return ERXExceptionUtilities.toParagraph(t, true);
	}

	/**
	 * Returns a paragraph form of the given throwable.
	 * 
	 * @param t
	 *            the throwable to convert to paragraph form
	 * @param removeHtmlTags if true, html tags will be filtered from the error messages (to remove, for instance, bold tags from validation messages)
	 * @return the paragraph string
	 */
	public static String toParagraph(Throwable t, boolean removeHtmlTags) {
		StringBuilder messageBuffer = new StringBuilder();
		boolean foundInternalError = false;
		Throwable throwable = t;
		while (throwable != null) {
			if (messageBuffer.length() > 0) {
				messageBuffer.append(' ');
			}
			Throwable oldThrowable = ERXExceptionUtilities.getMeaningfulThrowable(throwable);
			String message = throwable.getLocalizedMessage();
			if (message == null) {
				if (!foundInternalError) {
					message = "Your request produced an error.";
					foundInternalError = true;
				}
				else {
					message = "";
				}
			}
			if (removeHtmlTags) {
				message = message.replaceAll("<[^>]+>", "");
			}
			message = message.trim();
			messageBuffer.append(message);
			if (!message.endsWith(".")) {
				messageBuffer.append('.');
			}
			throwable = ERXExceptionUtilities.getCause(oldThrowable);
		}
		return messageBuffer.toString();
	}

	/**
	 * Returns the "meaningful" root cause from a throwable. For instance, an
	 * InvocationTargetException is useless -- it's the cause that matters.
	 * 
	 * @param t
	 *            the meaningful exception given another throwable
	 * @return the meaningful exception
	 */
	public static Throwable getMeaningfulThrowable(Throwable t) {
		Throwable meaningfulThrowable;
		if (t instanceof NSForwardException) {
			meaningfulThrowable = ((NSForwardException) t).originalException();
		}
		else if (t instanceof InvocationTargetException) {
			meaningfulThrowable = ((InvocationTargetException) t).getCause();
		}
		else if (t instanceof WeDontNeedAStackTraceException && t.getMessage() == null) {
			meaningfulThrowable = t.getCause();
		}
		else {
			meaningfulThrowable = t;
		}
		if (meaningfulThrowable != t) {
			meaningfulThrowable = ERXExceptionUtilities.getMeaningfulThrowable(meaningfulThrowable);
		}
		return meaningfulThrowable;
	}

	/**
	 * Prints a debug stack trace to the console.
	 */
	public static void printStackTrace() {
		Exception e = new Exception("DEBUG");
		e.fillInStackTrace();
		ERXExceptionUtilities.printStackTrace(e);
	}

	/**
	 * Logs a debug stack trace.
	 */
	public static void logStackTrace() {
		Exception e = new Exception("DEBUG");
		e.fillInStackTrace();
		ERXExceptionUtilities.log.error(null, e);
	}

	/**
	 * Prints the given throwable to the console (stdout).
	 * 
	 * @param t
	 *            the throwable to print
	 */
	public static void printStackTrace(Throwable t) {
		ERXExceptionUtilities.printStackTrace(t, System.out);
	}

	/**
	 * Prints the given throwable to the given outputstream.
	 * 
	 * @param t
	 *            the throwable to print
	 * @param os
	 *            the stream to print to
	 */
	public static void printStackTrace(Throwable t, OutputStream os) {
		ERXExceptionUtilities.printStackTrace(t, new PrintWriter(os, true), 0);
	}

	/**
	 * Prints the given throwable to the given printwriter.
	 * 
	 * @param t
	 *            the throwable to print
	 * @param writer
	 *            the writer to print to
	 */
	public static void printStackTrace(Throwable t, Writer writer) {
		ERXExceptionUtilities.printStackTrace(t, new PrintWriter(writer, true), 0);
	}

	/**
	 * Prints the given throwable to the given printwriter.
	 * 
	 * @param t
	 *            the throwable to print
	 * @param writer
	 *            the writer to print to
	 */
	public static void printStackTrace(Throwable t, PrintWriter writer) {
		ERXExceptionUtilities.printStackTrace(t, writer, 0);
	}

	private static NSArray<Pattern> _skipPatterns;

	protected static void _printSingleStackTrace(Throwable t, PrintWriter writer, int exceptionDepth, boolean cleanupStackTrace) {
		NSArray<Pattern> skipPatterns = ERXExceptionUtilities._skipPatterns;
		if (cleanupStackTrace && skipPatterns == null) {
			String skipPatternsFile = ERXProperties.stringForKey("er.extensions.stackTrace.skipPatternsFile");
			if (skipPatternsFile != null) {
				NSMutableArray<Pattern> mutableSkipPatterns = new NSMutableArray<Pattern>();

				Enumeration<String> frameworksEnum = ERXLocalizer.frameworkSearchPath().reverseObjectEnumerator();
				while (frameworksEnum.hasMoreElements()) {
					String framework = frameworksEnum.nextElement();
					URL path = ERXFileUtilities.pathURLForResourceNamed(skipPatternsFile, framework, null);
					if (path != null) {
						try {
							NSArray<String> skipPatternStrings = (NSArray<String>) ERXFileUtilities.readPropertyListFromFileInFramework(skipPatternsFile, framework, (NSArray)null);
							if (skipPatternStrings != null) {
								for (String skipPatternString : skipPatternStrings) {
									try {
										mutableSkipPatterns.addObject(Pattern.compile(skipPatternString));
									}
									catch (Throwable patternThrowable) {
										ERXExceptionUtilities.log.error("Skipping invalid exception pattern '" + skipPatternString + "' in '" + skipPatternsFile + "' in the framework '" + framework + "' (" + ERXExceptionUtilities.toParagraph(patternThrowable) + ")");
									}
								}
							}
						}
						catch (Throwable patternThrowable) {
							ERXExceptionUtilities.log.error("Failed to read pattern file '" + skipPatternsFile + "' in the framework '" + framework + "' (" + ERXExceptionUtilities.toParagraph(patternThrowable) + ")");
						}
					}
				}

				skipPatterns = mutableSkipPatterns;
			}

			if (ERXProperties.booleanForKeyWithDefault("er.extensions.stackTrace.cachePatterns", true)) {
				if (skipPatterns == null) {
					ERXExceptionUtilities._skipPatterns = NSArray.EmptyArray;
				}
				else {
					ERXExceptionUtilities._skipPatterns = skipPatterns;
				}
			}
		}

		StackTraceElement[] elements = t.getStackTrace();

		ERXStringUtilities.indent(writer, exceptionDepth);
		if (exceptionDepth > 0) {
			writer.print("Caused by a ");
		}
		if (cleanupStackTrace) {
			writer.print(t.getClass().getSimpleName());
		}
		else {
			writer.print(t.getClass().getName());
		}
		String message = t.getLocalizedMessage();
		if (message != null) {
			writer.print(": ");
			writer.print(message);
		}
		writer.println();

		int stackDepth = 0;
		int skippedCount = 0;
		for (StackTraceElement element : elements) {
			boolean showElement = true;

			if (stackDepth > 0 && cleanupStackTrace && skipPatterns != null && !skipPatterns.isEmpty()) {
				String elementName = element.getClassName() + "." + element.getMethodName();
				for (Pattern skipPattern : skipPatterns) {
					if (skipPattern.matcher(elementName).matches()) {
						showElement = false;
						break;
					}
				}
			}

			if (!showElement) {
				skippedCount++;
			}
			else {
				if (skippedCount > 0) {
					ERXStringUtilities.indent(writer, exceptionDepth + 1);
					writer.println("   ... skipped " + skippedCount + " stack elements");
					skippedCount = 0;
				}
				ERXStringUtilities.indent(writer, exceptionDepth + 1);
				writer.print("at ");
				writer.print(element.getClassName());
				writer.print(".");
				writer.print(element.getMethodName());
				writer.print("(");
				if (element.isNativeMethod()) {
					writer.print("Native Method");
				}
				else if (element.getLineNumber() < 0) {
					writer.print(element.getFileName());
					writer.print(":Unknown");
				}
				else {
					writer.print(element.getFileName());
					writer.print(":");
					writer.print(element.getLineNumber());
				}
				writer.print(")");
				writer.println();
			}

			stackDepth++;
		}

		if (skippedCount > 0) {
			ERXStringUtilities.indent(writer, exceptionDepth + 1);
			writer.println("... skipped " + skippedCount + " stack elements");
		}
	}
	
	/**
	 * Prints the given throwable to the given writer with an indent.
	 * 
	 * @param t
	 *            the throwable to print
	 * @param writer
	 *            the writer to print to
	 * @param exceptionDepth
	 *            the indent level to use
	 * @property er.extensions.stackTrace.cleanup if true, stack traces are
	 *           cleaned up for easier use
	 * @property er.extensions.stackTrace.skipPatternsFile the name the resource
	 *           that contains an array of class name and method regexes to skip
	 *           in stack traces
	 */
	public static void printStackTrace(Throwable t, PrintWriter writer, int exceptionDepth) {
		try {
			boolean cleanupStackTrace = ERXProperties.booleanForKeyWithDefault("er.extensions.stackTrace.cleanup", false);
			Throwable actualThrowable;
			if (cleanupStackTrace) {
				actualThrowable = t;
			}
			else {
				actualThrowable = ERXExceptionUtilities.getMeaningfulThrowable(t);
			}
			if (actualThrowable == null) {
				return;
			}

			Throwable cause = ERXExceptionUtilities.getCause(actualThrowable);
			boolean showOnlyBottomException = ERXProperties.booleanForKeyWithDefault("er.extensions.stackTrace.bottomOnly", true);
			if (!showOnlyBottomException || cause == null) {
				ERXExceptionUtilities._printSingleStackTrace(actualThrowable, writer, exceptionDepth, cleanupStackTrace);
			}
			if (cause != null && cause != actualThrowable) {
				ERXExceptionUtilities.printStackTrace(cause, writer, exceptionDepth);
			}
		}
		catch (Throwable thisSucks) {
			writer.println("ERXExceptionUtilities.printStackTrace Failed!");
			thisSucks.printStackTrace(writer);
		}
	}

}
