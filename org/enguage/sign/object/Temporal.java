package org.enguage.sign.object;

import org.enguage.sign.Assets;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.sign.symbol.when.Day;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Temporal {
	
	static public final String NAME = "temporal";
	static public final int      id = 240152112; //Strings.hash( NAME );
	static private      Audit audit = new Audit( NAME );
	
	private static Strings concepts = new Strings();
	public  static boolean isConcept( String s) { return concepts.contains( s ); }
	public  static void   addConcept( String s ) {if (!concepts.contains( s )) concepts.add( s );}
	public  static void   addConcepts( Strings ss) {for (String s : ss) addConcept( s );}

	public static String list() { return concepts.toString( Strings.CSV );}
	
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Response.IGNORE;
		if (args.size() > 0) {
			String cmd = args.remove( 0 );
			rc = Response.SUCCESS;
			if (args.size() == 0) {
				if (cmd.equals( "addCurrent" ))
					addConcept( Variable.get( Assets.NAME ));
				else
					rc = Response.FAIL;
			} else {
				if (cmd.equals( "dayOfWeek" )) {
					When w = Day.getWhen( args );
					rc = (w == null ? Response.FAIL : Day.name( w.from().moment()));
				} else if (cmd.equals( "set" )) {
					String arg = args.remove( 0 );
					if ( arg.equals( "future" ))
						When.futureIs();
					else if ( arg.equals( "past" ))
						When.pastIs();
					else if ( arg.equals( "present" ))
						When.presentIs();
					else
						rc = Response.FAIL;
				} else if (cmd.equals( "add" ))
					addConcepts( args );
				else
					rc = Response.FAIL;
			}
		}
		return audit.out( new Strings( rc ));
	}
	public static void main( String args[] ) {
		audit.debug( interpret( new Strings( "dayOfWeek 1225" )));
}	}