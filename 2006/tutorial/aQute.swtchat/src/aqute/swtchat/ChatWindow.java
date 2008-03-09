package aqute.swtchat;

import java.io.*;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.osgi.service.component.*;

import aQute.chat.*;
import aQute.service.channel.*;

/**
 */
public class ChatWindow extends Thread implements KeyListener, Runnable,
		Channel {
	Shell	shell;
	Text	text;
	boolean	quit;
	Text	line;
	Display	display;
	Chat	chat;

	protected void activate(ComponentContext context) throws Exception {
		this.chat = new Chat(context.getBundleContext(), this);
		start();
	}

	protected void deactivate(ComponentContext context) throws Exception {
		quit = true;
		interrupt();
	}

	void createShell() {
		shell = new Shell();
		shell.setText("SWT Chat");
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		shell.setSize(500, 300);
		shell.setLayout(layout);
		text = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL | SWT.READ_ONLY);
		GridData spec = new GridData();
		spec.horizontalAlignment = GridData.FILL;
		spec.grabExcessHorizontalSpace = true;
		spec.verticalAlignment = GridData.FILL;
		spec.grabExcessVerticalSpace = true;
		text.setLayoutData(spec);
		line = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		spec = new GridData();
		spec.horizontalAlignment = GridData.FILL;
		spec.grabExcessHorizontalSpace = true;
		spec.verticalAlignment = GridData.FILL;
		spec.grabExcessVerticalSpace = false;
		spec.heightHint = 40;
		line.setLayoutData(spec);
		line.addKeyListener(this);
	}

	public void run() {
		createShell();
		shell.open();
		display = shell.getDisplay();
		while (!shell.isDisposed() && !quit)
			try {
				if (!display.readAndDispatch())
					display.sleep();
			}
			catch (Exception e) {
				error(e);
			}

		if (!shell.isDisposed())
			shell.dispose();
		chat.close();
	}

	public void keyPressed(KeyEvent event) {
	}

	public void keyReleased(KeyEvent event) {
		switch (event.keyCode) {
			case SWT.CR :
				try {
					String txt = line.getText();
					chat.execute(txt.trim());
					line.setText("");
				}
				catch (IOException e1) {
					error(e1);
				}
				break;
		}
	}

	private void error(Exception e) {
		if (!quit) {
			text.append("error> ");
			text.append(e + "");
		}
	}

	public void send(final String from, final String string) {
		display.asyncExec(new Runnable() {
			public void run() {
				text.append(from + "> " + string + "\r\n");
			}
		});
	}
}
