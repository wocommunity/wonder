
package er.erxtest;

import org.junit.runner.Description;
import org.junit.runner.Result;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ERXTestRunQuietListener extends RunListener {

	int attempted = 0;
	int failed = 0;
	long start = 0L;

	int loop = 0;

	public void testRunStarted(Description description) { start = System.currentTimeMillis(); }

	public void testStarted(Description description) {
		attempted++;
		if ((loop % 100) == 0) System.out.println("");
		System.out.print(".");
		loop++;
	}

	public void testFailure(Failure failure) { System.out.print("x"); failed++; }

	public void testRunFinished(Result result) {
		long end = System.currentTimeMillis();

		System.out.println("\n");
		System.out.println("tests run: "+attempted);
		System.out.println("tests FAILED: "+failed);
		System.out.println("time elapsed: "+(int)((end - start)/1000)+" sec");
		System.out.println("");
	}
}
