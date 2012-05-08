package at.ac.tuwien.complang.carfactory.businesslogic.xvsm;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceLabels;

public class Supervisor implements NotificationListener{

	
	/**
	 * TODO:
	 * 1. Connect to the mozard space
	 * 2. Load a car from the mozardspace and verify its completed
	 * 3. Set the complete flag for the car
	 * 4. write it back into the space 		
	 */
	
	
	private Capi capi;
	private ContainerReference container;
	private NotificationManager notifMgr;
	private static long id = 0;
	
	public Supervisor() {
		id++;
		initSpace();
		while(true){
			readPaintedCar();
		}
	}
	
	private void readPaintedCar(){
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector());
		selectors.add(LabelCoordinator.newSelector(SpaceLabels.PAINTEDCAR, MzsConstants.Selecting.COUNT_MAX));
		selectors.add(AnyCoordinator.newSelector(1));
		
		List<ICarPart> parts = null;
		try {
			parts = capi.take(container, selectors, RequestTimeout.INFINITE, null);

		} catch (MzsCoreException e) {
			e.printStackTrace();
		}

		if(parts != null){
			Car c = (Car) parts.get(0);
			c.setComplete(id, true);
			writeCar(c);
		}
	}

	private void writeCar(Car c) {
		
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(SpaceLabels.FINISHEDCAR));
		cordinator.add(KeyCoordinator.newCoordinationData(""+c.getId()));
		//write
		try {
			capi.write(container, new Entry(c,cordinator));
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		
		//notify
		try {
			notifMgr.createNotification(container, this, Operation.WRITE);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);
		notifMgr = new NotificationManager(core);		
	
		this.container = null;
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());
			coords.add(new FifoCoordinator());						
			try {
				this.container = CapiUtil.lookupOrCreateContainer(SpaceConstants.CONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
			} catch (URISyntaxException e) {
				System.out.println("Error: Invalid container name");
				e.printStackTrace();
			}
				
		} catch (MzsCoreException e) {
			System.out.println("Error: Could not initialize Space");
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("[SpaceUtil]: Space initiated ");
		
	}

	@Override
	public void entryOperationFinished(Notification source,
			Operation operation, List<? extends Serializable> entries) {
		// TODO Auto-generated method stub
		
	}
	
}
