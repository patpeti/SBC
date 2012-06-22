package at.ac.tuwien.complang.carfactory.ui.jms;

import java.awt.Color;
import java.io.IOException;

import at.ac.tuwien.complang.carfactory.application.workers.jms.JmsPainter;

public class StartUpJmsPainter {
	private static int id;
	private static boolean waitForSignal;
	private static Color color;
	private static JmsPainter painter; 
	
	public static void main(String[] args) {
		parseArguments(args);
		painter = new JmsPainter(id, color, waitForSignal);
		Thread worker = new Thread(painter);
		painter.initialize();
		worker.start();
		System.out.println("Painter has started in background.");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down gracefully, please wait.");
				painter.shutdown();
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
		String usage = "[Usage] " + StartUpJmsAssembler.class.getName() + " --id=<id> --color=<color> [--signal]\nValid colors are: [RED, GREEN, BLUE]";
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
		} catch(NumberFormatException e) {
			System.out.println(usage);
			System.exit(1);
		}
		if(!args[1].startsWith("--color=")) {
			System.out.println(usage);
			System.exit(1);
		}
		String substring = args[1].substring(8).toUpperCase();
		if(substring.equals("RED")) {
			color = Color.RED;
		} else if (substring.equals("BLUE")) {
			color = Color.BLUE;
		} else if (substring.equals("GREEN")) {
			color = Color.green;
		} else {
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
