package aqute.remoteing;

import java.io.*;
import java.net.*;
import java.util.*;

import org.osgi.framework.*;

public class Discovery extends Thread {
	MulticastSocket	mcast;
	DatagramSocket	socket;
	Remoteing		remoteing;
	boolean			quit;
	InetAddress		group;
	int				ROSGI_PORT	= 2000;
	String			ROSGI_GROUP	= "239.0.0.0";

	Discovery(Remoteing remoteing) throws UnknownHostException {
		this.remoteing = remoteing;
		this.group = InetAddress.getByName(ROSGI_GROUP);
	}

	public void run() {
		while (!quit)
			try {
				mcast = new MulticastSocket(ROSGI_PORT);
				mcast.joinGroup(group);
				mcast.setSoTimeout(30000);
				Message m = new Message(Message.QUERY,null);
				send(m);
				while (!quit)
					try {
						byte[] buffer = new byte[2000];
						DatagramPacket packet = new DatagramPacket(buffer, 0,
								buffer.length);
						mcast.receive(packet);
						Message message = Message.getMessage(packet);
						switch (message.getCmd()) {
							case Message.ANNOUNCE :
								discover(packet.getAddress(),message);
								break;

							case Message.QUERY :
								announce();
								break;
						}
					}
					catch (InterruptedIOException e) {
						announce();
					}
					catch (Exception e) {
						if (!quit)
							e.printStackTrace();
					}
			}
			catch (Exception e) {
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
	}

	private void announce() throws Exception {
		ServiceReference references[] = remoteing.remotedServices
				.getServiceReferences();
		if (references == null)
			return;

		Properties properties[] = new Properties[references.length];
		for (int i = 0; i < references.length; i++) {
			properties[i] = convert(references[i]);
		}
		Object[] parms = new Object[3];
		parms[0] = InetAddress.getLocalHost();
		parms[1] = new Integer(remoteing.server.getLocalPort());
		parms[2] = properties;
		send(new Message(Message.ANNOUNCE, parms));
	}

	void discover(InetAddress address, Message message) throws IOException {
		Object[] parms = (Object[]) message.getData();
		InetAddress host = address;
		int port = ((Integer) parms[1]).intValue();
		if (host.equals(remoteing.server.getInetAddress())
				&& port == remoteing.server.getLocalPort())
			return;

		Properties[] properties = (Properties[]) parms[2];
		RemoteHost rhost = remoteing.getHost(host, port);
		rhost.announced(properties);
	}

	Properties convert(ServiceReference reference) {
		Properties props = new Properties();
		String[] keys = reference.getPropertyKeys();
		for (int k = 0; k < keys.length; k++) {
			props.put(keys[k], reference.getProperty(keys[k]));
		}
		return props;
	}

	void send(Message message) throws Exception {
		byte data[] = message.asBuffer();
		DatagramPacket packet = new DatagramPacket(data, 0, data.length);
		packet.setAddress(group);
		packet.setPort(ROSGI_PORT);
		mcast.send(packet);
	}

	public void close() {
		quit = true;
		mcast.close();
	}

}
