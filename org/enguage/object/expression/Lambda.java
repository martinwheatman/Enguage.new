package org.enguage.object.expression;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Value;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.vehicle.Numerals;

public class Lambda {
	static private Audit audit = new Audit( "Lambda" );

	// TODO: need attach()/detach() methods!
	public Lambda( Function f, Strings params, String body ) { // new/create
		signature = params;
		audit.debug( "creating: "+ signature.toString( Strings.CSV ) +"/"+ f.name());
		new Value(
				signature.toString( Strings.CSV ),
				f.name()
			).set( body );
	}
	public Lambda( String name, Strings values ) { // existing/find
		audit.in( "ctor", name +"( "+ values +" )" );
		Strings onames = Enguage.e.o.list( "." );
		if (null != onames) for (String params : onames) 
			if (match( (signature = new Strings( params, ',' )), values )
				&& !(body = new Value( params, name ).getAsString()).equals(""))
				break; // bingo! (can we revisit if this ain't right?)
		if (body.equals(""))
			audit.log( "no "+ values.toString( Strings.CSV ) +"/"+ name +" found" );
		audit.out();
	}
	
	private Strings signature = null;
	public  Strings signature() { return signature; }
	
	private String body = "";
	public  String body() { return body; }
	public  Lambda body( String b ) { body = b; return this; };
	
	public String toString() { return "( "+ signature +" ): {"+ body +"}";}
	
	private static boolean match( Strings names, Strings values ) {
		boolean rc = false;
		audit.in( "match", "names="+ names +", values="+ values.toString( Strings.DQCSV ) );
		if (names.size() == values.size()) {
			rc = true;
			ListIterator<String> ni = names.listIterator(),
			                     vi = values.listIterator();
			while (rc && ni.hasNext()) {
				String n = ni.next(),
				       v = vi.next();
				// if name is numeric we must match this value
				audit.debug( "matching "+ n +", "+ v );
				rc = Numerals.isNumeric( n ) ? // name=1 => value=1 !
						n.equals( v ) :
						     Numerals.isNumeric( v ) ? // value = xxx, deref
								null == Variable.get( v ) : true;
			}
		} else
			audit.debug( "Lambda: name/val mis-match in params: "+ names +"/"+ values.toString( Strings.DQCSV ));
		return audit.out( rc );
	}
	private static void matchTest( String names, String values, boolean pass ) {
		audit.in( "matchTest", names +"/"+ values );
		if (pass != match( new Strings( names ), new Strings( values )))
			audit.FATAL( "matched on "+ names +"/"+ values );
		else
			audit.debug( "matching "+ names +" with "+ values +" passed" );
		audit.out();
	}
	static public void main( String args[] ) {
		Enguage.e = new Enguage();
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Variable.set( "x", "1" );
			Variable.set( "y", "2" );
			matchTest( "1",   "1",   true );
			matchTest( "x",   "1",   true );
			matchTest( "x y", "1 2", true );
			matchTest(   "x", "1 2", false ); // n vals != n names
			matchTest( "x 1", "1 2", false ); // 1 != 2
			audit.log( "match tests PASSED" );
			
			//Audit.allOn();
			audit.log( "Creating a blank function, called 'sum'..." );
			Function f = new Function( "sum" );
			audit.log( "Creating a new lambda..." );
			new Lambda( f, new Strings( "a b" ), "a plus b" );
			audit.log( "Finding it:" );
			Audit.incr();
			Lambda l = new Lambda( "sum", new Strings( "2 3" ));
			Audit.decr();
			audit.log( "PASSED: "+ l.toString() );
}	}	}