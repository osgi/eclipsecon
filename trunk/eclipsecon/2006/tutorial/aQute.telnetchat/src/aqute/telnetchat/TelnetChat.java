package aqute.telnetchat;

import java.net.*;
import java.util.*;

import org.osgi.service.component.*;
import org.osgi.service.log.*;

public class TelnetChat extends Thread {
	ServerSocket		server;
	boolean				quit;
	ComponentContext	context;
	Set					handlers	= new HashSet();
	LogService			log;

	protected void activate(ComponentContext context) {
		this.context = context;
		log = (LogService) context.locateService("LOG");
		start();
	}

	public void deactivate(ComponentContext context) throws Exception {
		quit = true;
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			Handler handler = (Handler) i.next();
			try {
				handler.close();
			}
			catch (Throwable e) {// We are closing
			}
		}
		server.close();
	}

	public void run() {
		while (!quit)
			try {
				server = new ServerSocket(2030);
				server.setSoTimeout(1000);
				loop();
			}
			catch (Exception e) {
				log.log(LogService.LOG_ERROR,
						"[TelnetChat] Inner server listen loop", e);
				sleep(10000);
			}
	}

	void loop() throws Exception {
		while (!quit)
			try {
				Socket socket = server.accept();
				Handler handler = new Handler(context.getBundleContext(),
						socket, this);
				handlers.add(handler);
				handler.start();
			}
			catch (SocketTimeoutException e) {
				// Just for checking the quit flag
				// at a regular basis
			}
	}

	void remove(Handler handler) {
		handlers.remove(handler);
	}

	void sleep(int ms) {
		try {
			Thread.sleep(ms);
		}
		catch (InterruptedException e1) {
		}
	}
}
