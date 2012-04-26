package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.ui.ISpaceListener;

public abstract class AbstractFactory implements IProducer{

	private Capi capi;
	private ContainerReference cref;
	private ISpaceListener listener;
	
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

	public ISpaceListener getListener() {
		return listener;
	}

	public void setListener(ISpaceListener listener) {
		this.listener = listener;
	}
	
	

}
