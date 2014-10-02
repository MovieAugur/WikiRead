/**
 * 
 */
package augur.org;

import java.io.IOException;

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
		Wiki reader = new Wiki();
		String pageTitle = "Augur";
		System.out.println(reader.getPageText(pageTitle));
	}

}
