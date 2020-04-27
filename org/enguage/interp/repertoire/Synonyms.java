package org.enguage.interp.repertoire;

import org.enguage.objects.Variable;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.vehicle.Plural;
import org.enguage.vehicle.reply.Reply;

public class Synonyms {
	static public final String NAME = "synonyms";
	//static private      Audit audit = new Audit( NAME );
	static public final int      id = 237427137; //Strings.hash( "synonyms" );

	static private Attributes                synonyms = new Attributes();
	
	private static boolean load( String name, String load, String from, String to ) {
		if (null != Autoload.get( name )) {
			return true;
		} else {
			//Audit.log("name="+ name +", load="+ load +", from="+ from +", to="+to );
			String conceptName = com.yagadi.Assets.loadConcept( load, from, to );
			//Audit.log( "Synonym: "+ conceptName );
			if (!conceptName.equals( "" )) {
				//Audit.log( "Synonyms: loaded "+ conceptName );
				Autoload.put( conceptName );
				return true;
		}	}
		return false;
	}
	public static void autoload( Strings utterance ) {
		// utterance="i want a coffee" => load( "want+wants.txt", "need+needs.txt", "need", "want" )
		for (String synonym : synonyms.matchNames( utterance )) {
			String existing = synonyms.get( synonym );
			if (!load( synonym, existing, existing, synonym )                      &&
				!load( synonym+"+"+Plural.plural( synonym ),
				       existing+"+"+Plural.plural( existing ),  existing, synonym ) &&
				!load( Plural.plural(  synonym )+"+"+synonym,
				       Plural.plural( existing )+"+"+existing, existing, synonym ));
	}	}
	private static boolean unload( String name ) {
		//audit.in( "unload", "name="+name );
		if (Autoload.containsKey( name )) {
			//Audit.LOG( "removing:"+ name );
			Repertoire.signs.remove( name );
			Autoload.remove( name );
			return true; //audit.out( true );
		}
		return false; //audit.out( false );
	}
	private static void destroy( String name ) {
		synonyms.remove( name );
		if (unload( name ) ||
		    unload( name+"+"+Plural.plural( name )) ||
		    unload( Plural.plural( name )+"+"+name )) ;
	}
	static public Strings interpret( Strings cmds ) {
		// e.g. ["create", "want", "need"]
		Strings rc = Reply.failure();
		int     sz = cmds.size();
		if (sz > 0) {
			String cmd = cmds.remove( 0 );
			rc = Reply.success();
			
			if (cmd.equals( "create" ) && sz>3)
				// e.g. "create want / need"
				synonyms.add( Attribute.value( cmds.getUntil( "/" )).toString( Strings.UNDERSC ), //from
				              Attribute.value( cmds                ).toString( Strings.UNDERSC ));//to
				
			else if (cmd.equals( "destroy" ) && sz==2) {
				synonyms.remove( cmds.toString( Strings.UNDERSC ));
				destroy( cmds.toString( Strings.UNDERSC ));
				rc = Reply.success(); // success if destroyed or not!
				
			} else if (cmd.equals( "save" ))
				Variable.set( NAME, synonyms.toString());
			
			else if (cmd.equals( "recall" ))
				synonyms = new Attributes( Variable.get( NAME ));
			
			else rc = Reply.failure();				
		}
		return rc;
	}
	static public void test( String cmd ) {
		interpret( new Strings( cmd ));
	}
	public static void main( String args[]) {
		Audit.allOn();
		test( "create want / need" );
		test( "save" );
//		autoload( w/"I want a coffee" ); // -->loads need+needs, with need="want"
//		//...
//		unload();
		test( "destroy want" );
}	}
