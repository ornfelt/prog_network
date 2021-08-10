
//thread class that inherits from thread
public class T1 extends Thread {
	
	//boolean controlling active status
	private boolean active = false;
	
	//threads run method
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
	
	//synchronized method that sets active status to false
	public synchronized void setActiveToFalse() {
		System.out.println("Stopping thread 1");
		active = false;
	}
	
	//synchronized method that sets active status to true
	public synchronized void setActiveToTrue() {
		active = true;
	}
}
