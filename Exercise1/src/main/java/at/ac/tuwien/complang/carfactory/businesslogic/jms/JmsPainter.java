package at.ac.tuwien.complang.carfactory.businesslogic.jms;

import java.awt.Color;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.application.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public class JmsPainter extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic carTopic, paintedBodyTopic, paintedCarTopic;
	private Queue bodyQueue;
	private MessageConsumer bodyConsumer, carConsumer;
	private Color color;
	
	public JmsPainter(long id, Color color) {
		super(id);
		this.color = color;
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
			this.bodyQueue = session.createQueue(QueueConstants.BODYQUEUE);
			this.bodyConsumer = session.createConsumer(bodyQueue);
			this.paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			this.carConsumer = session.createConsumer(carTopic);
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void startWorkLoop() {
		//the assembly loop never terminates. As long as the assembler is running, it will paint cars and bodys.
		while(true) {
			try {
				//Try to get a car from the car Queue (one which is not yet painted)
				
				ObjectMessage objectMessage = null;
				while(objectMessage == null) {
					//System.out.println("LOOOPING... POLLING...");
					objectMessage = (ObjectMessage) carConsumer.receive(1);
					if(objectMessage == null) {
						objectMessage = (ObjectMessage) bodyConsumer.receive(1); //ich glaube der painter bleibt hier haengen, wenn es keine bodies gibt...und wenn du auch ein timeout einstellest?
						//dann gibt es diese nullpointer exception...
					}
				}
				//das problem bei der loesung ist, das man so immer am POLLEN ist, im loop, das ist nicht besonders effizient!!!!, im XVSM haben wird das so gemacht!!!
				Serializable object = (Serializable) objectMessage.getObject(); //wenn ich den timeout verwende, dann ist das objectMessage null, man koennte es so machen...
				if(object instanceof Car) {
					Car car = (Car) object;
					if(car.getPaintState() == PaintState.PAINTED) {
						throw new RuntimeException("PAINTED cars should only be in the painted car queue");
					}
					car.setPaintState(PaintState.PAINTED);
					car.setColor(pid, color);
					MessageProducer messageProducer;
					try {
						messageProducer = session.createProducer(paintedCarTopic);
						messageProducer.send(session.createObjectMessage(object));
						System.out.println("[Painter] Painted car " + car.getId() + " send to paintedCarTopic");
					} catch(JMSException e) {
						e.printStackTrace();
					}
				} else if (object instanceof Body) {
					Body body = (Body) object;
					if(body.getPaintState() == PaintState.PAINTED) {
						throw new RuntimeException("PAINTED cars should only be in the painted car queue");
					}
					body.setPaintState(PaintState.PAINTED);
					body.setColor(pid, color);
					MessageProducer messageProducer;
					try {
						messageProducer = session.createProducer(paintedBodyTopic);
						messageProducer.send(session.createObjectMessage(object));
						System.out.println("[Painter] Painted body " + body.getId() + " send to paintedBodyTopic");
					} catch(JMSException e) {
						e.printStackTrace();
					}
				}
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
