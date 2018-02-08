package test;

public class TestThreadInterrupt00 implements Runnable {

	public TestThreadInterrupt00() {
		Thread t = new Thread(this);
		t.start();
		
		synchronized(TestThreadInterrupt00.class) {
			try {
				TestThreadInterrupt00.class.wait(5000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Interrupted...");
		t.interrupt();
	}
	
	public void run() {
		while(true) {
			for(long i=0; i<100000000; i++) {
				for(long j=0; j<100000000; j++) {
					if(j%100==0)
						System.out.println("");
					System.out.print(".");
					try {
						Thread.sleep(0, 10);
					}
					catch (InterruptedException e) {
						
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		new TestThreadInterrupt00();
	}

}
