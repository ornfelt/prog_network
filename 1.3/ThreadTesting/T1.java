package ThreadTesting;

public class T1 extends Thread {
	
	private boolean active = false;
	
	public void run() {
		  while(isAlive()) {
			  while(active) {
				  System.out.println("Thread 1");
				  try { sleep(1000); } catch(InterruptedException ie) {}
			  }
			  try { sleep(25); } catch(InterruptedException ie) {}
		  }
	}
	
	public boolean isActive() {
		return active;
	}
	
	public synchronized  void setActiveToFalse() {
		System.out.println("Stopping thread 1");
		active = false;
	}
	
	public synchronized  void setActiveToTrue() {
		active = true;
	}
}
