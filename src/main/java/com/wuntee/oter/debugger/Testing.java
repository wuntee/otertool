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

public class Testing {

	/**
	 * @param args
	 * @throws IllegalConnectorArgumentsException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, IllegalConnectorArgumentsException {
		SocketAttachingConnector c = (SocketAttachingConnector)getConnector();
		Map<String, Connector.Argument> arguments = c.defaultArguments();
		
		Connector.Argument hostnameArgument = arguments.get("hostname");
		hostnameArgument.setValue("127.0.0.1");
		
		Connector.Argument portArgument = arguments.get("port");
		portArgument.setValue("8605");
		
		arguments.put("hostname", hostnameArgument);
		arguments.put("port", portArgument);

		VirtualMachine vm = c.attach(arguments);
		for(ReferenceType clas : vm.allClasses()){
			if(!clas.name().startsWith("android.") && !clas.name().startsWith("com.android.")){
				System.out.println(clas.name());
				for(Method m : clas.allMethods()){
					String mArgs = "(";
					try{
						for(LocalVariable arg : m.arguments()){
							mArgs = mArgs + arg.type() + " " + arg.name() + ",";
						}
					} catch(Exception e){
						mArgs = e.getMessage();
					}
					System.out.println(" -" + m.name() + mArgs + ")");
				}
			}
		}
		
	}
	
	public static Connector getConnector(){
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
