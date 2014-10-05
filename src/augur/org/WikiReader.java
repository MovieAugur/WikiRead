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
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Wiki reader = new Wiki();
		// String pageTitle = "Augur";
		// System.out.println(reader.getPageText(pageTitle));

		List<String> movieList = new ArrayList<String>();

		for (int year = 1940; year < 2014; year++) {
			List<String> yearList = findMoviesByYear(year);
			movieList.addAll(yearList);
			dumpToLog(String.join("\n", yearList), "log\\movies" + year + ".txt");
		}

		dumpToLog(String.join("\n", movieList), "log\\movieList.txt");

		// findMoviesByYear(2012);
	}

	/**
	 * Finds all the movies that released in the year specified
	 * 
	 * @param year
	 * @return The list of wikipedia movie URLs
	 * @throws Exception
	 */
	public static List<String> findMoviesByYear(int year) throws Exception {
		Wiki reader = new Wiki();
		String pageTitle = year + " in film";
		String pageText = reader.getPageText(pageTitle);

		if (!reader.exists(new String[] { pageTitle })[0]) {
			return new ArrayList<>();
		}

		List<String> table = getMovieByYearTable(pageText, year);
		// String Xml = wikiTableToXml(table);

		// System.out.println(pageText);

		return table;
	}

	private static List<String> getMovieByYearTable(String pageText, int year) throws IOException {
		dumpToLog(pageText, "log\\movieWiki" + year + ".txt");
		int i, begin = 0, end = 0;
		List<String> table = new ArrayList<String>();

		String[] lines = pageText.split("\\n");
		begin = lines.length;
		for (i = 0; i < lines.length; i++) {
			if(lines[i].contains("==#==")) {
				begin = (i < begin) ? i : begin;
			}
			if(lines[i].contains("== # ==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("==" + year + " films==")) {
				begin = (i < begin) ? i : begin;
			}
			if(lines[i].contains("==Notable films released in " + year + "=="))
			{
				begin = (i < begin) ? i : begin;
			}
			if(lines[i].contains("==" + year + " Wide-release films=="))
			{
				begin = (i < begin) ? i : begin;
			}
			if(lines[i].contains("== " + year + " Wide-release films =="))
			{
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("==") && (lines[i].contains("Births") || lines[i].contains("death")) && (i > begin) && (begin != 0)) {
				end = i;
				break;
			}
		}
		if (begin == end) {
			return table;
		}
		for (i = begin; i < end; i++) {
			if (lines[i].contains("\'\'[[")) {
				String[] line = lines[i].split("\\|\\|", -1);
				String[] movie = line[0].split("\\[\\[");
				if (movie[1].contains("|")) {
					String[] movieTitle = movie[1].split("\\|");
					table.add(movieTitle[0]);
				} else {
					String[] movieTitle = movie[1].split("]]");
					table.add(movieTitle[0]);
				}
			}
		}
		return table;
	}

	private static void dumpToLog(String text, String filePath)
			throws IOException {
		Path file = Paths.get(filePath);
		Files.write(file, text.getBytes());
	}
}
