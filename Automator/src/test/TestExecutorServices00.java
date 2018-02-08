package test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestExecutorServices00 {
	
	
	public class Task implements Callable<Integer> {
		
		public Task(Integer i) {
			id = i;
		}

		@Override
		public Integer call() throws Exception {
			for(long i=0; i < 100000; i++) {
				Thread.sleep(0, 50);
			}
			System.out.println("--> " + id);
			return id;
		}

		private Integer id = null;
		
	}
	
	

	public TestExecutorServices00() {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		
		CompletionService<Integer> compService = new ExecutorCompletionService<>(executor);
		for(int i=0; i < 10; i++) {
			Task t = new Task(i);
			compService.submit(t);
		}
		
		System.out.println("000 In Parallelo...");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("waiting...");
		int i=0;
		boolean isTimedOut = false;
		while(i<10 && !isTimedOut) {
			try {
				Future<Integer> future = compService.poll(30, TimeUnit.SECONDS);
				if(future == null) {
					isTimedOut = true;
					executor.shutdownNow();
					TimeoutException ex = new TimeoutException("Poll timeout occurred...");
					throw ex;
				}
				else {
					System.out.println("Res := " + future.get(3, TimeUnit.SECONDS));
				}
			}
			catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
		}
		
		executor.shutdown();
		
		
		
		System.out.println("111 In Parallelo....");
		
	}

	public static void main(String[] args) {
		new TestExecutorServices00();
	}

}
