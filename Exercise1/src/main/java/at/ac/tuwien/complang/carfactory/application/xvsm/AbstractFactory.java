package at.ac.tuwien.complang.carfactory.application.xvsm;

import java.util.Observable;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.application.IFactory;

public abstract class AbstractFactory extends Observable implements IProducer, IFactory {

	//Fields
	private int count;
	private boolean running = false;
	private Thread thread;
	private IProducer producer;
	private Capi capi;
	private ContainerReference cref;
	public static long carPartId = 1;
	
	public AbstractFactory(Capi capi, ContainerReference cref) {
		super();
		this.capi = capi;
		this.cref = cref;
	}

	public Capi getCapi() {
		return capi;
	}

	public void setCapi(Capi capi) {
		this.capi = capi;
	}

	public ContainerReference getCref() {
		return cref;
	}

	public void setCref(ContainerReference cref) {
		this.cref = cref;
	}
	
	public void start() {
		this.running = true;
		//start the timer task and produce bodies
		this.thread.start();
	}
	
	public void stop() {
		this.count = 0;
		this.thread.interrupt();
		this.running = false;
	}
	
	public void init(int count) throws IllegalStateException {
		if(running) {
			throw new IllegalStateException("Factory must be stopped first");
		}
		this.count = count;
		//Prepare TimerTask
		thread = new Thread(new Producer());
	}
	
	class Producer implements Runnable {

		public void run() {
			int originalCount = count;
			int delay = 0;
			int total = 0;
			while(count > 0) {
				//The producer sleeps for a random period between 1 and 3 seconds
				delay = (int) (Math.random() * timeInSec()) + 1;
				total += delay;
				int millisecondsPerSecond = 1000;
				try {
					Thread.sleep(delay * millisecondsPerSecond);
				} catch (InterruptedException e) {
					System.err.println("Producer was interrupted.");
				}
				//Produce a car part with the factory and decrease the counter
				count--;
				produce();
				System.out.println("Time to produce was " + delay + " seconds. Parts remaining: " + count);
			}
			System.out.println("All done. Average time to produce: " + total / (double) originalCount + " seconds.");
			AbstractFactory.this.running = false;
			finished();
		}
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * This function is called after the factory has produced
	 * the specified amount of parts. By default it does nothing,
	 * but this method can be overridden, to perform implementation
	 * specific task (such as cleaning up or disconnecting resources).
	 */
	public void finished() { }

	@Override
	protected synchronized void clearChanged() {
		super.clearChanged();
	}

	@Override
	protected synchronized void setChanged() {
		super.setChanged();
	}
}
