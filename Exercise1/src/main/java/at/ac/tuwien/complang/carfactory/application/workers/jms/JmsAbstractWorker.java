package at.ac.tuwien.complang.carfactory.application.workers.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ConnectionClosedException;

public abstract class JmsAbstractWorker implements Runnable, MessageListener {

	//Fields
	protected final long pid;
	protected Connection connection = null;
	protected boolean running, shutdownComplete;
	protected Object runningMutex; //An guarded lock mutex for the running state
	private boolean waitForSignal;
	
	private Topic signalTopic;
	private Session signalSession;

	public JmsAbstractWorker(long pid, boolean waitForSignal) {
		this.pid = pid;
		this.runningMutex = new Object();
		this.waitForSignal = waitForSignal;
	}
	
	public void initialize() {
		connectToQueues();
		/**************
		 *  ATTENTION:
		 *  If waitForSignal is set then the running flag will be set by the incoming start signal message.
		 *  If it the flag is not set, then we need to set it here right away, otherwise the work loop will
		 *  never commence!
		 *******************/
		if(!waitForSignal) {
			this.running = true;
		}
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
	
	public boolean getWaitForSignal() {
		return waitForSignal;
	}

	@Override
	public void run() {
		startWorkLoop();
	}

	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			String signal = (String) objectMessage.getObject();
			if(signal.equalsIgnoreCase("STOP")) {
				this.running = false;
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method blocks until it receives a start signal
	 */
	protected void waitForStartSignal() {
		try {
			MessageConsumer consumer= signalSession.createConsumer(this.signalTopic);
			System.out.println("Waiting for start signal.");
			ObjectMessage message = (ObjectMessage) consumer.receive();
			String signal = (String) message.getObject();
			if(signal.equalsIgnoreCase("START")) {
				this.running = true;
			}
			signalSession.setMessageListener(this);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
