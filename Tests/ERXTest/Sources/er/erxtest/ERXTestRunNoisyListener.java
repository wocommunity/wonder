
package er.erxtest;

import org.junit.runner.Description;
import org.junit.runner.Result;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ERXTestRunNoisyListener extends RunListener {

	int attempted = 0;
	int failed = 0;
	long start = 0L;

	public void testRunStarted(Description description) { start = System.currentTimeMillis(); }
	public void testStarted(Description description) { System.out.print("test: "+description); attempted++; }
	public void testFailure(Failure failure) { System.out.print("\n"+failure+"\n"); failed++; }
	public void testFinished(Description description) { System.out.println(""); }

	public void testRunFinished(Result result) {
		long end = System.currentTimeMillis();

		System.out.println("");
		System.out.println("tests run: "+attempted);
		System.out.println("tests FAILED: "+failed);
		System.out.println("time elapsed: "+(int)((end - start)/1000)+" sec");
		System.out.println("");
	}

}

