package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestExecutorServices01 {
	
	
	public class Task implements Callable<Integer> {
		
		public Task(Integer i) {
			id = i;
		}

		@Override
		public Integer call() throws Exception {
			for(long i=0; i < 100000 * 100000 * 100; i++) {
				for(long j=0; j < 100000 * 100000 * 100; j++) {
					
				}
			}
			System.out.println("--> " + id);
			return id;
		}

		private Integer id = null;
		
	}
	
	

	public TestExecutorServices01() {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		Collection<Callable<Integer>> tasks = new ArrayList<>();
		for(int i=0; i < 10; i++) {
			Task t = new Task(i);
			tasks.add(t);
		}
		
		System.out.println("000 In Parallelo....");
		
		
		List<Future<Integer>> results;
		try {
			results = executor.invokeAll(tasks);
			for(Future<Integer> result : results){
		    	System.out.println("Res := " + result.get(3, TimeUnit.SECONDS));
		    }
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
		
		executor.shutdown();
		
		
		
		System.out.println("111 In Parallelo....");
		
	}

	public static void main(String[] args) {
		new TestExecutorServices01();
	}

}
