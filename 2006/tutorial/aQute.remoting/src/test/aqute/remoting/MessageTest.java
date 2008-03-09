package test.aqute.remoting;

import java.io.*;

import junit.framework.*;
import aqute.remoting.*;

public class MessageTest extends TestCase {

	public void testMessage() throws Exception {
		Message message = new Message(Message.ANNOUNCE, null);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		message.writeTo(bout); 
		bout.close();
		byte []buf = bout.toByteArray();
		System.out.println(buf.length);
		
		ByteArrayInputStream	bin = new ByteArrayInputStream(buf);
		
		message = Message.getMessage(bin);
		System.out.println(message.getCmd());
	}
}
