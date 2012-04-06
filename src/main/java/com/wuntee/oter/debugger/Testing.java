package com.wuntee.oter.debugger;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jf.dexlib.Code.ReferenceType;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.EventSet;
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
		portArgument.setValue("8601");
		
		arguments.put("hostname", hostnameArgument);
		arguments.put("port", portArgument);

		VirtualMachine vm = c.attach(arguments);
		EventRequestManager mgr = vm.eventRequestManager();
	
		//System.out.println(vm.classesByName("com.wuntee.securesslcomm").size());
		//Location location = vm.classesByName("com.wuntee.securesslcomm.SecureSSLCommunicationActivity").get(0).methodsByName("startTheActivity").get(0).location();
		
		for(com.sun.jdi.ReferenceType rt: vm.allClasses()){
			for(Method m: rt.allMethods()){
				if(m.name().contains("cache")){
					addBreakpointToMethod(m, mgr);

				}
			}
		}
		

/*		for(Method m : vm.classesByName("android.content.Intent").get(0).methodsByName("<init>")){
			System.out.println("Breakpoint: " + m.toString());
			
			Location location = m.location(); 
			BreakpointRequest bpr = mgr.createBreakpointRequest(location);
			bpr.enable();
		}*/
		
		//addIntentBreakpoints(vm);
		
		com.sun.jdi.event.EventQueue q = vm.eventQueue();
		
		while(true){
			EventSet es = q.remove();
			Iterator<com.sun.jdi.event.Event> it = es.iterator();
			while(it.hasNext()){
				com.sun.jdi.event.Event e = it.next();
				//if(e.)
				BreakpointEvent bpe = (BreakpointEvent)e;
				//printBreakpointData(bpe);
				
				try{					
					System.out.println("Method: " + bpe.location().method().toString());
					for(StackFrame sf : bpe.thread().frames()){
						System.out.println("Stackframe Method: " + sf.location().method().toString());
						System.out.println("Arguments: ");
						for(Value lv : sf.getArgumentValues()){
								System.out.println("\t--");
								System.out.println("\t" + lv.toString());
						}
					}
				} catch (Exception ex){
					System.out.println("Error: ");
					ex.printStackTrace();
				}
				System.out.println();
				vm.resume();
			}
		}
		
	}
	
	public static void addBreakpointToMethod(Method m, EventRequestManager mgr){
		System.out.println("Breakpoint: " + m.toString());
		try{
			Location location = m.location(); 
			BreakpointRequest bpr = mgr.createBreakpointRequest(location);
			bpr.enable();
		} catch (com.sun.jdi.NativeMethodException e){
			System.out.println("Error: Cant add breakpoint to native method (" + m.toString() + ")");
		}
	}
	
	public static void printBreakpointData(BreakpointEvent e) throws IncompatibleThreadStateException, AbsentInformationException{
		
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
			System.out.println("\t   Visible Variables:");
			for(LocalVariable lv : sf.visibleVariables()){
				System.out.println("\t\t--");
				System.out.println("\t\tName: " + lv.name());
				System.out.println("\t\tType: " + lv.typeName());
				Value lvValue = sf.getValue(lv);
				if(lvValue != null){
					System.out.println("\t\tValue: " + lvValue);
					System.out.println("\t\tValue Type:" + lvValue.type());
					System.out.println("\t\ttoString: " + lv);
				}
			}
			System.out.println("\t   Argument Values:");

			for(Value lv : sf.getArgumentValues()){
				System.out.println("\t\t--");
				System.out.println("\t\t" + lv.toString());
			}
		}
	}
	
	@SuppressWarnings("restriction")
	public static void addIntentBreakpoints(VirtualMachine vm){
		
		EventRequestManager mgr = vm.eventRequestManager();
		com.sun.jdi.ReferenceType intentClass = vm.classesByName("android.content.Intent").get(0);
		for(Method m : intentClass.methodsByName("<init>")){
			System.out.println("Breakpoint: " + m.toString());
			
			Location location = m.location(); 
			BreakpointRequest bpr = mgr.createBreakpointRequest(location);
			bpr.enable();
		}
		for(Method m : intentClass.methodsByName("putExtra")){
			System.out.println("Breakpoint: " + m.toString());
			
			Location location = m.location(); 
			BreakpointRequest bpr = mgr.createBreakpointRequest(location);
			bpr.enable();
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
