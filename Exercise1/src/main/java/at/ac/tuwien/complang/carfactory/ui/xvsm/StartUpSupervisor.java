package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.application.workers.xvsm.Supervisor;

public class StartUpSupervisor {
	
	//Static Fields
	private static long id;
	private static boolean waitForSignal;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the Mozard space
		 * 2. Load a car from the Mozard space and verify its completed
		 * 3. Set the complete flag for the car
		 * 4. write it back into the space 		
		 */
		parseArguments(args);
		Supervisor supervisor = new Supervisor(id, waitForSignal);
		supervisor.start();
	}
	
	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpSupervisor.class.getName() + " --id=<id> [--signal]";
		if(args.length != 1 && args.length != 2) {
			System.out.println(usage);
			System.exit(1);
		}
		try {
			if(!args[0].startsWith("--id=")) {
				System.out.println(usage);
				System.exit(1);
			}
			String substring = args[0].substring(5);
			id = Integer.parseInt(substring);
		} catch(NumberFormatException e) {
			System.out.println(usage);
			System.exit(1);
		}
		if(args.length == 2) {
			if(args[1].equals("--signal")) {
				waitForSignal = true;
			}
		}
	}
}
