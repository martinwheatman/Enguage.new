package org.enguage.signs.symbol.number;

import org.enguage.signs.objects.expr.Function;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Representamen extends Strings {
	/* {}=repeating 0..n, []=optional
	 * 
	 * numeral == digit{digit}[.digit{digit}]
	 *  postOp == ["all"] "squared" | "cubed"
	 *      op == "plus" | "minus" | "times" | "divided by" |
	 *            "multiplied by" | "times by" | "+" | "-" | "*" | "/"
	 *    expr == numeral {[postOp] [op expr]}
	 *      
	 * E.g. expr = 1 plus 2 squared plus 3 squared plus 4 all squared. 
	 */
	static final long serialVersionUID = 0L;
	static private Audit audit = new Audit( "Number" );
	
	private int    idx = 0;
	
	public void resetIdx() {idx=0;}
	
	private String op  = "";
	private String nextOp = "";
	
	private boolean isInteger = true;
	public  boolean isInteger() { return isInteger; }
	public  void    isInteger( boolean b ) { isInteger = b; }
	
	private static String funcEval( String fn, Strings params ) {
		// fn="product", params=["a","b"]
		Strings cmd = new Strings();
		cmd.append( "evaluate" )
		   .append( fn )
		   .appendAll( params );
		String token = Function.interpret( cmd ).toString();
		return Strings.isNumeric( token ) ? token : null;
	}

	public String getOp() {
		// e.g. x divided by ...
		//      x to the power of ...
		if (!nextOp.equals( "" )) {
			op = nextOp;
			nextOp = "";
		} else if (idx >= size() ){
			audit.ERROR( "getOp(): Reading of end of val buffer");
			return "";
		} else {
			op = get( idx++ );
			if (idx < size() && op.equals( "divided" ))
				op +=(" "+get( idx++ )); // "by" ..."into"?
			
			else if (idx < size() && op.equals( "to" )) {
				op +=(" "+get( idx++ )); // the
				if (idx < size() && op.equals( "to the" )) {
					op +=(" "+get( idx++ )); // power
					if (idx < size() && op.equals( "to the power" ))
						op +=(" "+get( idx++ )); // of
		}	}	}
		return op;
	}
	//retrieves a number from the array and adjusts idx appropriately
	// e.g. "three", "point",  "one", "four", "two"
	//      "the",   "square", "of",  <params e.g. 'a', 'b' and 'c'>
	private Float getNumber() {
		//audit.in( "getNumba", "idx="+ idx +", array=["+toString( Strings.CSV )+"]");
		/*
		 * this retrieves a SPOKEN number as generated by Android e.g. [ "3", "point", "1", "4", "2" ]
		 */
		String sign="+",
		       number = "";
		if (size() > 0) {
			String got = get( idx );
			if (got.equals( "the" )) {
				if (size() > ++idx) {
					String fnName = get( idx );
					if (size() > ++idx) {
						got = get( idx++ );
						if (got.equals( "of" ) && idx < size()) {
							//
							//get params
							//
							got = get( idx++ );
							String initParam = got;
							Strings params = new Strings();
							while (idx < size() && !got.equals( "and" )) {
								params.add( got );
								got = get( idx++ );
							}
							if (got.equals( "and" )) {
								if (size() > idx) {
									got = get( idx++ );
									params.append( "and" ).append( got );
								} else
									params = new Strings( initParam );
							} else
								params.add( got );
							
							number = funcEval( fnName, params );
				}	}	}
			} else {
				if (got.equals( "plus" )) {
					sign = "+";
					idx++;
					
				} else if (got.equals( "minus" )) {
					sign = "-";
					idx++;
					
				} else if (got.equals( "+" ) || got.equals( "-" )) {
					sign = got;
					idx++;
				}
				number = get( idx++ );
				if (number.contains(".")) isInteger = false;
				if (idx < size()) {
					if ( get( idx ).equals( "point" )) {
						number += ".";
						idx++;
						isInteger = false;
					}
					while ( idx < size()) {
						String tmp = get( idx );
						if (tmp.length() != 1)
							break;
						else {
							int digit = tmp.charAt(0);
							if (digit >= '0' && digit <= '9')
								number += tmp;
							else
								break;
						}
						idx++;
		}	}	}	}
		Float rc = Float.NaN;
		try { rc =  Float.parseFloat( sign+number ); } catch (Exception e) {}
		return rc;
	}
	/* doPower( 3.0, [ "+", "2" ...]) => "3"
	 * doPower( 3.0, [ "squared", "*", "2" ...]) => "9"
	 */
	private int factorial( int n ) {
		return n == 0 ? 1 : n * factorial( n - 1 );
	}
	private Float doPower(Float value) {
		//audit.in( "doPower", op +" ["+copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (!Float.isNaN( value )) {
			if (idx<size() || !nextOp.equals("")) {
				op = getOp();
				if (op.equals( "cubed" )) {
					op = ""; // consumed!
					value = value * value * value;
				} else if (op.equals( "squared" )) {
					op = ""; // consumed!
					value *= value;
				} else if (op.equals( "factorial" )) {
					op = ""; // consumed!
					value = (Float.isNaN( value ) || !isInteger) ? Float.NaN
							 : (float)factorial( Math.round( value ));  // simple factorial?
				} else if (op.equals( "to the power of" )) {
					op = ""; // consumed!
					try {
						value = (float) Math.pow( (double)value, (double)doProduct( doPower( getNumber() )));
					} catch (Exception e) {
						value = Float.NaN;
					}
				} else
					nextOp = op;
		}	}
		//audit.out( value );
		return value;
	}
	/*
	 * product: restarts the product() process
	 * product( 3.0, [ "+", "2" ...]) => "3"
	 * product( 3.0, [ "*", "2", "+" ...]) => "6"
	 */
	/*
	 * Theres a bug here in that op and postOp should be dealt with in their own methods.
	 */
	private Float doProduct(Float value) {
		//audit.in( "doProduct", op +" ["+ copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (!Float.isNaN( value )) {
			// to process here we need an op and a value
			while( idx < size() ) { // must be at least two array items, e.g. ["x", "2", ...
				op = getOp();
				if (op.equals( "times" ) || op.equals( "x" )) {
					op = ""; // consumed!
					value *= doPower( getNumber());
				} else if (op.equals( "divided by" ) || op.equals( "/" )) {
					op = ""; // consumed!
					value /= doPower( getNumber());
				//} else if (op.equals( "all" )) {
				//	op = ""; // consumed!
				//	value = doPower( value );
				} else {
					nextOp = op;
					break;
			}	}
			if (idx >= size() && !nextOp.equals(""))
				value = doPower( value );
		}
		//audit.out( value );
		return value;
	}
	/*
	 * term([ "1", "+", "2" ]) => 3
	 * term([ "1", "+", "2", "*", "3" ]) => 7
	 */
	public Float doTerms() {
		//audit.in( "doTerms", op +", ["+ copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		Float value = doProduct( doPower( getNumber() ));
		if (!Float.isNaN( value )) {
			while (idx < size()) {
				op = getOp();
				if (op.equals( "plus" ) || op.equals( "+" )) {
					op = ""; // consumed!
					value += doProduct( doPower( getNumber() ));
				} else if (op.equals( "minus" ) || op.equals( "-" )) {
					op = ""; // consumed!
					value -= doProduct( doPower( getNumber() ));
				} else if (op.equals( "all" )) {
					op = ""; // consumed!
					value = doProduct( value );
				} else {
					value = doProduct( doPower( getNumber() ));
					nextOp = op;
					break;
			}	}
			if (!nextOp.equals(""))
				value = doProduct( value );
			if (idx < size())
				audit.ERROR( idx +" not end of array, on processing: "+ get( idx ));
		}
		//audit.out( value );
		return value;
}	}