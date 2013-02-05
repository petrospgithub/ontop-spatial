package sesameWrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.openrdf.repository.Repository;

public class QuestSesameCMD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Repository repo = null;
		org.openrdf.repository.RepositoryConnection conn = null; 
		//check argument correctness
		if (args.length != 3 && args.length !=4) {
			   System.out.println("Usage:");
			   System.out.println(" QuestSesameCMD owlfile obdafile queryfile [outputfile]");
			   System.out.println("");
			   System.out.println(" owlfile    The full path to the OWL file");
			   System.out.println(" obdafile   The full path to the OBDA file");
			   System.out.println(" queryfile  The full path to the file with the SPARQL query");
			   System.out.println(" outputfile [OPTIONAL] The full path to output file");
			   System.out.println("");
			   return;
			  }
		
		//get parameter values
		String owlfile = args[0].trim();
		String obdafile = args[1].trim();
		String qfile = args[2].trim();
		String out = null;
		if (args.length == 4)
			out = args[3].trim();
		
		try {
			//create and initialize repo
			repo = new SesameVirtualRepo("test_repo", owlfile, obdafile, false, "TreeWitness");
			repo.initialize();
			conn = repo.getConnection();
			System.out.println("test_repo created and initialized...");
			String query="";
			//read query from file
			FileInputStream input = new FileInputStream(new File(qfile));
			byte[] fileData = new byte[input.available()];

			input.read(fileData);
			input.close();

			query =  new String(fileData, "UTF-8");
			
			System.out.println("Query: \n"+query);
			
			//execute query
			TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
			TupleQueryResultHandler handler = null;
			
			//set handler to output file or printout
			if (out != null) {

				FileOutputStream output = new FileOutputStream(new File(out));
				handler = new SPARQLResultsTSVWriter(output);
				
			} else {
				
				 handler = new SPARQLResultsTSVWriter(System.out);
			}
			//evaluate the query
			tupleQuery.evaluate(handler);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			conn.close();
			} catch (Exception e1) {
				
			}
			
			try {
			repo.shutDown();
			} catch (Exception e1) {
				
			}
			System.out.println("Done.");
		}
		
	}

}
