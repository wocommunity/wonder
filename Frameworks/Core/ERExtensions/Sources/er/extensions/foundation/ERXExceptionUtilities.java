package er.extensions.foundation;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXExtensions;
import er.extensions.localization.ERXLocalizer;

/**
 * Provides a set of utilities for displaying and managing exceptions.
 * 
 * @author mschrag
 */
public class ERXExceptionUtilities {
	public static Logger log = Logger.getLogger(ERXExceptionUtilities.class);

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
		public HideStackTraceException(Throwable cause) {
			super(cause);
		}

		public void printStackTrace(PrintWriter s) {
			s.println("[stack trace already printed]");
		}
	}

	/**
	 * Returns a paragraph form of the given throwable.
	 * 
	 * @param t
	 *            the throwable to convert to paragraph form
	 * @return the paragraph string
	 */
	public static String toParagraph(Throwable t) {
		StringBuffer messageBuffer = new StringBuffer();
		boolean foundInternalError = false;
		Throwable throwable = t;
		while (throwable != null) {
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
			message = message.replaceAll("<[^>]+>", "");
			message = message.trim();
			messageBuffer.append(message);
			if (!message.endsWith(".")) {
				messageBuffer.append(". ");
			}
			else {
				messageBuffer.append(" ");
			}
			throwable = oldThrowable.getCause();
			if (throwable == oldThrowable) {
				throwable = null;
			}
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

	/**
	 * Prints the given throwable to the given writer with an indent.
	 * 
	 * @param t
	 *            the throwable to print
	 * @param writer
	 *            the writer to print to
	 * @param indent
	 *            the indent level to use
	 * @property er.extensions.stackTrace.cleanup if true, stack traces are
	 *           cleaned up for easier use
	 * @property er.extensions.stackTrace.skipPatternsFile the name the resource
	 *           that contains an array of class name and method regexes to skip
	 *           in stack traces
	 */
	public static void printStackTrace(Throwable t, PrintWriter writer, int indent) {
		try {
			boolean cleanup = ERXProperties.booleanForKeyWithDefault("er.extensions.stackTrace.cleanup", false);
			Throwable actualThrowable;
			if (cleanup) {
				actualThrowable = t;
			}
			else {
				actualThrowable = ERXExceptionUtilities.getMeaningfulThrowable(t);
			}
			if (actualThrowable == null) {
				return;
			}

			NSArray<Pattern> skipPatterns = ERXExceptionUtilities._skipPatterns;
			if (cleanup && skipPatterns == null) {
				String skipPatternsFile = ERXProperties.stringForKey("er.extensions.stackTrace.skipPatternsFile");
				if (skipPatternsFile != null) {
					NSMutableArray<Pattern> mutableSkipPatterns = new NSMutableArray<Pattern>();
					
					Enumeration<String> frameworksEnum = ERXLocalizer.frameworkSearchPath().reverseObjectEnumerator();
					while (frameworksEnum.hasMoreElements()) {
						String framework = frameworksEnum.nextElement();
						URL path = ERXFileUtilities.pathURLForResourceNamed(skipPatternsFile, framework, null);
						System.out.println("ERXExceptionUtilities.printStackTrace: " + path + ", " + framework);
						if (path != null) {
							try {
								NSArray<String> skipPatternStrings = (NSArray<String>) ERXExtensions.readPropertyListFromFileInFramework(skipPatternsFile, framework, null);
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
						ERXExceptionUtilities._skipPatterns = NSArray.<Pattern> emptyArray();
					}
					else {
						ERXExceptionUtilities._skipPatterns = skipPatterns;
					}
				}
			}

			StackTraceElement[] elements = actualThrowable.getStackTrace();

			ERXStringUtilities.indent(writer, indent);
			if (indent > 0) {
				writer.print("Caused by a ");
			}
			if (cleanup) {
				writer.print(actualThrowable.getClass().getSimpleName());
			}
			else {
				writer.print(actualThrowable.getClass().getName());
			}
			String message = actualThrowable.getLocalizedMessage();
			if (message != null) {
				writer.print(": ");
				writer.print(message);
			}
			writer.println();

			int stackDepth = 0;
			int skippedCount = 0;
			for (StackTraceElement element : elements) {
				boolean showElement = true;

				if (stackDepth > 0 && cleanup && skipPatterns != null && !skipPatterns.isEmpty()) {
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
						ERXStringUtilities.indent(writer, indent + 1);
						writer.println("   ... skipped " + skippedCount + " stack elements");
						skippedCount = 0;
					}
					ERXStringUtilities.indent(writer, indent + 1);
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
				ERXStringUtilities.indent(writer, indent + 1);
				writer.println("... skipped " + skippedCount + " stack elements");
			}

			Throwable cause = actualThrowable.getCause();
			if (cause != null && cause != actualThrowable) {
				ERXExceptionUtilities.printStackTrace(cause, writer, indent + 1);
			}
		}
		catch (Throwable thisSucks) {
			writer.println("ERXExceptionUtilities.printStackTrace Failed!");
			thisSucks.printStackTrace(writer);
		}
	}

}
