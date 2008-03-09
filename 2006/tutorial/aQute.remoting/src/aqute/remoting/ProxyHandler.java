package aqute.remoting;

import java.lang.reflect.*;

public class ProxyHandler implements InvocationHandler {
	RemoteHost	host;
	Object		sid;

	public ProxyHandler(RemoteHost host, Object sid) {
		this.host = host;
		this.sid = sid;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return host.call(sid,method.getName(),args);
	}
}
