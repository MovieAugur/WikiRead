/**
 * 
 */
package augur.org;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.wikipedia.Wiki;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;

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
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load credentials from the file", e);
		}

		if (args.length != 2) {
			System.out.println("Usage: \"<exec> <yearBeg> <yearEnd>\"");
			return;
		}

		int yearBeg = Integer.parseInt(args[0]);
		int yearEnd = Integer.parseInt(args[1]);

		AmazonS3Client s3 = new AmazonS3Client(credentials);

		List<String> movieList = new ArrayList<String>();
		List<String> movieRevenueList = new ArrayList<String>();

		for (int year = yearBeg; year <= yearEnd; year++) {
			List<String> yearList = findMoviesByYear(year);
			movieList.addAll(yearList);
			for (String movie : yearList) {
				movieRevenueList.add(movie + "\t" + "NWX " + getRevenue(movie));
			}
			// Write to S3 - s3://augurframework/mapreduceinput/train
			// Write movie list to S3 - s3://augurframework/trainingmovielist
			String yearMovies = "";
			for (String movie : yearList) {
				yearMovies = yearMovies + movie + "\n";
			}

			String yearRevenues = "";
			for (String revenue : movieRevenueList) {
				yearRevenues = yearRevenues + revenue + "\n";
			}

			// dumpToLog(yearMovies, "movielist\\movies" + year + ".txt");
			File movieFile = new File("movielist/movies" + year + ".txt");
			dumpToLog(yearMovies, movieFile.toPath());
			s3.putObject("augurframework", "trainingmovielist/movies" + year,
					movieFile);

			File file = new File("revenuelist/revenues" + year + ".txt");
			dumpToLog(yearRevenues, file.toPath());
			s3.putObject("augurframework",
					"mapreduceInput/train/movies" + year, file);
			// dumpToLog(String.join("\n",movieList), "log\\movies" + year +
			// ".txt");
		}
		//
		// dumpToLog(String.join("\n", movieRevenueList),
		// "log\\movieRevenue.txt");
		// System.out.println(movieList);
		// dumpToLog(String.join("\n", movieList), "log\\movieList.txt");
		//
		// int revenue = getRevenue("Nick and Norah's Infinite Playlist");
		// System.out.println(revenue);
		// findMoviesByYear(2012);
	}

	/**
	 * Calculates the revenue made by a movie.
	 * 
	 * @param movieName
	 *            The Wikipedia page name of the movie
	 * @return The revenue made. Returns 0 if the page/info was not found
	 * @throws IOException
	 */
	public static int getRevenue(String movieName) throws IOException {
		Wiki reader = new Wiki();
		if (!reader.exists(new String[] { movieName })[0]) {
			return 0;
		}
		String pageText = reader.getPageText(movieName);
		if (pageText.contains("#REDIRECT")) {
			String[] temp = pageText.split("\\[\\[");
			String[] text = temp[1].split("\\]\\]");
			pageText = reader.getPageText(text[0]);
		}

		List<String> textList = new ArrayList<String>();
		textList = Arrays.asList(pageText.split("\n"));
		int braces = -1;
		for (String line : textList) {
			if (line.contains("{{Infobox")) {
				braces = 1;
			} else {
				if (line.contains("{{")) {
					braces++;
				}
				if (line.contains("}}")) {
					braces--;
				}
			}
			if (braces == 0) {
				break;
			}
			if (line.contains("gross")) {
				if (!line.contains("$")) {
					return 0;
				}
				int numberIndex = begOfNumbers(line);
				if(numberIndex == 0) {
					break;
				}
				return extractNumber(line.substring(numberIndex-1));
			}
		}

		return 0;
	}

	/**
	 * Converts the text into a number
	 * 
	 * @param text
	 *            The text in the form 1,000 or 1,234 million or 3 billion
	 * @return The integer value that this number represents. Truncates decimal
	 *         point
	 */
	public static int extractNumber(String text) {
		int base = 0, multiplier = 1;
		if (text.contains("million")) {
			multiplier = 1000000;
		}
		if (text.contains("billion")) {
			multiplier = 1000000000;
		}
		String number = new String(text.substring(1,
				endOfNumbers(text.substring(1)))).replaceAll(",", "");
		base = Integer.parseInt(number);
		return base * multiplier;
	}

	/**
	 * Used by extractNumber to parse the string input
	 * 
	 * @param numString
	 *            The numeric string
	 * @return The index of the last number in the string
	 */
	public static int endOfNumbers(String numString) {
		char[] charArray = numString.toLowerCase().replaceAll(",", "")
				.toCharArray();
		for (char c : charArray) {
			if (!Character.isDigit(c)) {
				return numString.indexOf(c) + 1;
			}
		}
		return numString.length();
	}

	public static int begOfNumbers(String line)
	{
		char[] cArray = line.toCharArray();
		for(char c : cArray) {
			if(Character.isDigit(c)) {
				return line.indexOf(c);
			}
		}
		return 0;
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

	/**
	 * Gets the table corresponding to the wiki page with movies of a given year
	 * 
	 * @param pageText
	 *            The text of the page
	 * @param year
	 *            The year the movies were released
	 * @return The list of movie names
	 * @throws IOException
	 */
	public static List<String> getMovieByYearTable(String pageText, int year)
			throws IOException {
		// dumpToLog(pageText, new File("log\\movieWiki" + year +
		// ".txt").toPath());
		int i, begin = 0, end = 0;
		List<String> table = new ArrayList<String>();

		String[] lines = pageText.split("\\n");
		begin = lines.length;
		for (i = 0; i < lines.length; i++) {
			if (lines[i].contains("==#==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("== # ==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("==" + year + " films==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("==Notable films released in " + year + "==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("==" + year + " Wide-release films==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("== " + year + " Wide-release films ==")) {
				begin = (i < begin) ? i : begin;
			}
			if (lines[i].contains("==")
					&& (lines[i].contains("Births") || lines[i]
							.contains("death")) && (i > begin) && (begin != 0)) {
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

	/**
	 * Write text to a log file. Rewrites if file is already present
	 * 
	 * @param text
	 *            The text to write
	 * @param filePath
	 *            The path of the file to write
	 * @throws IOException
	 */
	public static void dumpToLog(String text, Path file) throws IOException {
		Files.write(file, text.getBytes());
	}
}
