package at.ac.tuwien.complang.carfactory.businesslogic.xvsm;

import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class Painter extends SpaceUtil {

	/**
	 * Workflow:
	 * 1. Connect to the Mozard space
	 * 2. Load a Body (which is not yet painted) or an
	 *    assembled car (which is not yet painted)
	 * 3. Paint the Body or the Body associated with the car object
	 * 4. Save the painted part back into the space 
	 */
	public static long pid = 0;
	/** Half the time is used to take a part and the other half is used to paint it.
	 * We use this to relax the update intervals of the UI, so that there is no
	 * flickering, which happens, when a part is taken and written back immediately. */
	public static final long TIME_TO_PAINT = 1400L; //time in milliseconds
	private TransactionReference tx;
	private Color color;
	
	public Painter(long id, Color color) {
		super(); //1
		pid = id;
		this.color = color;
		
		while(true){
			doPaint();
		}
	}

	private void doPaint() {
		try {
			Thread.sleep(TIME_TO_PAINT/2);
		} catch (InterruptedException e) { }
		
		try {
			tx = getCapi().createTransaction(SpaceTimeout.TENSEC, new URI(SpaceConstants.CONTAINER_URI));
		} catch (MzsCoreException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		
		List<ICarPart> carparts =  takeCarPart(CarPartType.CAR.toString(), new Integer(1), SpaceTimeout.INFINITE, tx);
		if(carparts != null ){
			//paint car body write it to space
			try {
				Thread.sleep(TIME_TO_PAINT/2);
			} catch (InterruptedException e) { }
			Car c = (Car) carparts.get(0);
			c.getBody().setColor(pid, this.color);
			writeCarIntoSpace(c);
		} else {
			List<ICarPart> parts = takeCarPart(CarPartType.BODY.toString(), new Integer(1), SpaceTimeout.INFINITE, tx);
			//get body paint it write it
			
			if(parts != null){
				try {
					Thread.sleep(TIME_TO_PAINT/2);
				} catch (InterruptedException e) { }
				Body b = (Body) parts.get(0);
				b.setColor(pid, this.color);
				writeBodyIntoSpace(b);
			}
		}
	}

	private void writeBodyIntoSpace(Body b) {
		
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		String label =  SpaceLabels.PAINTEDBODY;
		cordinator.add(LabelCoordinator.newCoordinationData(label));
		cordinator.add(KeyCoordinator.newCoordinationData(""+b.getId()));
		try {
			getCapi().write(new Entry(b,cordinator), getBodyContainer(), SpaceTimeout.INFINITE, tx);
			getCapi().commitTransaction(tx);
		} catch (MzsCoreException e) {
			try {
				getCapi().rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		System.out.println("[Painter] Body " + b.getId() + " Painted and written in space");
	
	}

	private void writeCarIntoSpace(Car c) {

		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		String label =  SpaceLabels.PAINTEDCAR;
		cordinator.add(LabelCoordinator.newCoordinationData(label));
		cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		cordinator.add(FifoCoordinator.newCoordinationData());
		try {
			getCapi().write( new Entry(c,cordinator), getCarContainer(), SpaceTimeout.TENSEC, tx);
			getCapi().commitTransaction(tx);
		} catch (MzsCoreException e) {
			try {
				getCapi().rollbackTransaction(tx);
			} catch (MzsCoreException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		System.out.println("[Painter] Car " + c.getId() + " painted and written in space");

	}
}
