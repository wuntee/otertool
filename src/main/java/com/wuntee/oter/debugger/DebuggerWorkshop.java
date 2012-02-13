package com.wuntee.oter.debugger;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.tools.jdi.SocketAttachingConnector;

public class DebuggerWorkshop {
	
	public VirtualMachine connectToRemoteVirtualMachine(String host, int port) throws IOException, IllegalConnectorArgumentsException{
		SocketAttachingConnector c = (SocketAttachingConnector)getRemoteConnector();
		Map<String, Connector.Argument> arguments = c.defaultArguments();
		
		Connector.Argument hostnameArgument = arguments.get("hostname");
		hostnameArgument.setValue(host);
		
		Connector.Argument portArgument = arguments.get("port");
		portArgument.setValue(String.valueOf(port));
		
		arguments.put("hostname", hostnameArgument);
		arguments.put("port", portArgument);

		VirtualMachine vm = c.attach(arguments);
		
		return(vm);
	}
	
	public static Connector getRemoteConnector(){
		List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
		for(Connector c : connectors){
			if(c.name().equals("com.sun.jdi.SocketAttach")){
				System.out.println(c.getClass());
				return(c);
			}
		}
		return(null);
	}
}
