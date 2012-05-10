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
import javax.jms.TopicSubscriber;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.application.jms.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class QueueListenerImpl implements IQueueListener, MessageListener {
	
	//Fields
	private Session session;
	private Connection connection;
	private Topic paintedBodyTopic, carTopic, paintedCarTopic;
	private Queue finishedCarQueue;
	private IQueueObserver gui;
	
	public QueueListenerImpl() { }

	public void onObjectWrittenInQueue(ICarPart carPart) {
		gui.onQueueChange(carPart, QueueChangeType.WRITE);
	}

	public void setQueueObserver(IQueueObserver gui) {
		this.gui = gui;
	}

	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			ICarPart part = (ICarPart) objectMessage.getObject();
			if(part instanceof Car) {
				System.out.println("Car (" + part.getId() + ") was received, im going to tell the GUI...");
				Car car = (Car) part;
				if(!car.hasColor()) {
					gui.addCar(car);
					//TODO: remove parts...
					Body body = car.getBody();
					Wheel[] wheels = car.getWheels();
					Motor motor = car.getMotor();
					gui.removePart(body);
					gui.removePart(motor);
					for(Wheel wheel : wheels) {
						gui.removePart(wheel);
					}
				} else {
					gui.addOrUpdateCar(car);
				}
				//update the semi/finished table model with the car object
				//If we receive a car, we must backtrack the parts used in the car to know what we should remove from the parts list
				//1. Car is not complete and not painted (from the carTopic)
				//2. Car is painted but not completed (from the paintedCarTopic)
				//3. Car is painted and completed (from the finishedCarTopic). This case has no representation in the GUI yet!
			} else {
				// The only ICarPart object we can receive which is not a car, 
				// is a painted body, in this case, we need to update the body in 
				// the spaceDataTableModel.
				Body body = (Body) part;
				System.out.println("Body " + body.getId() + " was received, im going to tell the GUI...");
				gui.updatePart(body);
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connectToQueues() {
		//connect to queues
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			MessageConsumer paintedBodyConsumer = session.createConsumer(this.paintedBodyTopic);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			MessageConsumer carConsumer = session.createConsumer(this.carTopic);
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			MessageConsumer paintedCarConsumer = session.createConsumer(this.paintedCarTopic);
			this.finishedCarQueue = session.createQueue(QueueConstants.FINISHEDCARQUEUE);
			MessageConsumer finishedCarConsumer = session.createConsumer(this.finishedCarQueue);
			System.out.println("[QueueListener] Queues connected");
			try {
				paintedBodyConsumer.setMessageListener(this);
				carConsumer.setMessageListener(this);
				paintedCarConsumer.setMessageListener(this);
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
}
