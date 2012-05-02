package at.ac.tuwien.complang.carfactory.alternative;

import at.ac.tuwien.complang.carfactory.alternative.altGui.AltProductionUI;
import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.QueueListenerImpl;

public class StartUpAlternativeGui {
	
	
	public static void main(String[] args) {
		
		//insatnciate globale Listener
		IQueueListener listener = new QueueListenerImpl();
		//1. Start the User interface
		AltProductionUI gui = new AltProductionUI(listener);
		listener.setQueueObserver(gui);
	}
}
