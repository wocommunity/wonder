package er.plugintest.tests;

import java.util.ArrayList;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;

import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEC;
import er.plugintest.model.City;
import er.plugintest.model.Country;

public class MultithreadedTest extends PluginTest {

	public MultithreadedTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		resetData();

	}
	
	
	
	
	public void testMutipleThreads() {
		
		ArrayList<QueryTask> qTasks = new  ArrayList<MultithreadedTest.QueryTask>();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for (int i = 0; i < 25; i++) {
			QueryTask task = new QueryTask();
			qTasks.add(task);
			new Thread(task, "QueryThread-" +i).start();
		}
		
		for (int i = 0; i < 5; i++) {
			InsertTask task = new InsertTask();
			threads.add(new Thread(task, "InsertThread-" + i));
		}
		
		for (int i = 0; i < 5; i++) {
			UpdateTask task = new UpdateTask();
			threads.add(new Thread(task, "UpdateThread-" + i));
		}
		
		for (Thread thread : threads) {
			thread.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
			}
		}
		
		for (QueryTask task : qTasks) {
			task.setRun(false);
		}
	}
	
	
	public static class QueryTask implements Runnable {

		private boolean run = true;
		@Override
		public void run() {
			
			while (getRun()) {
				ERXEC ec = (ERXEC) ERXEC.newEditingContext();
				ec.lock();
				try {
					
					NSArray<City> cities = City.fetchCities(ec, City.NAME.likeInsensitive(RandomStringUtils.randomAlphabetic(1) + "*"), City.NAME.ascs());
					
				} finally {
					ec.unlock();
				}
			}
		}
		public synchronized void setRun(boolean run) {
			this.run = run;
		}
		public  synchronized boolean getRun() {
			return run;
		}
		
	}

	public static class InsertTask implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 25; i++) {
				ERXEC ec = (ERXEC) ERXEC.newEditingContext();
				ec.lock();
				try {
					Country country = Country.fetchAllCountries(ec).lastObject();
					City city = City.createCity(ec, RandomStringUtils.randomAlphabetic(15));
					city.setCountryRelationship(country);
					
					ec.saveChanges();
				} finally {
					ec.unlock();
				}
				
			}
		}
		
	}

	public static class UpdateTask implements Runnable {

		@Override
		public void run() {
			for (int i = 0; i < 25; i++) {
				ERXEC ec = (ERXEC) ERXEC.newEditingContext();
				ec.lock();
				try {
					City city = City.fetchCity(ec, City.NAME.eq("Amsterdam"));
					
					if (city != null) {
						city.setPopulation(RandomUtils.nextInt(3));
					}
					
					ec.saveChanges();
				} finally {
					ec.unlock();
				}
				
			}
			
		}
		
	}
}
