package at.ac.tuwien.complang.carfactory.ui.jms;

import java.io.IOException;

import at.ac.tuwien.complang.carfactory.application.workers.jms.JmsSupervisor;

public class StartUpJmsSupervisor {

	//Static Fields
	private static int id;
	private static JmsSupervisor supervisor;
	
	public static void main(String[] args) {
		parseArguments(args);
		supervisor = new JmsSupervisor(id);
		Thread worker = new Thread(supervisor);
		supervisor.initialize();
		worker.start();
		System.out.println("Supervisor has started in background.");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down gracefully, please wait.");
				supervisor.shutdown();
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
		String usage = "[Usage] " + StartUpJmsAssembler.class.getName() + " --id=<id>";
		if(args.length != 1) {
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
	}
}
