package at.ac.tuwien.complang.carfactory.businesslogic.xvsm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.CapiUtil;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceConstants;
import at.ac.tuwien.complang.carfactory.ui.constants.SpaceTimeout;

public class SpaceUtil{

	private Capi capi;
	private ContainerReference CarContainer;
	private ContainerReference BodyContainer;

	public SpaceUtil(){
		initSpace();
	}

	private void initSpace(){
		MzsCore core = DefaultMzsCore.newInstance(0);
		this.capi = new Capi(core);

		
		try {
			List<Coordinator> coords = new ArrayList<Coordinator>();
			coords.add(new AnyCoordinator());
			coords.add(new LabelCoordinator());
			coords.add(new KeyCoordinator());

			try {
				this.CarContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.CARCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
				this.BodyContainer = CapiUtil.lookupOrCreateContainer(SpaceConstants.BODYCONTAINER_NAME, new URI(SpaceConstants.CONTAINER_URI), coords, null, capi);
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

	public List<ICarPart> takeCarPart(String selectorLabel, Integer amount, long timeout, TransactionReference tx){
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(LabelCoordinator.newSelector(selectorLabel, MzsConstants.Selecting.COUNT_MAX));
		selectors.add(AnyCoordinator.newSelector(amount));
		List<ICarPart> parts = null;
		
		ContainerReference container  = null;
		if(selectorLabel.equals(CarPartType.BODY.toString())){
			container = BodyContainer;
		}else{
			container = CarContainer;
		}
			
		
		try {
			if (timeout == 0) {
				try {
					parts = capi.take(container, selectors, RequestTimeout.ZERO, tx);
				} catch (CountNotMetException ex) {
					return null;
				}
			} else if (timeout == SpaceTimeout.ZERO_WITHEXCEPTION) {
				parts = capi.take(container, selectors, RequestTimeout.ZERO, tx);
			} else if (timeout == SpaceTimeout.INFINITE) {
				parts = capi.take(container, selectors, RequestTimeout.INFINITE, tx);
			} else {
				parts = capi.take(container, selectors, timeout, tx);
			}
		} catch (CountNotMetException ex) {
			System.err.println("Not enough object found");
			try {
				capi.rollbackTransaction(tx);
			} catch (MzsCoreException e) {
				e.printStackTrace();
			}
		} catch (MzsTimeoutException e) {
			e.printStackTrace();
		} catch (TransactionException e) {
			e.printStackTrace();
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		if (!parts.isEmpty()) {
			return parts;
		} else {
			return null;
		}
	}

	
	public Capi getCapi() {
		return capi;
	}
	
	public ContainerReference getBodyContainer() {
		return BodyContainer;
	}
	public ContainerReference getCarContainer() {
		return CarContainer;
	}
	
}
