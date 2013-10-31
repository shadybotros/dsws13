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
	private String host;
	private String port;
	
	public void start() {
		initializeLogging();
		client = new Client();
		logger.info("App started. Logging initialized. ClientDelegate object created. Entering parse loop...");
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
			logger.error("Cannot access log file");
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
				System.out.println("EchoClient> Program will exit now");
				logger.info("Closing app...");
				break;
			}
			
			/* <anything else> */
			else {
				System.out.println("EchoClient> Unknown command\n" + help("all"));
			}
		}
	}
	
	private void connect(String[] tokens) {
		if(tokens.length < 3) {
			/* print help message for connect */
			System.out.println("EchoClient> "+insufficientArgs + help("connect"));
		} else {
			try {
				//client.connect("131.159.52.1", 50000);
				client.connect(tokens[1], Integer.parseInt(tokens[2]));
				System.out.println("EchoClient> " + unmarshall(client.receive()));
				logger.info("Connected to " + tokens[1] + ":" + Integer.parseInt(tokens[2]));
				host=tokens[1];
				port=tokens[2];
			} catch (UnknownHostException e) {
				logger.error("IP address of the host could not be determined");
			} catch (IOException e) {
				logger.error("Failed to connect; please make sure you have internet connection and review the IP address and/or port number");
			} catch (NumberFormatException e) {
				logger.error("The second argument should be the port number");
			}
		}
	}
	
	private void disconnect() {
<<<<<<< HEAD
		try {
			client.disconnect();
			System.out.println("Disconnected from the echo server");
			logger.info("Disconnected");
		} catch (IOException e) {
			logger.error("Failed to disconnect; you may be already disconnected");
=======
		if(!client.isConnected()) {
			System.out.println("EchoClient> You are already disconnected from the echo server");
		} else {
			try {
				client.disconnect();
				System.out.println("EchoClient> Connection terminated: "+host +" / "+port);
				logger.info("Disconnected");
			} catch (IOException e) {
				logger.error("Failed to disconnect; please try again");
			}
>>>>>>> c4867014a8c980483e4cc1d7653240b71b76ca49
		}
	}
	
	private void send(String[] tokens) {
<<<<<<< HEAD
		if (tokens.length < 2) {
=======
		if(!client.isConnected()) {
			System.out.println("EchoClient> You are not connected to an echo server");
		} else if (tokens.length < 2) {
>>>>>>> c4867014a8c980483e4cc1d7653240b71b76ca49
			/* print help message for send */
			System.out.println("EchoClient> "+insufficientArgs + help("send"));
		} else {
			String msg="", reply;
			for(int i=1;i<tokens.length;i++){
				msg+=tokens[i]+" ";
			}
			try {
				if(client.send(marshall(msg))) {
					logger.info("Message sent: '" + msg + "'");
					reply = unmarshall(client.receive());
					if(reply.equals("")) {
						logger.error("Did not get a reply; please  make sure you are connected to the server");
					} else {
						logger.info("Reply received: '" + reply + "'");
						System.out.println("EchoClient> " + reply);
					}
				} else {
					logger.error("Failed to send; please  make sure you are connected to the server");
				}
			} catch (IOException e) {
				logger.error("Failed to send; please make sure you are connected to the server");
			}
		}
	}
	
	private void logLevel(String[] tokens) {
		if (tokens.length < 2) {
			/* print help message for logLevel */
			System.out.println("EchoClient> "+insufficientArgs + help("logLevel"));
		} else {
			Level level = Level.toLevel(tokens[1].toUpperCase());
			if (level == Level.DEBUG && !tokens[1].toUpperCase().equals("DEBUG")) {
				/* print help message for logLevel */
				System.out.println("EchoClient> The provided argument is not a valid option\n"
						+ help("logLevel"));
			} else {
				logger.setLevel(level);
				logger.info("Log level set to " + logger.getLevel().toString());
				System.out.println("EchoClient> Log level set to " + logger.getLevel().toString());
			}
		}
	}
	
	private String help(String command) {
		String msg = "";
		if (command == "all") {
			msg += "\nIntended Usage:\n---------------";
		} if (command == "all" || command == "connect") {
			msg += "\nconnect <address> <port>\t"
				+ "Connects to the echo server\n"
				+ "<address>\t\t\t(string) Hostname or IP address of echo server\n"
				+ "<port>\t\t\t\t(integer) Port number of echo server\n---------------";
		} if (command == "all" || command == "disconnect") {
			msg += "\ndisconnect\t\t\t"
				+ "Disconnects from echo server\n---------------";
		} if (command == "all" || command == "send") {
			msg += "\nsend <message>\t\t\t"
				+ "Sends a text message to echo server\n"
				+ "<message>\t\t\t(string) A sequence of ASCII characters - max 128 Kbyte\n---------------";
		} if (command == "all" || command == "logLevel") {
			msg += "\nlogLevel <level>\t\t"
				+ "Sets the logger to the specified log level\n"
				+ "<level>\t\t\t\t(string) Log level: can be ALL, DEBUG, INFO, WARN, ERROR, FATAL or OFF\n---------------";
		} if (command == "all" || command == "quit") {
			msg += "\nquit\t\t\t\t"
				+ "Disconnects from echo server and exits the application\n---------------";
		}
		return msg;
	}
	
	private byte[] marshall(String s) {
		return (s == null) ? null : s.getBytes();
	}
	
	private String unmarshall(byte[] bytes) {
		if(bytes == null) {
			return "";
		}
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
