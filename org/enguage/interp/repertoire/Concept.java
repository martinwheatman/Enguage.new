package org.enguage.interp.repertoire;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/* ANDROID --
import android.app.Activity;
// */
import org.enguage.Enguage;
import org.enguage.interp.intention.Intention;
import org.enguage.objects.Variable;
import org.enguage.objects.space.Ospace;
import org.enguage.util.Audit;

public class Concept {
	static public final String LOADING = "CONCEPT";
	static private       Audit   audit = new Audit( LOADING );
	
	static public void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( name( cname )),
			     newFile = new File( name( cname, "del" ));
			if (!oldFile.renameTo( newFile ))
				audit.ERROR( "renaming "+ oldFile +" to "+ newFile );
	}	}
	static public String name( String name, String ext ) {
		// would just return "concepts"/name.ext
		String fname = Concepts.LOCATION + name +"."+ ext;
		if (new File( fname ).exists())
			return fname; // found, return existing
		else {
			String[] names = new File( Concepts.LOCATION ).list();
			for ( String dir : names ) { // e.g. name="hello.txt"
				String[] components = dir.split( "\\." );
				if (components.length == 1 && new File( Concepts.LOCATION + components[ 0 ]+"/"+name+"."+ ext ).exists())
					return Concepts.LOCATION + components[ 0 ]+"/"+name+"."+ ext; // found, return existing component
		}	}
		return fname; // not found, new filename
	}	
	
	static public String name( String name ) {return name( name, "txt" );}
	
	static public boolean load( String name ) {return load( name, null, null );}
	static public boolean load( String name, String from, String to ) {
		boolean wasLoaded   = false,
		        wasSilenced = false,
		        wasAloud    = Enguage.shell().isAloud();
		
		Variable.set( LOADING, name );
		
		// silence inner thought...
		if (!Audit.startupDebug) {
			wasSilenced = true;
			Audit.suspend();
			Enguage.shell().aloudIs( false );
		}
		
		Intention.concept( name );
		InputStream  is = null;
		
		try { // ...add concept from user space...
			is = new FileInputStream( name( name ));
			Enguage.shell().interpret( is, from, to );
			wasLoaded = true;
		} catch (IOException e1) {
			InputStream is2 = null;
			try { // ...or add concept from asset...
				String fname = name( name );
				// ANDROID --
				//Activity a = (Activity) Enguage.context(); //*/
				is2 =   //a == null ?//*/
								new FileInputStream( Ospace.location() + fname )
						/*      : a.getAssets().open( fname ); //*/ ;
				Enguage.shell().interpret( is2, from, to );
				wasLoaded = true;
			} catch (IOException e2) { audit.ERROR( "name: "+ name +", not found" );
			} finally {if (is2 != null) try{is2.close();} catch(IOException e2) {} }
		} finally { if (is != null) try{is.close();} catch(IOException e2) {} }
		
		//...un-silence after inner thought
		if (wasSilenced) {
			Audit.resume();
			Enguage.shell().aloudIs( wasAloud );
		}
		
		Variable.unset( LOADING );
		return wasLoaded;
}	}
