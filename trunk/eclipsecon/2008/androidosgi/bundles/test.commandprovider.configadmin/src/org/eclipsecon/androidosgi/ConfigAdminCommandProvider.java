package org.eclipsecon.androidosgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ConfigAdminCommandProvider implements CommandProvider {
	
	private static final Object ALIAS_KEY = "_alias_factory_pid";
	private volatile ConfigurationAdmin configAdmin;
	
	public ConfigurationAdmin getConfigAdmin() {
		return configAdmin;
	}

	public void setConfigAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}

	public String getHelp() {
		return "---Configuration Admin---\n"
				+ "\tinstallConfig <url> - install a configuration properties file\n"
				+ "\tlistConfigs - list configurations\n"
				+ "\tshowConfig <pid> - show contents of specified configuration\n"
				+ "\tdeleteConfig <pid> - delete specified configuration";
	}
	
	public void _installConfig(CommandInterpreter ci) throws IOException, InvalidSyntaxException {
		String argument = ci.nextArgument();
		if(argument == null) {
			ci.println("Usage: installConfig <url>");
		} else {
			URL url = new URL(argument);
			InputStream stream = null;
			try {
				stream = url.openStream();
				String pid = installProperties(url.getPath(), stream);
				ci.println("Installed PID: " + pid);
			} finally {
				if(stream != null) stream.close();
			}
		}
	}
	
	public void _listConfigs(CommandInterpreter ci) throws IOException, InvalidSyntaxException {
		Configuration[] configs = configAdmin.listConfigurations(null);
		if(configs == null || configs.length == 0) {
			ci.println("No configurations found");
		} else {
			for (int i = 0; i < configs.length; i++) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("PID: ").append(configs[i].getPid());
				if(configs[i].getFactoryPid() != null) {
					buffer.append(", Factory PID: ").append(configs[i].getFactoryPid());
				}
				ci.println(buffer.toString());
			}
		}
	}
	
	public void _showConfig(CommandInterpreter ci) throws IOException, InvalidSyntaxException {
		String pid = ci.nextArgument();
		if(pid == null) {
			ci.println("Usage: showConfig <pid>");
		} else {
			Configuration[] configs = configAdmin.listConfigurations("(service.pid=" + pid + ")");
			if(configs == null || configs.length == 0) {
				ci.println("No configurations matching PID " + pid);
			} else {
				Configuration config = configs[0];
				Dictionary props = config.getProperties();
				Enumeration keys = props.keys();
				StringBuffer buffer = new StringBuffer();
				
				while(keys.hasMoreElements()) {
					String key = (String) keys.nextElement();
					String value = (String) props.get(key);
					
					buffer.append('\t').append(key).append('=').append(value).append('\n');
				}
				
				ci.print(buffer.toString());
			}
		}
	}

	private String installProperties(String path, InputStream stream) throws IOException, InvalidSyntaxException {
		String fullFileName;
		int lastSlashIndex = path.lastIndexOf('/');
		if(lastSlashIndex < 0) {
			fullFileName = path;
		} else {
			fullFileName = path.substring(lastSlashIndex + 1);
		}
		
		String fileName;
		int lastDotIndex = fullFileName.lastIndexOf('.');
		if(lastDotIndex < 0) {
			fileName = fullFileName;
		} else {
			fileName = fullFileName.substring(0, lastDotIndex);
		}
		
		String pid;
		String factoryPid;
		int hyphenIndex = fileName.lastIndexOf('-');
		if(hyphenIndex < 0) {
			pid = fileName;
			factoryPid = null;
		} else {
			pid = fileName.substring(0, hyphenIndex);
			factoryPid = fileName.substring(hyphenIndex + 1);
		}
		
		Properties props = new Properties();
		props.load(stream);
		
		Configuration config;
		if(factoryPid == null) {
			config = configAdmin.getConfiguration(pid, null);
		} else {
			props.put(ALIAS_KEY, factoryPid);
			Configuration[] configs = configAdmin.listConfigurations("(" + ALIAS_KEY + "=" + factoryPid + ")");
			if(configs == null || configs.length == 0) {
				config = configAdmin.createFactoryConfiguration(pid, null);
			} else {
				config = configs[0];
			}
		}
		if(config.getBundleLocation() != null) {
			config.setBundleLocation(null);
		}
		config.update(props);
		return config.getPid();
	}

}
