package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ConnectionClosedException;

import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Task;
import at.ac.tuwien.complang.carfactory.domain.Wheel;
import at.ac.tuwien.complang.carfactory.ui.IFactoryData;

/**
 * The QueueListener connects to all Queues and Topics
 * that are relevant for the UI and listens for incoming
 * messages. If there is a relevant message the Listener will
 * inform the UI by invoking the necessary update methods.
 * 
 * @author Sebastian Geiger
 */
public class QueueListenerImpl implements IQueueListener, MessageListener {
	
	//Fields
	private Session session;
	private Connection connection;
	private Topic motorTopic, bodyTopic, wheelTopic, paintedBodyTopic, carTopic, paintedCarTopic, defectTestedCarTopic, completenessTestedCarTopic;
	private Queue finishedCarQueue, defectCarQueue;
	private IFactoryData gui;
	
	public QueueListenerImpl() { }

	@Override
	public void onObjectWrittenInQueue(ICarPart carPart) {
		if(gui != null) {
			gui.addPart(carPart);
		}
	}
	
	@Override
	public void onTaskWrittenInQueue(Task task) {
		if(gui != null) {
			gui.addTask(task);
		}
	}

	public void setQueueObserver(IFactoryData gui) {
		this.gui = gui;
	}

	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			ICarPart part = (ICarPart) objectMessage.getObject();
			if(part instanceof Car) {
				System.out.println("[QueueListener] Received car (" + part.getId() + ")");
				Car car = (Car) part;
				Body body = car.getBody();
				Wheel[] wheels = car.getWheels();
				Motor motor = car.getMotor();
				boolean isTested = (car.getCompletenessTesterId() != -1 || car.getDefectTesterId() != -1);
				boolean hasProblem = (!car.isComplete() || car.isDefect());
				if(isTested && hasProblem) {
					handleDefectCar(car);
					if(car.getSupervisorId() != -1) System.out.println("HAVE ID: " + car.getSupervisorId());
				} else {
					handleSemiFinishedCar(car, body, wheels, motor);
				}
			} else {
				System.out.println("[QueueListener] Received part (" + part.getId() + ")");
				if(!gui.updatePart(part)) {
					gui.addPart(part);
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void handleDefectCar(Car car) {
		gui.removeCar(car);
		if(!gui.updateDefectCar(car)) {
			gui.addDefectCar(car);
		}
	}

	private void handleSemiFinishedCar(Car car, Body body, Wheel[] wheels,
			Motor motor) {
		if(!car.hasColor()) {
			gui.addCar(car);
			gui.removePart(body);
			gui.removePart(motor);
			for(Wheel wheel : wheels) {
				gui.removePart(wheel);
			}
		} else {
			//We need to know here if the car is already in the data model of the UI. Because it could be,
			//that we receive a car object here which was already in the data model (as unpainted car) or 
			//it could also be that we receive a car that was build with a painted body part and is thus 
			//already painted. In the later case, we still need to remove the parts from the data model,
			//that were used by the car.
			if(!gui.updateCar(car)) {
				gui.addCar(car);
				gui.removePart(body);
				gui.removePart(motor);
				for(Wheel wheel : wheels) {
					gui.removePart(wheel);
				}
			}
		}
	}
	
	@Override
	public void connectToQueues() {
		System.out.println("[QueueListener] Connecting to queues.");
		//connect to queues
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			MessageConsumer bodyConsumer = session.createConsumer(this.bodyTopic);
			this.motorTopic = session.createTopic(QueueConstants.MOTORTOPIC);
			MessageConsumer motorConsumer = session.createConsumer(this.motorTopic);
			this.wheelTopic = session.createTopic(QueueConstants.WHEELTOPIC);
			MessageConsumer wheelConsumer = session.createConsumer(this.wheelTopic);
			this.paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			MessageConsumer paintedBodyConsumer = session.createConsumer(this.paintedBodyTopic);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			MessageConsumer carConsumer = session.createConsumer(this.carTopic);
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			MessageConsumer paintedCarConsumer = session.createConsumer(this.paintedCarTopic);
			
			this.defectTestedCarTopic = session.createTopic(QueueConstants.DEFECT_TESTED_TOPIC);
			MessageConsumer defectTestedCarConsumer = session.createConsumer(this.defectTestedCarTopic);
			this.completenessTestedCarTopic = session.createTopic(QueueConstants.COMPLETENESS_TESTED_TOPIC);
			MessageConsumer completenessTestedCarTopic = session.createConsumer(this.completenessTestedCarTopic);
			
			this.finishedCarQueue = session.createQueue(QueueConstants.FINISHEDCARQUEUE);
			MessageConsumer finishedCarConsumer = session.createConsumer(this.finishedCarQueue);
			
			this.defectCarQueue = session.createQueue(QueueConstants.DEFECTCARQUEUE);
			MessageConsumer defectCarConsumer = session.createConsumer(this.defectCarQueue);
			System.out.println("[QueueListener] Queues connected");
			try {
				bodyConsumer.setMessageListener(this);
				motorConsumer.setMessageListener(this);
				wheelConsumer.setMessageListener(this);
				paintedBodyConsumer.setMessageListener(this);
				carConsumer.setMessageListener(this);
				paintedCarConsumer.setMessageListener(this);
				defectTestedCarConsumer.setMessageListener(this);
				completenessTestedCarTopic.setMessageListener(this);
				finishedCarConsumer.setMessageListener(this);
				defectCarConsumer.setMessageListener(this);
			} catch (JMSException e) {
				e.printStackTrace();
			}
			System.out.println("[QueueListener] Listener attached (listening for messages on all queues)");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void disconnect() {
		try {
			session.close();
			connection.close();
			System.out.println("Shutdown complete.");
		} catch (JMSException e) {
			if(e instanceof ConnectionClosedException) return;
			e.printStackTrace();
		}
	}
}
