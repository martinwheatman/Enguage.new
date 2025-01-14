package org.enguage.sign.interpretant.intentions;

import org.enguage.sign.Config;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.symbol.Utterance;
import org.enguage.util.attr.Context;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

public class Reply {
	
	/** a Reply is a 'formatted' 'answer', with possibly a 'say' prefix
	 * [ prefix = "according to Douglas Adams"
	 *   answer = '42',
	 *   format = "the answer to life, the universe and everything is ..."
	 * ]
	 */
	
	public  static final String ANSWER_PH = "whatever"; // Placeholder

	private boolean repeated = false;
	public  void    repeated( boolean s ) {repeated = s;}
	public  boolean repeated()            {return repeated;}
	
	// This is used to control Intentions.mediate() loop
	private boolean done = false;
	public  Reply   doneIs( boolean b ) {done = b; return this;}
	public  boolean isDone() {return done;}
	
	private Response.Type  type = Response.Type.DNU;
	public  Response.Type  type() {return type;}
	public  Reply          type( Response.Type t ) {type = t; return this;}
	
	/* ------------------------------------------------------------------------
	 * Say list
	 */
	private static  Strings say = new Strings();
	public  static  Strings say() {return say;}
	public  static  void    say( Strings sa ) {
		if (sa == null)  // null to reset it!
			say = new Strings();
		else
			say.addAll( Terminator.addTerminator( sa ));
	}
	
	/* ------------------------------------------------------------------------
	 * Answer - a simple string "42" or list "coffee and biscuits"?
	 */
	private String noAnswer = "";	
	private String answer = noAnswer;	
	public  Reply  answer( String ans ) {answer = ans; return this;}
	public  String answer() {return answer;}
	
	/* ------------------------------------------------------------------------
	 * Format - the shape of the reply "x y Z" intention, e.g. x y 24
	 */
	private Strings format = new Strings();

	public  String  format() {return format.toString();}
	public  Reply   format( String  f ) {return format( new Strings( f ));}
	public  Reply   format( Strings f ) {
		format = Context.deref( f );
		return this;
	}
	
	private static  boolean verbatim = false; // set to true in handleDNU()
	private static  boolean isVerbatim() {return verbatim;}
	private static  void    verbatimIs( boolean val ) { verbatim = val; }

	/* previous() is used to retrieve the reply from the previous thought. It is
	 * used in implementing imagination.  If the imagination session goes ok,
	 * we need the reply from that session. Was implemented with the equiv 
	 * intention in previous C incarnation.
	 */
	private static  Strings previous = new Strings( "" );
	public  static  Strings previous( Strings rep ) { previous = rep; return previous; }
	public  static  Strings previous() {return previous;}
	
	/* ------------------------------------------------------------------------
	 * Common replies....
	 */
	public void toDnu( Strings thought ) {
		verbatimIs( true ); // repeat exactly on DNU
		// Construct the DNU format
		format( new Strings( Response.dnu() + ", ..." ));
		answer( thought.toString() );
		// must come after answer()
		type( Response.Type.SOZ );
		verbatimIs( false );
	}
	public void toIdk() {
		format( Response.dnk());
		type( Response.Type.DNK );
		answer( noAnswer ); // reset
	}
	
	/* ------------------------------------------------------------------------
	 * To strings....
	 */
	private Strings replyToStrings() {
		if (format.isEmpty())
			format = answer().isEmpty() ? Response.dnu() : new Strings( answer() ); 
			
		else if (format.contains( Strings.ELLIPSIS )) // if required put in answer (verbatim!)
			format.replace( Strings.ellipsis, answer() );
		
		else if (format.contains( Config.placeholder() ))
			format.replace( Config.placeholder(), answer() );
		
		return  Utterance.externalise( format, isVerbatim() );
	}
	public Strings sayThis() {
		Strings reply = replyToStrings();
		if (Utterance.understoodIs( type() != Response.Type.DNU )) {
			// used in disambiguation ordering :/
			if (!repeated())
				previous( reply ); // never used
			
		} else
			toDnu( Utterance.previous() );
		
		return reply;
	}
	public String toString() {return replyToStrings().toString();}
	
	/* ------------------------------------------------------------------------
	 *  Intention
	 */
	public Reply reply( Strings values ) {
		// Accumulate reply - currently "say this"
		if (values.equals( Config.accumulateCmds() )) // say so -- possibly need new intention type?
			Reply.say( sayThis());
		
		// propagate reply and return - currently "say so"
		else if (values.equals( Config.propagateReplys() ))
			doneIs( true ); // just pass out this reply
		
		else {// reply "I don't understand" is like an exception?
			format( new Strings( values ));
			type( Response.typeFromStrings( values ));
			doneIs( type() != Response.Type.DNU );
		}
		return this;
}	}
