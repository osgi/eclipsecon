package aqute.remoting;

import java.lang.reflect.Proxy;
import java.net.*;
import java.util.*;

import org.osgi.framework.*;

public class RemoteHost {
	InetAddress			address;
	int					port;
	Map					services		= new HashMap();
	long				modified;
	Remoting			parent;
	final static Class	EMPTY_CLASS[]	= new Class[0];

	public RemoteHost(Remoting parent, InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.parent = parent;
	}

	public boolean is(InetAddress address, int port) {
		return this.address.equals(address) && this.port == port;
	}

	public void announced(Properties properties[]) {
		Map nextGen = new HashMap();
		modified = System.currentTimeMillis();
		for (int i = 0; i<properties.length; i++ ) {
			Properties props = properties[i];
			Object sid = props.get(Constants.SERVICE_ID);			
			ServiceRegistration service = (ServiceRegistration) services
					.get(sid);
			if (service == null) {
				String classNames[] = (String[]) props.get(Constants.OBJECTCLASS);
				Class[] interfaces = getInterfaces(classNames);
				String[] names = getInterfaceNames(interfaces);
				Object instance = Proxy.newProxyInstance(getClass()
						.getClassLoader(), interfaces, new ProxyHandler(this,sid));
				props.remove("remote");
				service = parent.context.registerService(names, instance, props);
				services.put(sid,service);
			} else {
				// TODO Do we update the properties?
			}
			nextGen.put(sid, service);
		}
		purge(nextGen);
	}

	private void purge(Map nextGen) {
		Set set = new HashSet(services.values());
		set.removeAll(nextGen.values());
		services = nextGen;
		for (Iterator i = set.iterator(); i.hasNext();) {
			ServiceRegistration proxy = (ServiceRegistration) i.next();
			proxy.unregister();
		}
	}

	private String[] getInterfaceNames(Class[] interfaces) {
		String names[] = new String[interfaces.length];
		for (int n = 0; n < names.length; n++) {
			names[n] = interfaces[n].getName();
		}
		return names;
	}

	private Class[] getInterfaces(String[] classNames) {
		List list = new ArrayList();
		for (int c = 0; c < classNames.length; c++) {
			try {
				Class clazz = Class.forName(classNames[c]);
				if (clazz.isInterface())
					list.add(clazz);
			}
			catch (Exception e) {
				// Ignore
			}
		}
		Class interfaces[] = (Class[]) list.toArray(EMPTY_CLASS);
		return interfaces;
	}

	public Object call(Object sid, String name, Object[] args) throws Exception {
		return parent.call( address, port, sid, name, args );
	}
}
