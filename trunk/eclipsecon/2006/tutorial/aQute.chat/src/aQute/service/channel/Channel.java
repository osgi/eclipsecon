package aQute.service.channel;

import java.io.*;

public interface Channel {
	String	CH_NAME	= "channel.name";

	void send(String from, String msg) throws IOException ;
}
