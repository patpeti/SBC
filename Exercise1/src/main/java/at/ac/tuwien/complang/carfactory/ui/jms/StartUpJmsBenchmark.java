package at.ac.tuwien.complang.carfactory.ui.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.producers.IFacade;
import at.ac.tuwien.complang.carfactory.application.producers.IFactory;
import at.ac.tuwien.complang.carfactory.application.producers.jms.JmsFactoryFacade;
import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.QueueListenerImpl;

public class StartUpJmsBenchmark {
	
	private Session session;
	private Connection connection;
	private Topic signalTopic;
	private MessageProducer messageProducer;
	
	public static void main(String[] args) {
		new StartUpJmsBenchmark().go();
	}
	
	public void go() {
		//instantiate global Listener
		final IQueueListener listener = new QueueListenerImpl();
		IFacade factoryFacade = JmsFactoryFacade.getInstance(listener);
		IFactory bodyFactory = factoryFacade.getInstance(ProducerType.BODY);
		bodyFactory.init(2000, 0.0);
		bodyFactory.start();
		IFactory motorFactory = factoryFacade.getInstance(ProducerType.MOTOR);
		motorFactory.init(2000, 0.0);
		motorFactory.start();
		IFactory wheelFactory = factoryFacade.getInstance(ProducerType.WHEEL);
		wheelFactory.init(8000, 0.0);
		wheelFactory.start();
		while(bodyFactory.isRunning() || motorFactory.isRunning() || wheelFactory.isRunning()) { 
			//busyloop to wait until the factories are finished
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Production is finished. Press ENTER to send signal to workers.");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		connectToQueues();
		try {
			//Send "START" signal to signalTopic
			ObjectMessage message = session.createObjectMessage();
			message.setObject(new String("START"));
			messageProducer.send(message);
		} catch(JMSException e) {
			
		}
		System.out.println("Workers are starting...");
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					stop();
					System.out.println("Workers are stopping...");
					timer.cancel();
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		timer.schedule(task, 60000);
	}
	
	public void stop() throws JMSException{
		//Send "STOP" to signalTopic
		ObjectMessage message = session.createObjectMessage();
		message.setObject(new String("STOP"));
		messageProducer.send(message);
	}
	
	public void connectToQueues() {
		System.out.println("[QueueListener] Connecting to queues.");
		//connect to queues
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.signalTopic = session.createTopic(QueueConstants.SIGNALTOPIC);
			this.messageProducer = session.createProducer(this.signalTopic);
			
			System.out.println("[QueueListener] Listener attached (listening for messages on all queues)");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
