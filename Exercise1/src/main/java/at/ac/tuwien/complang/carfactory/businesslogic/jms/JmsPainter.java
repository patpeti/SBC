package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Car;

public class JmsPainter extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic carTopic, bodyTopic;
	private MessageConsumer bodyConsumer, carConsumer;
	
	public JmsPainter(long id) {
		super(id);
		/**
		 * Workflow:
		 * 1. Connect to the Car and Body Topics
		 * 2. Try to take a unpainted car
		 * 3. Try to take an unpainted body
		 * 4. Paint the body (of the car)
		 * 5. Write the body back to the topic (body or car topic)
		 * 6. notify GUI (gui should update the part with the painter id
		 */
	}
	
	@Override
	protected void connectToQueues() {
		//test get Motor
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			this.bodyConsumer = session.createConsumer(bodyTopic);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			this.carConsumer = session.createConsumer(carTopic);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void startAssemblyLoop() {
		while(true) {
			try {
				//Try to get a car from the car Queue (one which is not yet painted)
				ObjectMessage objectMessage = (ObjectMessage) carConsumer.receive(100);
				Car car = (Car) objectMessage.getObject();
				if(car.getPaintState() == PaintState.UNPAINTED) {
					car.setPaintState(PaintState.PAINTED);
					//TODO: write it to the PAINTEDCARQUEUE
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
