package at.ac.tuwien.complang.carfactory.application.workers.jms;

import java.awt.Color;
import java.io.Serializable;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;

import at.ac.tuwien.complang.carfactory.application.enums.PaintState;
import at.ac.tuwien.complang.carfactory.application.producers.jms.constants.QueueConstants;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.Task;

public class JmsPainter extends JmsAbstractWorker {

	//Fields
	private Session session;
	private Topic carTopic, paintedBodyTopic, paintedCarTopic, bodyTopic;
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
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
		ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
		policy.setQueuePrefetch(0);
		connectionFactory.setPrefetchPolicy(policy);
		try {
			connection = connectionFactory.createConnection();
			connection.setClientID("painter_" + this.pid);
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			//createQueue connects to a queue if it exists otherwise creates it
			this.bodyTopic = session.createTopic(QueueConstants.BODYTOPIC);
			this.bodyConsumer = session.createDurableSubscriber(bodyTopic, "bodySubscriber");
			this.paintedBodyTopic = session.createTopic(QueueConstants.PAINTEDBODYTOPIC);
			this.carTopic = session.createTopic(QueueConstants.CARTOPIC);
			this.paintedCarTopic = session.createTopic(QueueConstants.PAINTEDCARTOPIC);
			this.carConsumer = session.createDurableSubscriber(this.carTopic, "carSubscriber");
			//TODO connect to tasktopic
			System.out.println("Queues connected");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void startWorkLoop() {
		
		while(running) {
			try {
				//get task where task.color = this.color
				Task task = readFirstTask();
				
				if(task == null){
					normalLoop();
				}else{
					//try to get auto where taskid = task.id
					Car car = null;
					
					if(car == null){
						normalLoop(); //get any other car o a body to paint
						//in this case the task is updated by the assembler
					}else{
						car.setColor(pid, color);
						//write car
						//update task
						task.increasePaintAmount(1);
						//writetask
					}
					
				}
				
				
				
				
				
			} catch (JMSException e) {
				if(e instanceof IllegalStateException) break;
				e.printStackTrace();
			}
		}
		//disconnect from queue
		disconnect();
		shutdownComplete = true;
		synchronized(runningMutex) {
			runningMutex.notifyAll();
		}
	}
	
	private Task readFirstTask() {
		//TODO get task where task.color = this.color
		return null;
	}

	private void normalLoop() throws JMSException{
		
		ObjectMessage objectMessage = null;
		while(objectMessage == null) {
			if(!running) return;
			//Try to get a car from the car Queue (one which is not yet painted)
			objectMessage = (ObjectMessage) carConsumer.receive(1);
			if(objectMessage == null) {
				objectMessage = (ObjectMessage) bodyConsumer.receive(1);
			}
			if(objectMessage == null) {
				//Sleep 500ms if both message queues are empty
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) { }
			}
		}
		Serializable object = (Serializable) objectMessage.getObject();
		if(object instanceof Car) {
			Car car = (Car) object;
			if(car.getPaintState() == PaintState.PAINTED) {
				throw new RuntimeException("PAINTED cars should only be in the painted car queue");
			}
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
				throw new RuntimeException("PAINTED bodies should only be in the painted bodies queue");
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
		
	}
	
	
}
