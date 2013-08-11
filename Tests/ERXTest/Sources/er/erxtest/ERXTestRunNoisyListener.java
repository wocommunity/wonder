
package er.erxtest;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class ERXTestRunNoisyListener extends RunListener {

	int attempted = 0;
	int failed = 0;
	long start = 0L;

	@Override
	public void testRunStarted(Description description) { start = System.currentTimeMillis(); }
	@Override
	public void testStarted(Description description) { attempted++; }
	@Override
	public void testFailure(Failure failure) {
		System.out.print("\n"+failure+" FAILED\n");
		failed++;
	}
	@Override
	public void testFinished(Description description) { System.out.println(description.toString()); }

	@Override
	public void testRunFinished(Result result) {
		long end = System.currentTimeMillis();

		System.out.println("");
		System.out.println("tests run: "+attempted);
		System.out.println("tests FAILED: "+failed);
		System.out.println("time elapsed: "+(int)((end - start)/1000)+" sec");
		System.out.println("");
	}

}

