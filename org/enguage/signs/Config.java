package org.enguage.signs;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.written.Autoload;
import org.enguage.repertoires.written.Load;
import org.enguage.signs.interpretant.Commands;
import org.enguage.signs.interpretant.Redo;
import org.enguage.signs.objects.Variable;
import org.enguage.signs.symbol.reply.Answer;
import org.enguage.signs.symbol.reply.Reply;
import org.enguage.signs.symbol.reply.Response;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.attr.Attribute;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;
import org.enguage.util.tag.Tag;

import com.yagadi.Assets;

public class Config {
	private Config() {}
	
	private static final Audit audit = new Audit( "Config" );
	private static final String NAME = "config";
	
	private static boolean setValues( String name, String value ) {
		     if (name.equals("LISTFORMATSEP")) Reply.listSep(       value);
		else if (name.equals("ANDCONJUNCTIONS")) Reply.andConjunction( value );
		else if (name.equals("ORCONJUNCTIONS")) Reply.orConjunctions(  new Strings( value ));
		else if (name.equals("ANDLISTFORMAT" )) Reply.andListFormat( value);
		else if (name.equals( "ORLISTFORMAT" )) Reply.orListFormat(  value );
		else if (name.equals( "REPEATFORMAT" )) Reply.repeatFormat(  value );
		else if (name.equals( "REFERENCERS" )) Reply.referencers(   new Strings( value ));
		else if (name.equals( "CLASSPATH" )) Commands.classpath( value );
		else if (name.equals( "LOCATION"  )) Fs.location( value );
		else if (name.equals( "SUCCESS" )) Response.success( value );
		else if (name.equals( "FAILURE" )) Response.failure( value );
		else if (name.equals(  "ANSWER" )) Answer.placeholder( value );
		else if (name.equals(   "SHELL" )) Commands.shell( value );
		else if (name.equals(    "TERMS")) Shell.terminators( new Strings( value ));
		else if (name.equals(    "SOFA" )) Commands.java( value );
		else if (name.equals(     "TTL" )) Autoload.ttl( value );
		else if (name.equals(     "DNU" )) Response.dnu( value );
		else if (name.equals(     "DNK" )) Response.dnk( value );
		else if (name.equals(     "YES" )) Response.yes( value );
		else if (name.equals(      "NO" )) Response.no(  value );
		else
			return false;
		return true;
	}
	private static void setContext( ArrayList<Attribute> aa ) {
		ListIterator<Attribute> pi = aa.listIterator();
		while (pi.hasNext()) {
			Attribute  a = pi.next();
			String  name = a.name().toUpperCase( Locale.getDefault() );
			String value = a.value();
			if (!setValues( name, value))
				Variable.set( name,  value );
	}	}	
	private static void loadTag( Tag concepts ) {
		Repertoires.transformation( true );
		for (Tag t : concepts.content()) {
			if (t.name.equals( "concept" )) {
				String op = t.attribute( "op" );
				String id = t.attribute( "id" );

				if (!op.equals( "ignore" ))
					Load.load( id );
		}	}
		Repertoires.transformation( false );
	}
	public static int load( String fname ) {
		int rc = -1;
		String content = Fs.stringFromStream(
			Assets.getStream( Enguage.RO_SPACE+ File.separator + fname )
		);
		if (content.equals( "" )) {
			content = Fs.stringFromFile( "/app/etc/"+ fname );
			if (content.equals( "" ))
				audit.error( "config not found" );
		}
		
		long then = new GregorianCalendar().getTimeInMillis();
		Redo.undoEnabledIs( false );
		
		if (Enguage.isVerbose())
			Audit.log(
				Enguage.shell().copyright() +
				"\nEnguage main(): odb root is: " + Fs.root()
			);

		Tag t = new Tag( new Strings( content ).listIterator());

		if ((t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			loadTag( t.findByName( "concepts" ));
			rc = content.length();
		}

		Redo.undoEnabledIs( true );
		long now = new GregorianCalendar().getTimeInMillis();
		
		if (Enguage.isVerbose()) {
			Audit.log( "Initialisation in: " + (now - then) + "ms" );
			Audit.log( Signs.stats() );
		}
		
		return audit.out( rc );
}	}