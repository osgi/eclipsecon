package aqute.remoteing;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import org.osgi.framework.*;
import org.osgi.util.tracker.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class Remoteing extends Thread implements BundleActivator {
	ServiceTracker	remotedServices;
	boolean			quit;
	ServerSocket	server;
	Set				hosts	= new HashSet();
	BundleContext	context;
	Discovery		discovery;
	
	public Remoteing() {
		super("Remoteing");
	}

	public void start(BundleContext context) throws Exception {
		this.context = context;
		discovery = new Discovery(this);
		Filter filter = context.createFilter("(remote=*)");
		remotedServices = new ServiceTracker(context, filter, null);
		remotedServices.open();
		start();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) {
		quit = true;
		discovery.close();
		remotedServices.close();
		try {
			server.close();
		}
		catch (IOException e) {
			// Ignore
		}
	}

	public void run() {
		while (!quit)
			try {
				discovery.start();
				server = new ServerSocket(0);
				while (!quit) {
					final Socket socket = server.accept();
					new Thread("remoteing transaction " + socket) {
						public void run() {
							try {
								request(socket);
							}
							catch (Exception e) {
								log("Request failed", e);
							}
						}
					}.start();
				}
			}
			catch (Throwable e) {
				try {
					if (!quit) {
						log("Error in processing remoteing, sleeping ", e);
						Thread.sleep(1000);
					}
				}
				catch (Exception ie) {
					// Ignore
				}

			}
	}

	protected void request(Socket socket) throws Exception {
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
		Object		result = null;
		
		Message message = Message.getMessage(in);
		try {
			switch (message.getCmd()) {
				case Message.CALL :
					Object[] parms = (Object[]) message.getData();
					Long sid = (Long) parms[0];
					String name = (String) parms[1];
					Object args[] = (Object[]) parms[2];
					Class[] types = getClasses(args);
	
					try {
						ServiceReference[] references = context
								.getAllServiceReferences(null, "(service.id=" + sid
										+ ")");
						if (references != null) {
							if (references.length != 1)
								log("The service id is supposed to be unique: "
										+ sid);
							
							// TODO security
							
							Object service = context.getService(references[0]);
							if (service != null) {
								Class clazz = service.getClass();
								Method method = clazz.getMethod(name, types);
								result = method.invoke(service, args);
							}
							else {
								log("No service object for " + sid + " for method "
										+ name);
							}
						}
						else {
							log("No service reference for " + sid + " for method "
									+ name);
						}
					}
					catch (Exception e) {
						log("Request failed for " + sid + " " + name, e);
					}
					break;
	
				default :
					log("Invalid request: " + message);
					break;
			}
		} catch(Throwable e ) {
			e.printStackTrace();
		}finally {
			Message reply = new Message(Message.REPLY, result);
			reply.writeTo(out);
			out.flush();
			//out.close();
			//in.close();
			//socket.close();
		}
	}

	private Class[] getClasses(Object[] args) {
		Class types[] = new Class[args.length];
		for (int i = 0; i < args.length; i++)
			types[i] = args[i].getClass();
		return types;
	}

	public Object call(InetAddress address, int port, Object sid, String name,
			Object[] args) throws Exception {
		Socket socket = null;
		try {
			Object parms[] = new Object[4];
			parms[0] = sid;
			parms[1] = name;
			parms[2] = args;
			Message message = new Message(Message.CALL, parms);
			socket = new Socket(address, port);
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			message.writeTo(out);
			out.flush();
			Message reply = Message.getMessage(in);

			socket.close();
			return reply.getData();
		}
		finally {
			if (socket != null)
				socket.close();
		}
	}

	private void log(String string, Throwable e) {
		System.out.println("Log: " + string + " : " + e);
		e.printStackTrace();
	}

	private void log(String string) {
		System.out.println("Log: " + string);
	}

	RemoteHost getHost(InetAddress address, int port) {
		for (Iterator i = hosts.iterator(); i.hasNext();) {
			RemoteHost rhost = (RemoteHost) i.next();
			if (rhost.is(address, port))
				return rhost;
		}
		RemoteHost rhost = new RemoteHost(this, address, port);
		hosts.add(rhost);
		return rhost;
	}

}
