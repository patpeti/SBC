package at.ac.tuwien.complang.carfactory.application.xvsm;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.ui.xvsm.ISpaceListener;

public abstract class AbstractFactory implements IProducer {

	private Capi capi;
	private ContainerReference cref;
	public static long carPartId = 1;
	
	public AbstractFactory(Capi capi, ContainerReference cref) {
		super();
		this.capi = capi;
		this.cref = cref;
	}

	public Capi getCapi() {
		return capi;
	}

	public void setCapi(Capi capi) {
		this.capi = capi;
	}

	public ContainerReference getCref() {
		return cref;
	}

	public void setCref(ContainerReference cref) {
		this.cref = cref;
	}
}
