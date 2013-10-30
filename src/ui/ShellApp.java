package ui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import comm.Client;
import comm.ClientDelegate;
import org.apache.log4j.*;

public class ShellApp {
	
	private Logger logger;
	private ClientDelegate client;
	private String insufficientArgs = "Insufficient arguments\n\nUsage:";
	
	public void start() {
		initializeLogging();
		client = new Client();
		parse();
	}
	
	private void initializeLogging() {
		logger = Logger.getLogger(ShellApp.class);
		logger.setLevel(Level.ALL);
		/* initialize console logging*/
		SimpleLayout sL = new SimpleLayout();
		ConsoleAppender cA = new ConsoleAppender(sL);
		logger.addAppender(cA);
		/* initialize log file */
		String logDir = "logs/client.log";
		String pattern = "%d{ISO8601} %-5p [%t] %c: %m%n";
		PatternLayout pL = new PatternLayout(pattern);
		try {
			FileAppender fA = new FileAppender(pL, logDir, true);
			logger.addAppender(fA);
		} catch (IOException e) {
			logger.error(e.toString() + " while creating log file");
		}
	}
	
	private void parse() {
		Scanner scanner = new Scanner(System.in);
		String[] tokens;
		
		while(true) {
			System.out.print("EchoClient> ");
			tokens = scanner.nextLine().trim().split("[ ]+");
			tokens[0] = tokens[0].toLowerCase();
			
			/* connect <address> <port> */
			if(tokens[0].equals("connect")) {
				connect(tokens);
			}
			
			/* disconnect */
			else if(tokens[0].equals("disconnect")) {
				disconnect();
			}
			
			/* send <message> */
			else if(tokens[0].equals("send")) {
				send(tokens);
			}
			
			/* logLevel <level> */
			else if(tokens[0].equals("loglevel")) {
				logLevel(tokens);
			}
			
			/* help */
			else if(tokens[0].equals("help")) {
				System.out.println(help("all"));
			}
			
			/* quit */
			else if(tokens[0].equals("quit")) {
				disconnect();
				System.out.println("Program will exit now");
				break;
			}
			
			/* <anything else> */
			else {
				System.out.println("Unknown command\n" + help("all"));
			}
		}
	}
	
	private void connect(String[] tokens) {
		if(tokens.length < 3) {
			/* print help message for connect */
			System.out.println(insufficientArgs + help("connect"));
		} else {
			try {
				//client.connect("131.159.52.1", 50000);
				client.connect(tokens[1], Integer.parseInt(tokens[2]));
				System.out.println("EchoClient> " + unmarshall(client.receive()));
			} catch (UnknownHostException e) {
				logger.error(e.toString());
			} catch (IOException e) {
				logger.error(e.toString());
			} catch (NumberFormatException e) {
				logger.error(e.toString());
			}
		}
	}
	
	private void disconnect() {
		if(!client.isConnected()) {
			System.out.println("You are already disconnected from the echo server");
		} else {
			try {
				client.disconnect();
				System.out.println("Successfully disconnected from the echo server");
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}
	
	private void send(String[] tokens) {
		if(!client.isConnected()) {
			System.out.println("You are not connected to the echo server");
		} else if (tokens.length < 2) {
			/* print help message for send */
			System.out.println(insufficientArgs + help("send"));
		} else {
			String msg="";
			for(int i=1;i<tokens.length;i++){
				msg+=tokens[i]+" ";
			}
			try {
				if(client.send(marshall(msg))) {
					System.out.println("EchoClient> " + unmarshall(client.receive()));
				} else {
					logger.error("You are not connected to the echo server");
				}
			} catch (IOException e) {
				logger.error(e.toString());
			}
		}
	}
	
	private void logLevel(String[] tokens) {
		if (tokens.length < 2) {
			/* print help message for logLevel */
			System.out.println(insufficientArgs + help("logLevel"));
		} else {
			Level level = Level.toLevel(tokens[1].toUpperCase());
			if (level == Level.DEBUG && !tokens[1].toUpperCase().equals("DEBUG")) {
				/* print help message for logLevel */
				System.out.println("The provided argument is not a valid option\n"
						+ help("logLevel"));
			} else {
				logger.setLevel(level);
				System.out.println("Log level set to " + logger.getLevel().toString());
			}
		}
	}
	
	private String help(String command) {
		String msg = "";
		if (command == "all") {
			msg += "\nIntended Usage:\n";
		} if (command == "all" || command == "connect") {
			msg += "\nconnect <address> <port>\n"
				+ "Connects to the echo server\n"
				+ "<address> (string) Hostname or IP address of echo server\n"
				+ "<port> (integer) Port number of echo server\n";
		} if (command == "all" || command == "disconnect") {
			msg += "\ndisconnect\n"
				+ "Disconnects from echo server";
		} if (command == "all" || command == "send") {
			msg += "\nsend <message>\n"
				+ "Sends a text message to echo server\n"
				+ "<message> (string) A sequence of ASCII characters - max 128 Kbyte\n";
		} if (command == "all" || command == "logLevel") {
			msg += "\nlogLevel <level>\n"
				+ "Sets the logger to the specified log level\n"
				+ "<level> (string) can be ALL, DEBUG, INFO, WARN, ERROR, FATAL or OFF\n";
		} if (command == "all" || command == "quit") {
			msg += "\nquit\n"
				+ "Disconnects from echo server and exits the application\n";
		}
		return msg;
	}
	
	private byte[] marshall(String s) {
		return s.getBytes();
	}
	
	private String unmarshall(byte[] bytes) {
		String s = "";
		char c;
		for (int i = 0; i < bytes.length; ++i) {
			c = (char)bytes[i];
			s += c;
			if (c == '\r') {
				break;
			}
		}
		return s.replace("\n", "").replace("\r", "");
	}
}
