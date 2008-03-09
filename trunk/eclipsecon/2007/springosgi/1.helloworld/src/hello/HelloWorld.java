package hello;

import org.osgi.framework.*;

public class HelloWorld implements BundleActivator {
	
	public void start(BundleContext context) throws Exception {
		System.out.println("Hello OSGi World!! "  );
	}
	
	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye OSGi World!!");
	}

}
