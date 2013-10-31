package comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client implements ClientDelegate {
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private byte[] receiveBytes;
	
	public void connect(String host, int port)
		throws UnknownHostException, IOException {
		disconnect();
		socket = new Socket(host, port);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		receiveBytes = new byte[131072];		/*  max msg size is 128 Kbyte */
	}
	
	public void disconnect() throws IOException {
		if(out != null)
			out.close();
		if(in != null)
			in.close();
		if(socket != null)
			socket.close();
	}
	
	public boolean send(byte[] sendBytes) throws IOException {
		if(out == null) {
			return false;
		}
		out.write(sendBytes);
		out.write('\r');
		out.flush();
		return true;
	}
	
	public byte[] receive() throws IOException {
		if(in == null) {
			return null;
		}
		in.read(receiveBytes);
		return receiveBytes;
	}
}
