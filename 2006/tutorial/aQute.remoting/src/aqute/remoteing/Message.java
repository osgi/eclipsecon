package aqute.remoteing;

import java.io.*;
import java.net.*;

public class Message implements Serializable {
	private static final long	serialVersionUID	= 1L;

	public final static int			ANNOUNCE			= 1;
	static final int			CALL				= 3;
	static final int			REPLY				= 4;

	public static final int	QUERY	= 0;

	int							cmd;
	Object						data;
	InetAddress					from;
	int							port;
	Object						reply;

	public Message(int cmd, Object data) {
		this.cmd = cmd;
		this.data = data;
	}

	public static Message getMessage(DatagramPacket packet) throws Exception {
		Message message = getMessage(getStream(packet));
		message.from = packet.getAddress();
		message.port = packet.getPort();
		return message;
	}
	
	private static InputStream getStream(DatagramPacket packet) {
		byte data[] = packet.getData();
		int l = packet.getLength();
		int o = packet.getOffset();
		byte [] real = new byte[ l ];
		System.arraycopy(data, o, real, 0, l);
		return new ByteArrayInputStream(real);
	}

	public static Message getMessage(InputStream bin) throws Exception {
		ObjectInputStream in = new ObjectInputStream(bin);
		Message m = (Message) in.readObject();
		return m;
	}


	byte[] asBuffer() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		writeTo(bout);		
		return bout.toByteArray();
	}

	public int getCmd() {
		return cmd;
	}

	Object getData() {
		return data;
	}

	InetAddress getFrom() {
		return from;
	}

	int getPort() {
		return port;
	}

	public void writeTo(OutputStream bout) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(bout);
		out.writeObject(this);
		out.flush();
	}
	
}
