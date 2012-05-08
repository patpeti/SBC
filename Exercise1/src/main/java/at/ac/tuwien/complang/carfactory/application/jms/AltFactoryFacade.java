package at.ac.tuwien.complang.carfactory.application.jms;

import java.util.Hashtable;
import java.util.Map;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.xvsm.BodyFactory;
import at.ac.tuwien.complang.carfactory.application.xvsm.IProducer;
import at.ac.tuwien.complang.carfactory.application.xvsm.MotorFactory;
import at.ac.tuwien.complang.carfactory.application.xvsm.WheelFactory;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.ui.xvsm.ISpaceListener;

public class AltFactoryFacade {

	//Static Fields
	private static Map<ProducerType, AltFactoryFacade> factories;
	private static long next_id = 0;
	
	static {
		factories = new Hashtable<ProducerType, AltFactoryFacade>();
	}
	
	//Fields
	private int count;
	private boolean running = false;
	private Thread thread;
	private IProducer producer;
	
	private AltFactoryFacade(ProducerType type, IQueueListener listener) {
		next_id++;
		switch(type) {
			case BODY: producer = new AltBodyFactory(next_id, listener);	break;
			case WHEEL: producer = new AltWheelFactory(next_id, listener); break;
			case MOTOR: producer = new AltMotorFactory(next_id, listener); break;
			default: throw new IllegalArgumentException("Specificed ProducerType is not implemented");
		}
	}
	
	public static AltFactoryFacade getInstance(ProducerType type, IQueueListener listener) {
		if(factories.get(type) == null) {
			synchronized(AltFactoryFacade.class) {
				if(factories.get(type) == null) {
					AltFactoryFacade.factories.put(type, new AltFactoryFacade(type, listener));
				}
			}
		}
		return factories.get(type);
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
				delay = (int) (Math.random() * 3) + 1;
				total += delay;
				int millisecondsPerSecond = 1000;
				try {
					Thread.sleep(delay * millisecondsPerSecond);
				} catch (InterruptedException e) {
					System.err.println("Producer was interrupted.");
				}
				//TODO: Produce a Body...
				count--;
				producer.produce();
				System.out.println("Time to produce was " + delay + " seconds. Parts remaining: " + count);
			}
			System.out.println("All done. Average time to produce: " + total / (double) originalCount + " seconds.");
			AltFactoryFacade.this.running = false;
		}

	}

	public boolean isRunning() {
		return running;
	}

}
