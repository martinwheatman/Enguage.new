package org.enguage.sign.object;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Value;
import org.enguage.sign.symbol.pattern.Frags;
import org.enguage.sign.symbol.reply.Response;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Variable {
	/* As part of the Sofa library, variable manages persistent values:
	 * /a/b/c/_NAME -> value, which is the persistent equivalent of NAME="value".
	 * Because $ has special significance in the filesystem/shell
	 * prefix variables with '_'
	 */
	static public  final String NAME = "variable";
	static public  final int      id = 262169728; //Strings.hash( NAME );
	static private       Audit audit = new Audit( "Variable" );
	
	private static TreeMap<String,String> cache = encache();
	private static TreeMap<String,String> encache() { return encache( Overlay.get() );}
	private static TreeMap<String,String> encache( Overlay o ) {
		//audit.in( "encache", Ospace.location());
		cache = new TreeMap<String,String>();
		for( String name : o.list( NAME ))
			if (name.equals( name.toUpperCase( Locale.getDefault()) )) // if valid variable name
				cache.put( name, new Value( NAME, name ).getAsString());
		//audit.out();
		return cache;
	}
	static private void printCache() {
		Audit.title( "Printing cache" );
		Set<Map.Entry<String,String>> entries = cache.entrySet();
		Iterator<Map.Entry<String,String>> ei = entries.iterator();
		Audit.incr();
		while (ei.hasNext()) {
			Map.Entry<String,String> e = ei.next();
			audit.debug( e.getKey()  +"='"+  e.getValue()  +"'" );
		}
		Audit.decr();
	}

	String name;
	Value  value;
	
	public Variable( String nm ) {
		name = nm.toUpperCase( Locale.getDefault());
		value = new Value( NAME, name );
		cache.put( name, value.getAsString());
	}
	public Variable( String nm, String val ) {
		name = nm.toUpperCase( Locale.getDefault());
		value = new Value( NAME, name );
		set( val );
	}
	public String     get() { return cache.get( name ); }
	public boolean  isSet( String value ) { return cache.get( name ).equals( value );}
	public Variable   set( String val ) {
		cache.put( name, val );
		value.set( val );
		return this;
	}
	public Variable unset() {
		cache.remove( name );
		value.ignore();
		return this;
	}
	
	// TODO: need to have lowercase variables too - so concept scripts can set them
	// 		 Can't be UPPERCASE, unless TAGs passes through u/c unmatches vars.
	// 		 May match - then difficult bugs to find!
	// for backward compatibility, keeping these statics 
	static public String set( String name, String val ) { new Variable( name ).set( val ); return val;}
	static public void unset( String name ) { new Variable( name ).unset(); }
	static public String get( String name ) { return cache.get( name.toUpperCase( Locale.getDefault()) ); } // raw name
	static public String get( String name, String def ) {
		boolean reflect = name.startsWith( Frags.externPrefix );
		if (reflect)
			name = name.substring( Frags.externPrefix.length() );
		
		boolean first = name.startsWith( Frags.firstPrefix );
		if (first)
			name = name.substring( Frags.firstPrefix.length() );
		
		boolean rest = name.startsWith( Frags.restPrefix );
		if (rest)
			name = name.substring( Frags.restPrefix.length() );
		
		String value = cache.get( name );
		if (reflect)
			value = Attributes.reflect( new Strings( value )).toString();
		
		if (first)
			value = new Strings( value ).before( "and" ).toString();
		
		if (rest)
			value = new Strings( value ).after( "and" ).toString();
		
		return value==null || value.equals("") ? def : value;
	}
	static public boolean isSet( String name, String value ) {
		String val = new Variable( name ).get();
		return  val != null &&
				((value == null && !val.equals( "" )  ) ||
				 (value != null && val.equals( value ))   );
	}
	public static String derefUc( String in ) {
		String out = "", word = "";
		char[] buffer = in.toCharArray();
		for (char ch : buffer) {
			if (Character.isUpperCase( ch ))
				word += ch;
			else {
				if (!word.equals( "" )) {
					word = Variable.get( word );
					if (word == null) word = "";
					out += word;
					word = "";
				}
				out += ch;
		}	}
		return out;
	}
	
	static private Strings exceptions = new Strings();
	static public  void exceptionAdd( Strings excepts ) {
		for (String s : excepts ) if (!exceptions.contains( s )) exceptions.add( s );
	}
	static private void exceptionRemove( Strings excepts ) {
		for (String s : excepts ) exceptions.remove( s );
	}
	
	static private Strings deref( String name ) {
		// must return strings for case where variable value is 'hello world'
		// must contract( "=" ) for case where 'name' is "SUBJECT='fred'"
		return (!exceptions.contains( name ) && Strings.isUCwHyphUs( name )
				 ?	new Strings( get( name, name )).replace( ",", "and" )
				 :	new Strings( name )).contract( "=" );
	}
	static public Strings derefOrPop( ListIterator<String> ai ) { return derefOrPop( ai, false ); }
	static public Strings derefOrPop( ListIterator<String> ai, boolean internal ) {
		// "QUANTITY='2' UNITS='cup' of" => "2 cups of"
		// "LOCATION='here' LOCATOR=''"  => ""
		Strings b = new Strings();
		while (ai.hasNext()) {
			String next = ai.next();
			if (next.equals( "[" ))
				b.appendAll( derefOrPop( ai, true ));
			else if (next.equals( "]" ))
				break;
			else {
				Strings c = deref( next ); // deref ...
				if (internal) for (String d : c)
					if (!exceptions.contains( d )
							&& Strings.isUCwHyphUs( d )) { // ... POP!
						//audit.debug( "popping on "+ d );
						while (ai.hasNext() && !ai.next().equals( "]" ));
						return new Strings();  // or null?
					}
				b.appendAll( c );
		}	}
		return b;
	}
	static public Strings deref( Strings in ) {
		Strings out = new Strings();
		Iterator<String> it = in.iterator();
		while (it.hasNext())
			out.appendAll( deref( it.next() )); // preserve name='value'
		return out;
	}
	
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String  rc = Response.SUCCESS,
		       cmd = args.remove( 0 );
		int sz = args.size();
		if (sz > 0) {
			String name = args.remove( 0 );
			if (sz > 1)
				if (cmd.equals( "set" ))
					rc = set( name, args.toString() );

				else if (cmd.equals( "equals" ))
					rc = isSet( name, args.toString()) ? Response.SUCCESS : Response.FAIL;
			
				else if (cmd.equals( "exception" )) {
					
					String direction = args.remove( 0 );
					if (direction.equals( "add" ))
						exceptionAdd( args );
					else if (direction.equals( "remove" ))
						exceptionRemove( args );
					else
						rc = Response.FAIL;
				} else
					rc = Response.FAIL;
				
			else { // sz == 1, name and no params
				if (cmd.equals( "exists" ))
					rc = isSet( name, null ) ? Response.SUCCESS : Response.FAIL;
				else if (cmd.equals( "unset" ))
					unset( name );
				else if (cmd.equals( "get" ))
					rc = get( name.toUpperCase( Locale.getDefault() ));
				else
					rc = Response.FAIL;
			}
		} else if (cmd.equals( "show" )) {
			audit.debug( "printing cache" );
			printCache();
			audit.debug( "printed" );
		} else
			rc = Response.FAIL;
		audit.out( rc = rc==null?"":rc );
		return new Strings( rc );
	}
	
	// --
	public static void test( String cmd, String expected ) {
		Strings actual = interpret( new Strings( cmd ));
		if (actual.equals( new Strings( expected )))
			if ( actual.equals( Response.Ignore ))
				audit.debug(   "PASS: "+ cmd );
			else
				audit.debug(   "PASS: "+ cmd +" = '"+ actual +"'" );
		else
			audit.FATAL( "FAIL: "+ cmd +" = '"+ actual +"' (expected: "+ expected +")" );
	}
	public static void main( String args[] ) {
		Overlay.attach( NAME );
		Audit.on();
		
		printCache();
		Variable spk = new Variable( "NAME" );
		String tmp = spk.get();
		audit.debug( "was="+ (tmp==null?"<null>":tmp));
		if ( tmp.equals( "fred" ))
			interpret( new Strings( "set NAME billy boy" ));
		else
			spk.set( "fred" );
		tmp = spk.get();
		audit.debug( "now="+ (tmp==null?"<null>":tmp));
		if ( spk.isSet( "fred" ))
			audit.debug( "spk set to Fred" );
		else
			audit.debug( "spk is set to Bill" );
		printCache();
		
		//*		Static test, backwards compat...
		test( "set hello there", "there" );
		test( "get HELLO", "there" );
		test( "equals HELLO there", Response.SUCCESS );
		audit.debug( "deref: HELLO hello there="+ deref( new Strings( "HELLO hello there" )));
		test( "unset HELLO", Response.SUCCESS );
		test( "get HELLO", "" );
		{	// derefOrPop test...
			Variable.unset( "LOCATOR" );
			Variable.set( "LOCATION", "Sainsburys" );
			Strings b = new Strings( "where [LOCATOR LOCATION] to LOCATION" );
			
			b = derefOrPop( b.listIterator() );

			audit.debug( "b is now '"+ b +"' (should be 'where to Sainsburys')");
		}
		audit.debug( "PASSED" );
}	}