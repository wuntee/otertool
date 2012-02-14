package com.wuntee.oter.debugger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.tools.jdi.SocketAttachingConnector;

public class Testing {

	/**
	 * @param args
	 * @throws IllegalConnectorArgumentsException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws IncompatibleThreadStateException 
	 * @throws AbsentInformationException 
	 */
	@SuppressWarnings("restriction")
	public static void main(String[] args) throws IOException, IllegalConnectorArgumentsException, InterruptedException, IncompatibleThreadStateException, AbsentInformationException {
		SocketAttachingConnector c = (SocketAttachingConnector)getConnector();
		Map<String, Connector.Argument> arguments = c.defaultArguments();
		
		Connector.Argument hostnameArgument = arguments.get("hostname");
		hostnameArgument.setValue("127.0.0.1");
		
		Connector.Argument portArgument = arguments.get("port");
		portArgument.setValue("8615");
		
		arguments.put("hostname", hostnameArgument);
		arguments.put("port", portArgument);

		VirtualMachine vm = c.attach(arguments);
		EventRequestManager mgr = vm.eventRequestManager();
		
		Location location = vm.classesByName("android.app.Activity").get(0).methodsByName("onCreate").get(0).location();
		
		BreakpointRequest bpr = mgr.createBreakpointRequest(location);
		bpr.enable();
		
		while(true){
			com.sun.jdi.event.EventQueue q = vm.eventQueue();
			EventSet es = q.remove();
			Iterator<com.sun.jdi.event.Event> it = es.iterator();
			while(it.hasNext()){
				BreakpointEvent e = (BreakpointEvent)it.next();
				
				System.out.println(e.location());
				System.out.println("Line number: " + e.location().lineNumber());
				System.out.println("Code index: " + e.location().codeIndex());
				try {
					System.out.println("SourceName: " + e.location().sourceName());
				} catch (AbsentInformationException e1) {
					System.out.println("SourceName: UNAVAILABLE");
				}
				System.out.println("Declaration type: " + e.location().declaringType());
				System.out.println("Method: " + e.location().method());	
				
				System.out.println("Stack frames:");
				for(StackFrame sf : e.thread().frames()){
					System.out.println("\t" + sf.toString());
					for(LocalVariable lv : sf.visibleVariables()){
						System.out.println("\t\t" + lv);
					}
				}
			}
		}
		
	}
	
	public static Connector getConnector(){
		List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
		for(Connector c : connectors){
			if(c.name().equals("com.sun.jdi.SocketAttach")){
				return(c);
			}
		}
		return(null);
	}

}
