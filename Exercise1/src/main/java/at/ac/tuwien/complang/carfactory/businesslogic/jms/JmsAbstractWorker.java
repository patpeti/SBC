package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ConnectionClosedException;

public abstract class JmsAbstractWorker implements Runnable {

	//Fields
	protected final long pid;
	protected Connection connection = null;
	protected boolean running, shutdownComplete;
	protected Object runningMutex; //An guarded lock mutex for the running state

	public JmsAbstractWorker(long pid) {
		this.pid = pid;
		this.runningMutex = new Object();
	}
	
	public void initialize() {
		connectToQueues();
		this.running = true;
	}
	
	public void shutdown() {
		this.running = false;
		while (!shutdownComplete) {
			synchronized(runningMutex) {
				try {
					this.runningMutex.wait(1000);
					shutdownComplete = true;
					disconnect();
				} catch (InterruptedException e) { }
			}
		}
		System.out.println("Shutdown complete.");
	}
	
	protected void disconnect() {
		try {
			connection.close();
		} catch (JMSException e) {
			if(e instanceof ConnectionClosedException) return;
			e.printStackTrace();
		}
	}
	
	abstract void connectToQueues();
	abstract void startWorkLoop();
	
	@Override
	public void run() {
		startWorkLoop();
	}
}
