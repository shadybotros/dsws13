package ui;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import comm.Client;
import comm.ClientDelegate;
import org.apache.log4j.*;

/**
 * Class to prcoess the user entries, doing initial setups and communicating with the echo server.
 * 
 * @author Shady Yacoub, Amin Chawki, Dominik Figlestahler
 *
 */
public class ShellApp {
	
	private Logger logger;
	private ClientDelegate client;
	/**
	 *  Default string for insufficient arguments
	 */
	private String insufficientArgs = "Insufficient arguments\n\nUsage:";
	/**
	 * Hostname of echo server
	 */
	private String host;
	/**
	 *  Port of echo server
	 */
	private String port;
	
	/**
	 * Method to initialize logging, initialize ClientDelegate and initialize parsing.
	 */
	public void start() {
		initializeLogging();
		client = new Client();
		logger.info("App started. Logging initialized. ClientDelegate object created. Entering parse loop...");
		parse();
	}
	
	/**
	 * Method sets default logging level to ALL and initializes the log file to log user entries.
	 */
	private void initializeLogging() {
		logger = Logger.getLogger(ShellApp.class);
		logger.setLevel(Level.ALL);
		/* initialize console logging */
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
	
	/**
	 * Method reads user entries, tokenises them and calls suitable methods to handle the user request.
	 * It calls the help function if the user entry is not supported.
	 */
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
				if (host!=null)
					System.out.println("EchoClient> Connection terminated: "+ host + " / " + port);
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
		scanner.close();
	}
	
	/**
	 * Method tries to connect the client to a server specified by user entry.
	 * It calls the help function if the hostname or port of the server are not specified.
	 * Possible errors during connection establishment are logged to the logging file.
	 * @param tokens token[1] hostname of server, token[2] port of server.
	 */
	private void connect(String[] tokens) {
		if(tokens.length!=3) {
			/* print help message for connect */
			System.out.println("EchoClient> " + insufficientArgs + help("connect"));
		} else {
			try {
				//client.connect("131.159.52.1", 50000);
				client.connect(tokens[1], Integer.parseInt(tokens[2]));
				System.out.println("EchoClient> " + unmarshall(client.receive()));
				logger.info("Connected to " + tokens[1] + ":" + Integer.parseInt(tokens[2]));
				host = tokens[1];
				port = tokens[2];
			} catch (UnknownHostException e) {
				logger.error("IP address of the host could not be determined");
			} catch (IOException e) {
				logger.error("Failed to connect; please make sure you have internet connection and review the IP address and/or port number");
			} catch (NumberFormatException e) {
				logger.error("The second argument should be the port number");
			} catch(IllegalArgumentException e){
				logger.error("Invalid Arguments");
			}
		}
	}
	
	/**
	 * Methods tries to disconnect the client from the echo server.
	 * Possible error during connection clearing is logged to the logging file. 
	 */
	private void disconnect() {
		if(host!=null){
		try {
			client.disconnect();
			logger.info("Disconnected");
			host=null;
		} catch (IOException e) {
			logger.error("Failed to disconnect; you may be already disconnected");
		}
		}
		else{
			logger.error("You are not connected");
		}
	}
	
	/**
	 * Method tries to send the user message to the echo server by marshalling it.
	 * If a reply comes back it will be unmarshalled and displayed to the user.
	 * It calls the help function if there is no user message to be send.
	 * Possible errors during message sending are logged to the logging file.
	 * @param tokens tokens[i>=1] user message to be send to the echo server.
	 */
	private void send(String[] tokens) {
		if (tokens.length < 2) {
			/* print help message for send */
			System.out.println("Echo Client> " + insufficientArgs + help("send"));
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
	
	/**
	 * Method sets the logger to a logging level specified by user entry.
	 * It proofs that the logging level is set correctly.
	 * It calls the help function if the logging level is not supported.
	 * @param tokens token[1] new logging level
	 */
	private void logLevel(String[] tokens) {
		if (tokens.length != 2) {
			/* print help message for logLevel */
			System.out.println("Echo Client> " + insufficientArgs + help("logLevel"));
		} else {
			Level level = Level.toLevel(tokens[1].toUpperCase());
			if (level == Level.DEBUG && !tokens[1].toUpperCase().equals("DEBUG")) {
				/* print help message for logLevel */
				System.out.println("Echo Client> The provided argument is not a valid option\n"
						+ help("logLevel"));
			} else {
				logger.setLevel(level);
				logger.info("Log level set to " + logger.getLevel().toString());
				System.out.println("Echo Client> Log level set to " + logger.getLevel().toString());
			}
		}
	}
	
	/**
	 * Method informs the user about how to use the application.
	 * @param command String specified by calling method to return proper help message.
	 * @return help message to be displayed to the user.
	 */
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
	
	/**
	 * Methods to convert a user entry into a processible format for sending it to the echo server.
	 * @param s User entry to be send to the echo server.
	 * @return Byte array of given string.
	 */
	private byte[] marshall(String s) {
		return (s == null) ? null : s.getBytes();
	}
	
	/**
	 * Method to convert a reply from the echo server into a processible format to displaying it to the user.
	 * @param bytes Reply of echo server as byte array.
	 * @return String to the given byte array.
	 */
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
