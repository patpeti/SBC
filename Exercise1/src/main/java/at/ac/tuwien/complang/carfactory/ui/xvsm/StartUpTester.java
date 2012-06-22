package at.ac.tuwien.complang.carfactory.ui.xvsm;

import at.ac.tuwien.complang.carfactory.application.workers.xvsm.Tester;
import at.ac.tuwien.complang.carfactory.domain.TesterType;

public class StartUpTester {
	
	//Static Fields
	private static long id;
	private static boolean waitForSignal;
	private static TesterType type;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the Mozard space
		 * 2. Load a car from the Mozard space (in a transaction) and verify if it contains defect parts
		 * 3. Set the defect flag for the car if necessary
		 * 4. write it back into the space
		 */
		parseArguments(args);
		//Initialize tester: 
		Tester tester = new Tester(type, id, waitForSignal);
		tester.start();
	}

	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpSupervisor.class.getName() + " --id=<id> --type=<String> [--signal]\nValid string values are: [defectTester, completenessTester])";
		if(args.length != 2 && args.length != 3) {
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
			if(!args[1].startsWith("--type=")) {
				System.out.println(usage);
				System.exit(1);
			}
			String substring2 = args[1].substring(7).toUpperCase();
			if(substring2.equals("DEFECTTESTER")) {
				type = TesterType.DEFECTTESTER;
			} else if (substring2.equals("COMPLETENESSTESTER")) {
				type = TesterType.COMPLETETESTER;
			} else {
				System.out.println(usage);
				System.exit(1);
			}
		} catch(NumberFormatException e) {
			System.out.println(usage);
			System.exit(1);
		}
		if(args.length == 3) {
			if(args[2].equals("--signal")) {
				waitForSignal = true;
			}
		}
	}
}
