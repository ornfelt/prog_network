//thread class that implements runnable interface
public class T2 implements Runnable {
	
	private Thread t;
	private boolean active = false;

	//constructor that sets new thread to this
	public T2() {
		t = new Thread(this);
	  }
	
	//start thread
	public void start() {
		if(t == null) {
			t = new Thread(this);
		}
		t.start();
	}
	
	//stop thread
	public synchronized void stop() {
		System.out.println("Stopping thread 2");
		active = false;
		t.interrupt();
		t = null;	
	}

	//run method for thread
	public void run() {
		  while(t != null) {
			  while(active) {
				  System.out.println("Thread 2");
				  try { t.sleep(1000); } catch(InterruptedException ie) {}
			  }
			  try { t.sleep(25); } catch(InterruptedException ie) {}
		  }
  }
	
	//pause the thread for incoming time parameter
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
	
	//synchronized method that sets active status to false
	public synchronized void setActiveToFalse() {
		active = false;
	}
	
	//synchronized method that sets active status to true
	public synchronized void setActiveToTrue() {
		active = true;
	}
}
