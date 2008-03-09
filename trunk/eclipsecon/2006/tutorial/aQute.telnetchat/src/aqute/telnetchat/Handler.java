package aqute.telnetchat;

import java.io.*;
import java.net.*;
import java.util.*;

import org.osgi.framework.*;
import org.osgi.service.log.*;

import aQute.service.channel.*;

public class Handler extends Thread implements Channel {
	BundleContext		ctxt;
	boolean				quit;
	Socket				socket;
	PrintWriter			writer;
	TelnetChat			parent;
	String				user;
	ServiceRegistration	channel;
	String line;

	public Handler(BundleContext context, Socket socket, TelnetChat activator)
			throws Exception {
		this.ctxt = context;
		this.socket = socket;
		this.parent = activator;
		writer = new PrintWriter(new OutputStreamWriter(socket
				.getOutputStream()));
	}

	public void send(String source, String msg) throws IOException {
		writer.println(source + "> " + msg);
		writer.flush();
	}

	void close() {
		try {
			quit = true;
			writer.close();
			socket.close();
		}
		catch (IOException e) {
			// Ignore in close
		}
	}
	
	public void run() {
		writer.println("Welcome to the aQute Telnet Chat");
		writer.print("Enter name: ");
		writer.flush();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			while (!quit && (line = reader.readLine()) != null) {
				line = line.trim();
				process(line);
			}
		}
		catch (Exception e) {
			if (!quit)
				parent.log
						.log(LogService.LOG_ERROR, "reading user input", e);
		}
		finally {
			if ( channel != null )
				channel.unregister();
			parent.remove(this);
			parent = null;
			close();
		}
	}

	void process(String line) throws IOException, Exception {
		if ( user == null) {
			user = line;
			Hashtable props = new Hashtable();
			props.put(Channel.CH_NAME, user);
			channel = ctxt.registerService(Channel.class.getName(), this,
					props);
			send("info", "User set to: " + user);
		}
		else {
			if (line.startsWith("/quit")) {
				writer.println("bye ");
			}
			else
				dispatch(line);
		}
	}

	void dispatch(String line) throws Exception {
		String parts[] = line.split("\\W");

		ServiceReference channels[] = ctxt
				.getServiceReferences(Channel.class.getName(), "("
						+ Channel.CH_NAME + "=" + parts[0] + ")");
		if (channels != null) {
			Channel toChannel = (Channel) ctxt.getService(channels[0]);
			toChannel.send(user, line.substring(parts[0].length()));
			ctxt.ungetService(channels[0]);
		}
		else {
			send("error", "no such user: " + parts[0]);
		}
	}

}
