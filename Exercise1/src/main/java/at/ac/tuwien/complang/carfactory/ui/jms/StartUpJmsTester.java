package at.ac.tuwien.complang.carfactory.ui.jms;

import java.io.IOException;

import at.ac.tuwien.complang.carfactory.businesslogic.jms.JmsTester;
import at.ac.tuwien.complang.carfactory.domain.TesterType;
import at.ac.tuwien.complang.carfactory.ui.xvsm.StartUpSupervisor;

public class StartUpJmsTester {
	
	//Static Fields
	private static long id;
	private static TesterType type;
	private static JmsTester tester;
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * 1. Connect to the Mozard space
		 * 2. Load a car from the Mozard space (in a transaction) and verify if it contains defect parts
		 * 3. Set the defect flag for the car if necessary
		 * 4. write it back into the space
		 */
		parseArguments(args);
		tester = new JmsTester(id, type);
		Thread worker = new Thread(tester);
		tester.initialize();
		worker.start();
		System.out.println("Tester has started in background.");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down gracefully, please wait.");
				tester.shutdown();
			}
		});
		//The following lines are needed to be able to kill the program from within eclipse.
		try {
			System.out.println("Press enter to terminate application");
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void parseArguments(String[] args) {
		String usage = "[Usage] " + StartUpSupervisor.class.getName() + " --id=<id> --type=<String> [defectTester, completenessTester])";
		if(args.length != 2) {
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
	}
}
