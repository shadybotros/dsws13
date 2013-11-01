package comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Class to handle user entries by using socket streams.
 * 
 * @author Shady Yacoub, Amin Chawki, Dominik Figlestahler
 *
 */
public class Client implements ClientDelegate {
	private Socket socket;
	/**
	 *  Stream receiving replies from echo server
     */
	private InputStream in;
	/**
	 *  Stream receiving user entries 
	 */
	private OutputStream out;
	/**
	 *  Byte array received by echo server 
	 */
	private byte[] receiveBytes;
	/**
	 *  Boolean whether client is connected to server 
	 */
	private boolean connected;
	
	/**
	 * Method initializes stream socket to a server specified by user entry.
	 * @param host Hostname of echo server to connect to
	 * @param port Port of echo server to connect to
	 * @throws UnknownHostException IP address failure
	 * @throws IOException Connection failure
	 */
	public void connect(String host, int port)
		throws UnknownHostException, IOException {
		disconnect();
		socket = new Socket(host, port);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		receiveBytes = new byte[131072];		/*  max msg size is 128 Kbyte */
		connected=true;
		
	}
	
	/**
	 * Methods closes all stream sockets when disconnecting from the echo server.
	 * @throws IOException Disconnection failure
	 */
	public void disconnect() throws IOException {
		connected=false;
		if(out != null)
			out.close();
		if(in != null)
			in.close();
		if(socket != null)
			socket.close();
	}
	
	/**
	 * Method sends user entry to the echo server and clears the output stream. 
	 * @param sendBytes User entry to be send to the echo server as byte array.
	 * @return true if byte array could be send to echo server.
	 * @throws IOException Sending failure
	 */
	public boolean send(byte[] sendBytes) throws IOException {
		if(out == null) {
			return false;
		}
		out.write(sendBytes);
		out.write('\r');
		out.flush();
		return true;
	}
	
	/**
	 * Method reads byte array received by echo server.
	 * @return Reply of echo server as byte array.
	 * @throws IOException Receiving failure
	 */
	public byte[] receive() throws IOException {
		if(in == null) {
			return null;
		}
		in.read(receiveBytes);
		return receiveBytes;
	}
	/**
	 * Methods returns whether client is connected
	 * @return boolean
	 */
	public boolean isConnected(){
		return connected;
	}
}
