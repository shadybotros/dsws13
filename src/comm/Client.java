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
	private boolean connected = false;
	
	public void connect(String host, int port)
		throws UnknownHostException, IOException {
		if (connected) disconnect();
		socket = new Socket(host, port);
		in = socket.getInputStream();
		out = socket.getOutputStream();
		receiveBytes = new byte[131072];		/*  max msg size is 128 Kbyte */
		connected = true;
	}
	
	public void disconnect() throws IOException {
		if (!connected) return;
		connected = false;
		out.close();
		in.close();
		socket.close();
	}
	
	public boolean send(byte[] sendBytes) throws IOException {
		if (!connected) return false;
		out.write(sendBytes);
		out.write('\r');
		out.flush();
		return true;
	}
	
	public byte[] receive() throws IOException {
		if (!connected) return null;
		in.read(receiveBytes);
		return receiveBytes;
	}

	public boolean isConnected() {
		return connected;
	}
}
