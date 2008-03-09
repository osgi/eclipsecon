package helloworld;

import org.osgi.framework.*;

public class Activator implements BundleActivator {
	
	public void start(BundleContext context) throws Exception {
		System.out.println("Hello Beatiful World!! "  );
	}
	
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye Ugly World!!");
	}

}
