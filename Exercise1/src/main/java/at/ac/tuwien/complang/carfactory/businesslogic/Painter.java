package at.ac.tuwien.complang.carfactory.businesslogic;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class Painter extends SpaceUtil {

	/**
	 * TODO:
	 * 1. Connect to the mozard space
	 * 2. Load a Body (which is not yet painted) or an
	 *    assembled car (which is not yet painted)
	 * 3. Paint the Body or the Body associated with the car object
	 * 4. Save the painted part back into the space 
	 */
	public static long pid = 0;
	
	public Painter(){
		super(); //1
		pid++;
		
		while(true){
			doPaint();
		}
	}

	private void doPaint() {
		List<ICarPart> carparts =  takeCarPart(CarPartType.CAR.toString(), new Integer(1), SpaceTimeout.ZERO, null);
		if(carparts != null ){
			//paint car body write it to space
			
			Car c = (Car) carparts.get(0);
			c.getBody().setColor(pid, new Color(90,60,90));
			
			writeCarIntoSpace(c);
			
		}else{
			List<ICarPart> parts = takeCarPart(CarPartType.BODY.toString(), new Integer(1), SpaceTimeout.ZERO, null);
			//get body paint it write it
			
			if(parts != null){
				Body b = (Body) parts.get(0);
				b.setColor(pid, new Color(100,100,100));
				writeBodyIntoSpace(b);
			}
		}
		
	}

	private void writeBodyIntoSpace(Body b) {
		try {
			List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
			String label =  SpaceLabels.PAINTEDBODY;
			
			cordinator.add(LabelCoordinator.newCoordinationData(label));
			cordinator.add(KeyCoordinator.newCoordinationData(""+b.getId()));
			getCapi().write(getContainer(), new Entry(b,cordinator));
			System.out.println("[Painter] Body Painted and written in space");
			getNotifMgr().createNotification(getContainer(), this, Operation.WRITE);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}

	private void writeCarIntoSpace(Car c) {
		try {
			List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
			String label =  SpaceLabels.PAINTEDCAR;
			
			cordinator.add(LabelCoordinator.newCoordinationData(label));
			cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
			getCapi().write(getContainer(), new Entry(c,cordinator));
			System.out.println("[Painter] Car painted and written in space");
			getNotifMgr().createNotification(getContainer(), this, Operation.WRITE);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
}
