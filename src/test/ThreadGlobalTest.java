import java.util.concurrent.atomic.AtomicInteger;


public class ThreadGlobalTest {

	public static class Pool {
		public Object[] arr = new Object[1<<14];
		public AtomicInteger next = new AtomicInteger(0);
		
		public Object getNext() {
			int i = next.getAndIncrement();
			if (i >= arr.length) {
				while (!next.compareAndSet(i, 0)) {
					i = next.get();
				}
				i = 0;
			}
			Object o = arr[i];
			i = next.get();
			
			return o;
		}
	}
	
	static Pool pool = new Pool();
	
	public static class Worker implements Runnable {
		public volatile boolean stop = false;
		
		public long count = 0;
		
		public void run() {
			while(!stop) {
				pool.getNext();
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
