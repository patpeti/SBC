package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.Connection;
import javax.jms.JMSException;

public abstract class JmsAbstractWorker {

	//Fields
	protected final long pid;
	protected Connection connection = null;

	public JmsAbstractWorker(long pid) {
		this.pid = pid;
	}
	
	public void initialize() {
		connectToQueues();
	}
	
	public void disconnect() {
		try {
			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	abstract void connectToQueues();
	abstract void startWorkLoop();
}
