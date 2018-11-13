package org.enguage.util.sys;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.enguage.Enguage;
import org.enguage.interp.Context;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.Utterance;
import org.enguage.vehicle.reply.Reply;

public class Net {
	
	static final public int TestPort = 0;

	static private Audit audit = new Audit( "net" );
	
	static private boolean serverOn = false;
	static public  boolean serverOn() { return serverOn; }
	
	static public void server( String port ) { server( port, "" );}
	static public void server( String port, String prefix ) {
		ServerSocket server = null;
		serverOn = true;
		try {
			server = new ServerSocket( Integer.valueOf( port ));
			audit.LOG( "Server listening on port: "+ port );
			while (true) {
				
				Socket    connection = server.accept();
				BufferedReader   in  = null;
				DataOutputStream out = null;
				
				try {
					in  = new BufferedReader( new InputStreamReader( connection.getInputStream()));
					out = new DataOutputStream( connection.getOutputStream());
					out.writeBytes( prefix + Enguage.get().interpret( new Strings( in.readLine() )) + "\n" );
				} catch (Exception e) {
					audit.ERROR( "Error in child socket");
				} finally {
					try {
						if (null != in) in.close();
					} catch (IOException e) {
						audit.ERROR( "Net.server():IO error in closing TCP child in socket" );
					}
					try {
						if (null != out) out.close();
					} catch (IOException e) {
						audit.ERROR( "Net.server():IO error in closing TCP child out socket" );
					}
					try {
						if (null != connection) connection.close();
					} catch (IOException e) {
						audit.ERROR( "Net.server():IO error in closing TCP child connection socket" );
				}	}
			}
		} catch (IOException e) {
			audit.ERROR( "Engauge.main():IO error in TCP socket operation" );
		} finally {
			try {
				if (null != server) server.close();
			} catch (IOException e) {
				audit.ERROR( "Net.server():IO error in closing TCP server socket" );
		}	}
		serverOn = false;
	}
	static public String client( String addr, int port, String data ) {
		audit.in( "tcpip", "addr="+ addr +", port="+ port +", value='"+ data +"', ["+ Context.valueOf() +"]" );
		
		// don't know why, but data needs to be stripped of single quotes???
		data=Strings.trim( data, '\'' );
		data=Utterance.externalise( new Strings( data ), false );
		audit.debug( "data is: "+ data);
		
		String rc = Reply.failure();
		
		if (port == TestPort) { // test value
			rc = Reply.success(); // assume we've stuffed the server intentionally
			
		} else if (port > 1024 && port < 65536) {
			addr = addr==null || addr.equals( "" ) ? "localhost" : addr;
			
			Socket connection = null;
			DataOutputStream out = null;
			BufferedReader in = null;
			try {
				audit.debug( "creating socket" );
				connection = new Socket( addr, port );
				
				out = new DataOutputStream( connection.getOutputStream());
				audit.debug( "  writing: "+ data );
				out.writeBytes( data );
				out.flush();
				
				audit.debug( "reading" );
				in = new BufferedReader( new InputStreamReader( connection.getInputStream()));
				audit.debug( "  reading..." );
				rc = in.readLine();
				
			} catch (IOException e) {
				audit.ERROR( "error: "+ e.toString());
			} finally {
				try {
					if (null != in) in.close();
				} catch (IOException e){
					audit.ERROR("closing connection:"+ e.toString());
				}
				try {
					if (null != out) out.close();
				} catch (IOException e){
					audit.ERROR("closing connection:"+ e.toString());
				}
				try {
					if (null != connection) connection.close();
				} catch (IOException e){
					audit.ERROR("closing connection:"+ e.toString());
		}	}	}
		return audit.out( rc );
	}
	public static void main( String args[]) {
		Audit.allOn();
		if (args.length == 1)
			server( args[ 0 ]);
		else
			audit.log( "output: "+ client( "localhost", 8080, "'put your hands on your head'" ));
}	}
