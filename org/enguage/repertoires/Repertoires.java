package org.enguage.repertoires;

import java.util.TreeSet;

import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Signs;
import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.reply.Reply;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Repertoires {
	
	private Repertoires() {}
	
	public  static final String NAME = "repertoires";
	public  static final int      ID = 216434732;
	private static final Audit audit = new Audit( NAME );
		
	private static Signs signs = new Signs( "user" );
	public  static Signs signs() {return signs;}
	
	private static Signs engine = new Signs( Engine.NAME ).add( Engine.commands );
	private static Signs engine() {return engine;};
	
	// entry point for Enguage, re-entry point for Intentions
	public static Reply mediate( Utterance u ) {
		
		// not sure why blank mediations are being requested?
		if ( "".equals( u.toString() )) return new Reply();
		
		audit.in( "mediate", "\""+ u.toString() +"\"" );
			
		Autoload.autoload( u.representamen() ); // unloaded up in Enguage.interpret()
		
		/* At this point we need to rebuild utterance with the (auto)loaded concept,
		 * with any colloquialisms it may have loaded...
		 * Needs to be expanded in case we've expanded any parameters (e.g. whatever)
		 */
		u = new Utterance( u.expanded() );
		
		Reply r = signs.mediate( u );
		if (Response.N_DNU == r.response())
			r = engine().mediate( u );
	
		audit.out( r );
		return r;
	}
	
	public static Strings interpret( Strings cmds ) {
		audit.in( "interpret", "cmds="+ cmds );
		Strings rc = Response.failure();
		if (!cmds.isEmpty()) {
			String cmd = cmds.remove( 0 );
			
			if (cmd.equals("show")) {
				rc = Response.success();
				
				String name = cmds.remove( 0 );
				if (name.equals("signs") ||
					name.equals("user"))
					
					signs.show();
					
				else if (name.equals( Engine.NAME ))
					engine().show();
					
				else if (name.equals( "all" )) {
					engine.show();
					signs.show();
					
				} else
					rc = Response.failure();
				
			} else if (cmd.equals( "variable" )) {
				Variable.interpret( new Strings( "show" ));
				
			} else if (cmd.equals( "list" )) {
				//Strings reps = Enguage.e.signs.toIdList()
				/* This becomes less important as the interesting stuff becomes auto loaded 
				 * Don't want to list all repertoires once the repertoire base begins to grow?
				 * May want to ask "is there a repertoire for needs" ?
				 */
				rc = new Strings( "loaded repertoires include "+ new Strings( (TreeSet<String>)Concept.loaded()).toString( Reply.andListFormat() ));
			}
		}
		audit.out();
		return rc;
}	}
