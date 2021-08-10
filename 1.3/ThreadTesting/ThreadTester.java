package ThreadTesting;

import java.io.IOException;


public class ThreadTester {
	
	public static void main (String args[]) {
		T1 t1 = new T1();
		t1.start();
		t1.setActiveToTrue();
		
		try {
			Thread.sleep(5000);
			T2 t2 = new T2();
			t2.start();
			t2.setActiveToTrue();
			Thread.sleep(5000);
			t2.pause(5000);
			Thread.sleep(5000);
			t1.setActiveToFalse();
			Thread.sleep(5000);
			t2.stop();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			System.out.println("Thread testing successfull!");
			try {
				System.in.read();
			} catch (IOException e) {e.printStackTrace(); }
		}
	}
}