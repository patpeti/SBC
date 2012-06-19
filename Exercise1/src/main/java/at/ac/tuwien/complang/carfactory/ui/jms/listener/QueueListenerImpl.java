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

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
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
	private Queue finishedCarQueue;
	private IFactoryData gui;
	
	public QueueListenerImpl() { }

	@Override
	public void onObjectWrittenInQueue(ICarPart carPart) {
		gui.addPart(carPart);
	}
	
	@Override
	public void onTaskWrittenInQueue(Task task) {
		gui.addTask(task);
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
				System.out.println("Car (" + part.getId() + ") was received, im going to update the GUI.");
				Car car = (Car) part;
				Body body = car.getBody();
				Wheel[] wheels = car.getWheels();
				Motor motor = car.getMotor();
				if(!car.hasColor()) {
					gui.addCar(car);
					gui.removePart(body);
					gui.removePart(motor);
					for(Wheel wheel : wheels) {
						gui.removePart(wheel);
					}
				} else {
					//We need to know here if the car is already in the data model of the ui. Because it could be,
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
			} else {
				System.out.println("Part" + part.getId() + " was received, im going to update the GUI...");
				if(!gui.updatePart(part)) {
					gui.addPart(part);
				}
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void connectToQueues() {
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
				//session.setMessageListener(this);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
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
			connection.close();
			System.out.println("Shutdown complete.");
		} catch (JMSException e) {
			if(e instanceof ConnectionClosedException) return;
			e.printStackTrace();
		}
	}
}
