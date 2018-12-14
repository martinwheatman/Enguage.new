package org.enguage.objects.space;

import org.enguage.objects.Every;
import org.enguage.objects.Numeric;
import org.enguage.objects.Sign;
import org.enguage.objects.Spatial;
import org.enguage.objects.Temporal;
import org.enguage.objects.Variable;
import org.enguage.objects.expr.Function;
import org.enguage.objects.list.Item;
import org.enguage.objects.list.List;
import org.enguage.objects.list.Transitive;
import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;
import org.enguage.vehicle.Colloquial;
import org.enguage.vehicle.Plural;

public class Sofa extends Shell {
	static private Audit audit = new Audit( "Sofa" );

	public Sofa(){
		super( "Sofa" );
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch! in sofa" );
	}
	private static final long lSuccess  = Strings.hash( Shell.SUCCESS );
	private static final long lFail     = Strings.hash( Shell.FAIL );

	public Strings doCall( Strings a ) {
		//audit.in( "doCall", a.toString( Strings.CSV ));
		if (a.size() > 1) {
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Sofa.interpret() typically deals with:
			 * 		["string", "get", "martin", "name"]
			 * 		["colloquial", "both", "'I have'", "'I've'"]
			 */			
			String  type = a.remove( 0 );
			long  typeId = Strings.hash( type );
			if (a.size() == 0 && typeId == lSuccess) return Shell.Success;
			if (a.size() == 0 && typeId ==  lFail) return Shell.Fail;
			return (typeId == Plural.id) ?  Plural.interpret( a )
				: typeId < Plural.id ?
					typeId ==  Sign.id ?  Sign.interpret( a )
						: typeId<Sign.id ?
							typeId == Link.id ? Link.interpret( a )
							: typeId < Link.id ?
								typeId == Item.id ? Item.interpret( a ) : Fail
								: typeId == List.id ? List.interpret( a ) : Fail
						// >Sign
						: (typeId == Value.id) ? Value.interpret( a )
							: (typeId < Value.id) ?
								typeId ==  Every.id ? Every.interpret( a ) : Fail
								: typeId == Entity.id ? Entity.interpret( a ) : Fail
					// > Plural
					: typeId == Temporal.id ? Temporal.interpret( a )
						: typeId < Temporal.id ?
							typeId == Spatial.id ? Spatial.interpret( a )
								: typeId < Spatial.id ?
									typeId == Numeric.id ? Numeric.interpret( a )
										: typeId == Overlay.id ? Overlay.interpret( a ) : Fail
									// > Spatial
									: typeId == Function.id ? Function.interpret( a ) : Fail
							// > Temporal
							: typeId == Colloquial.id ?	Colloquial.interpret( a )
								: typeId < Colloquial.id ?
									typeId == Variable.id ? Variable.interpret( a ) : Fail
										: typeId == Transitive.id ? Transitive.interpret( a ) : Fail;
			}
		audit.ERROR("doCall() fails - "+ (a==null?"no params":"not enough params: "+ a.toString()));
		return Fail;
	}
	
	// perhaps need to re-think this? Do we need this stage - other than for relative concept???
//	private Strings xdoSofa( Strings prog ) {
//		String cmd = prog.get( 0 );
//		char firstCh = cmd.charAt( 0 );
//		return (Strings.DOUBLE_QUOTE == firstCh ||
//				Strings.SINGLE_QUOTE == firstCh) ?
//					Strings.stripQuotes( cmd )
//					: doCall( prog );
//	}

	private Strings doNeg( Strings prog ) {
		//audit.traceIn( "doNeg", prog.toString( Strings.SPACED ));
		boolean negated = prog.get( 0 ).equals( "!" );
		Strings rc = doCall( prog.copyAfter( negated ? 0 : -1 ) ); // was do sofa
		if (negated) rc = rc.equals( Success ) ? Fail : rc.equals( Fail ) ? Success : rc;
		return rc; // */audit.traceOut( rc );
	}

/*private static String doAssign( Strings prog ) { // x = a b .. z
	TRACEIN1( "'%s'", arrayAsChars( prog, SPACED ));
	int assignment = 0 == .compareTo( prog[ 1 ], "=" );
	Strings e = copyStringsAfter( prog, assignment ? 1 : -1 );
	long rc = doNeg( e );
	if (assignment) {
		if (0 == .compareTo( "value", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning STRING %s = %s", prog.get( 0 ), rc ? (String )rc : "" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? (String )rc : "" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? (String )rc : "" );
		} else if (0 == .compareTo( "exists", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning BOOLEAN %s = %s", prog.get( 0 ), rc ? "true" : "false" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? "true" : "false" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? "true" : "false" );
		} else {
			printf( "type conversion error in '%s'\n", arrayAsChars( prog, SPACED ));
	}	}
	deleteStrings( &e, KEEP_ITEMS );
	TRACEOUTint( rc );
	return rc ;
}// */

	// a b .. z {| a b .. z}
	private Strings doOrList( Strings a ) {
		//audit.traceIn( "doOrList", a.toString( Strings.SPACED ));
		Strings rc = Fail;
		for (int i = 0, sz = a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "||" );
			i += cmd.size(); // left pointing at "|" or null
			if (rc.equals( Fail )) rc = doNeg( cmd ); // only do if not yet succeeded -- was doAssign()
		}
		//return audit.traceOut( rc );
		return rc;
	}

	private Strings doAndList( Strings a ) {
		//audit.traceIn( "doAndList", a.toString( Strings.SPACED ));
		Strings rc = Success;
		for (int i=0, sz=a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "&&" );
			//audit.debug( "cmd=" + cmd +", i="+ i );
			i += cmd == null ? 0 : cmd.size();
			if (rc.equals( Success )) rc = doOrList( cmd );
		}
		return rc; // */ audit.traceOut( rc );
	}

	private Strings doExpr( Strings a ) {
		//audit.traceIn( "doExpr", a.toString( Strings.SPACED ));
		Strings cmd = new Strings(); // -- build a command...
		while (0 < a.size() && !a.get( 0 ).equals( ")" )) {
			if (a.get( 0 ).equals( "(" )) {
				a.remove( 0 );
				cmd.addAll( doExpr( a ));
			} else {
				cmd.add( a.get( 0 ));
				a.remove( 0 ); // KEEP_ITEMS!
			}
			//audit.debug( "a="+ a.toString() +", cmd+"+ cmd.toString() );
		}
		Strings rc = doAndList( cmd );
		if ( 0 < a.size() ) a.remove( 0 ); // remove ")"
		return rc; // */audit.traceOut( rc );
	}
	public Strings interpret( Strings sa ) {
		Strings a = new Strings( sa );
		for (String s : sa) {
			if (   s.equals("&&") 
				|| s.equals("||")
				|| s.equals("(")
				|| s.equals("!")
			   ) {
				return doCall( a ); //doSofa( a );
		}	}
		return new Strings( doExpr( a )); // still need to check if it is a constant
	}
	
	public static void main( String[] argv ) { // sanity check...
		Sofa cmd = new Sofa();
		if (argv.length > 0) {
			cmd.interpret( new Strings( argv ));
		} else {
			Audit.allOn();
			Audit.traceAll( true );
			audit.log( "Sofa: Ovl is: "+ Overlay.Get().toString());
			cmd.run();
}	}	}
