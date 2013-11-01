package comm;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Interface specifying the methods for the client class.
 * 
 * @author Shady Yacoub, Amin Chawki, Dominik Figlestahler
 *
 */
public interface ClientDelegate {
	public void connect(String string, int i) throws UnknownHostException, IOException;
	public void disconnect() throws IOException;
	public boolean send(byte[] sendBytes) throws IOException;
	public byte[] receive() throws IOException;
	public boolean isConnected();
}
