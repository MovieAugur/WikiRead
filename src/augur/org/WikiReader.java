/**
 * 
 */
package augur.org;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.wikipedia.Wiki;

/**
 * @author Aniruddha
 *
 */
public class WikiReader {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		//Wiki reader = new Wiki();
		//String pageTitle = "Augur";
		// System.out.println(reader.getPageText(pageTitle));
		findMoviesByYear(2012);
	}

	/**
	 * Finds all the movies that released in the year specified
	 * 
	 * @param year
	 * @return The list of wikipedia movie URLs
	 * @throws IOException 
	 */
	public static List<String> findMoviesByYear(int year) throws IOException {
		List<String> movieUrlList = new ArrayList<String>();
		Wiki reader = new Wiki();
		String pageTitle = year + " in film";
		String pageText = reader.getPageText(pageTitle);
		
		String table = getMovieByYearTable(pageText, year);
		String Xml = wikiTableToXml(table);
		
//		 System.out.println(pageText);
		
		return movieUrlList;
	}

	private static String getMovieByYearTable(String pageText, int year) throws IOException {
		dumpToLog(pageText, "movies.txt");
		return null;
	}

	private static void dumpToLog(String text, String filePath) throws IOException {
		Path file = Paths.get(filePath);
		Files.write(file, text.getBytes());
	}

	/**
	 * Converts the string wiki table to parse-able XML
	 * @param text the table text
	 * @return the XML string
	 */
	private static String wikiTableToXml(String text) {
		
		return null;
	}

}
