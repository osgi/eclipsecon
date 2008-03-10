/* 
 * © Copyright 2008 IBM Corp.  All rights reserved.  
 * This source code is made available under the terms of the Eclipse Public License, v1.0. 
 */
package test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class HelloWorld implements BundleActivator {
    public static void main(String[] args) throws Exception {
    	HelloWorld hw = new HelloWorld();
    	hw.start(null);
    	hw.stop(null);
    }

	public void start(BundleContext context) throws Exception {
		System.out.println("Hello World!");
	}

	public void stop(BundleContext context) throws Exception {
		System.out.println("Goodbye World!");
	}
}