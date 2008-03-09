package aQute.chat;

import java.io.*;
import java.net.*;
import java.util.*;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

import aQute.service.channel.*;

public class Chat {
	Channel				user;
	String				name;
	BundleContext		cntxt;
	ServiceTracker		channels;
	Map					bdds	= new TreeMap();
	String				lastTo;
	ServiceRegistration	registration;

	public Chat(BundleContext context, final Channel user) {
		this.user = user;
		this.cntxt = context;
		channels = doChannelTracker(context, user);
		channels.open();
	}

	ServiceTracker doChannelTracker(BundleContext context, final Channel user) {
		return new ServiceTracker(context, Channel.class.getName(), null) {
			public Object addingService(ServiceReference ref) {
				Channel buddy = (Channel) context.getService(ref);
				if (buddy != user) {
					String name = (String) ref.getProperty(Channel.CH_NAME);
					String rn = name;
					int n = 0;
					synchronized (bdds) {
						while (bdds.containsKey(rn))
							rn = name + "-" + n++;
						bdds.put(rn, buddy);
					}
				}
				return buddy;
			}

			public void removedService(ServiceReference ref, Object buddy) {
				bdds.remove(ref.getProperty(Channel.CH_NAME));
			}
		};
	}

	public void login(String name, String password) throws IOException {
		if (registration != null)
			registration.unregister();
		registration = null;
		this.name = name;
		Hashtable properties = new Hashtable();
		properties.put(Constants.SERVICE_PID, "pid:chat["
				+ InetAddress.getLocalHost() + "]:" + name);
		properties.put(Channel.CH_NAME, name);
		properties.put("remote", "*");
		registration = cntxt.registerService(Channel.class.getName(), user,
				properties);
		user.send("", "Logged in as " + name);
	}

	public void execute(String line) throws IOException {
		if (!line.startsWith("/"))
			send(line);
		else {
			String ws[] = line.split("\\s+");
			if ("/buddies".startsWith(ws[0]))
				doBuddies();
			else
				if ("/help".startsWith(ws[0]))
					doHelp();
				else
					if ("/login".startsWith(ws[0]))
						login(ws[1], null);
					else {
						lastTo = ws[0].substring(1);
						send(line.substring(ws[0].length() + 1));
					}
		}
	}

	void send(String line) throws IOException {
		if ( lastTo == null )
			user.send("", "No buddy");
		else {
			Channel channel = (Channel) bdds.get(lastTo);
			if (channel != null) {
				channel.send(name, line);
				user.send(name, line);
			}
			else
				user.send("?", "Can't find " + lastTo);
		}
	}

	void doBuddies() throws IOException {
		StringBuffer sb = new StringBuffer();
		String del = "";
		for (Iterator i = bdds.keySet().iterator(); i.hasNext();) {
			sb.append(del);
			sb.append(i.next());
			del = ", ";
		}
		user.send("", sb.toString());
	}


	public synchronized String[] getBuddies() {
		return (String[]) bdds.keySet().toArray(new String[0]);
	}

	void doHelp() throws IOException {
		user.send("", "You are logged in as: " + name);
		user.send("", "/bdds          - List current online bdds");
		user.send("", "/help             - This message");
		user.send("", "/login <id> <pw>  - Login under id");
		user.send("", "/<name> ...       - Send msg to buddy");
		user.send("", "...               - Send msg to last used buddy");
	}

	public void close() {
		ServiceRegistration reg;
		synchronized (this) {
			reg = registration;
			if (reg == null)
				return;
			registration = null;
		}
		reg.unregister();
	}

	public String getName() {
		return name;
	}
}
