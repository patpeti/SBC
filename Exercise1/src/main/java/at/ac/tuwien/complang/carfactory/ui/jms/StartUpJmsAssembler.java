package at.ac.tuwien.complang.carfactory.ui.jms;

import java.io.IOException;

import at.ac.tuwien.complang.carfactory.application.workers.jms.JmsAssembler;

public class StartUpJmsAssembler {

	private static int id;
	private static boolean waitForSignal;
	private static JmsAssembler assembler; 
	
	public static void main(String[] args) {
		parseArguments(args);
		assembler = new JmsAssembler(id, waitForSignal);
		Thread worker = new Thread(assembler);
		assembler.initialize();
		worker.start();
		System.out.println("Assembler has started in background.");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down gracefully...");
				assembler.shutdown();
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
		String usage = "[Usage] " + StartUpJmsAssembler.class.getName() + " --id=<id> [-signal]";
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
