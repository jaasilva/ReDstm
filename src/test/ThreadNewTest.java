import java.util.concurrent.atomic.AtomicInteger;


public class ThreadNewTest {
	
	
	public static class A {
		String i = new String("");
	}
	
	public static class Dummy {
		int i = 0;
		Dummy d = null;
		A b = new A();
		A a = new A();
		
	}
	
	public static class Worker implements Runnable {
		public volatile boolean stop = false;
		public volatile boolean warm = true;
		
		public long count = 0;
		
		public void run() {
			while(warm) {
				
			}
			while(!stop) {
				Dummy o = new Dummy();
				count++;
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		Worker[] ws = new Worker[2];
		Thread[] ts = new Thread[2];
		for (int i=0; i < ws.length; i++) {
			ws[i] = new Worker();
			ts[i] = new Thread(ws[i]);
 		}
		
		for (int i=0; i < ws.length; i++) {
			ts[i].start();
 		}
		
		Thread.sleep(1000);
		
		for (int i=0; i < ws.length; i++) {
			ws[i].warm = false;
 		}
		
		Thread.sleep(5000);
		
		for (int i=0; i < ws.length; i++) {
			ws[i].stop = true;
 		}
		
		for (int i=0; i < ws.length; i++) {
			ts[i].join();
 		}
		
		for (int i=0; i < ws.length; i++) {
			System.out.println("T"+i+" -> "+ws[i].count);
 		}
	}
	
}
