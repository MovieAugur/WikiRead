/**
 * 
 */
package augur.org;

import java.io.IOException;
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
		
		String Xml = wikiTableToXml(pageText);
		
		System.out.println(pageText);
		
		return movieUrlList;
	}

	private static String wikiTableToXml(String pageText) {
		// TODO Auto-generated method stub
		return null;
	}

}
