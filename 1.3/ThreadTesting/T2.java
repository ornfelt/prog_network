package ThreadTesting;

public class T2 implements Runnable {
	
	private Thread t;
	private boolean active = false;

	public T2() {
		t = new Thread(this);
	  }
	
	public void start() {
		if(t == null) {
			t = new Thread(this);
		}
		t.start();
	}
	
	public synchronized void stop() {
		System.out.println("Stopping thread 2");
		active = false;
		t.interrupt();
		t = null;	
	}

	public void run() {
		  while(t != null) {
			  while(active) {
				  System.out.println("Thread 2");
				  try { t.sleep(1000); } catch(InterruptedException ie) {}
			  }
			  try { t.sleep(25); } catch(InterruptedException ie) {}
		  }
  }
	
	public void pause(long time) {
		System.out.println("Pausing thread 2 ");
		try {
			active = false;
			t.sleep(time);
			active = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	  
	public boolean isActive() {
		return active;
	}
	
	public synchronized void setActiveToFalse() {
		active = false;
	}
	
	public synchronized void setActiveToTrue() {
		active = true;
	}
}
