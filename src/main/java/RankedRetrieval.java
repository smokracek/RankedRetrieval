import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class RankedRetrieval {

	static TimerUtil timer = new TimerUtil();

	static Pattern pattern = Pattern.compile("[^'\\p{IsAlphabetic}]+");

	public static void print(String s) {
		System.out.println(timer.getTime() + "s\t" + s);
	}

	public static double tfidf(int t_f, int d_f, int corpusSize) {
		return (t_f == 0) ? 0 : (1.0 + Math.log10(t_f)) * (Math.log10((double) corpusSize / (double) d_f));
	}

	// the following two methods are borrowed user Raim Alam via the Stack Overflow
	// post at
	// https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
	// and are somewhat modified from their originals
	public static boolean ASC = true;
	public static boolean DESC = false;

	public static HashMap<String, Double> valueSort(Map<String, Double> unsortMap, final boolean order) {

		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

		// Sorting the list based on values
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				if (order) {
					return o1.getValue().compareTo(o2.getValue());
				} else {
					return o2.getValue().compareTo(o1.getValue());

				}
			}
		});

		// Maintaining insertion order with the help of LinkedList
		HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Entry<String, Double> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		return sortedMap;
	}

	public static void printMap(Map<String, Double> map, int num) {
		int i = 0;
		for (Entry<String, Double> entry : map.entrySet()) {
			if (!(i < 10)) {
				break;
			}
			print(entry.getKey() + "\t" + entry.getValue());
			i++;
		}
	}

	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) throws ParseException, ClassNotFoundException, IOException {

		String sample = args[0];

		String filename = sample + ".txt";

		DataAccess postingListDB = null;
		DataAccess magnitudesDB = null;

		// check if directory(s) DON'T exist
		// if they don't exist, do all the work
		if (!new File("/Users/sammokracek/Desktop/cs128/RankedRetrieval/" + sample + "PostingListDB").exists()
				|| !new File("/Users/sammokracek/Desktop/cs128/RankedRetrieval/" + sample + "MagnitudesDB").exists()) {

			print("postingListDB doesn't exist!");
			print("Initializing postingListDB...");
			postingListDB = new DataAccess(new Database(sample + "PostingListDB").db);

			print("magnitudesDB doesn't exist!");
			print("Initializing magnitudesDB...");
			magnitudesDB = new DataAccess(new Database(sample + "MagnitudesDB").db);

			// load posting list
			Scanner postingList = null;
			try {
				postingList = new Scanner(new File("/Users/sammokracek/Desktop/cs128/RankedRetrieval/" + filename));
				print("Posting List File loaded!");
			} catch (FileNotFoundException e) {
				print("File not found!");
				e.printStackTrace();
			}

			TreeSet<String> allURLs = new TreeSet<String>();

			JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

			print("Creating filling postingListDB");
			while (postingList.hasNextLine()) {
				String[] splitLine = postingList.nextLine().split("\t");
				String key = splitLine[0];
				JSONArray jsonArray = (JSONArray) parser.parse(splitLine[1]);
				ListIterator<Object> jsonIterator = jsonArray.listIterator(2);

				while (jsonIterator.hasNext()) {
					// construct URL and add to set of all URLs
					ArrayList<Object> posting = (ArrayList<Object>) jsonIterator.next();
					String prefix = posting.get(1).toString().equals("m") ? "https://en.m.wikipedia.org/wiki/"
							: "https://en.wikipedia.org/wiki/";
					String URLtoAdd = prefix + posting.get(0).toString();
					allURLs.add(URLtoAdd);
				}

				// add Term and list of its URLs to DB
				DAO DAOtoAdd = new DAO(key, jsonArray);
				try {
					postingListDB.put(DAOtoAdd);
				} catch (IOException e) {
					print("Error in DB storage!");
					e.printStackTrace();
				}
			}

			try {
				postingListDB.put(new DAO(0, allURLs.size()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			print("Successfully loaded posting list into postingListDB!");
			postingList.close();

			// create magnitude DB
			// load posting list again
			Scanner postingList2 = null;
			try {
				postingList2 = new Scanner(new File("/Users/sammokracek/Desktop/cs128/RankedRetrieval/" + filename));
				print("Posting List File loaded for magnitudesDB");
			} catch (FileNotFoundException e) {
				print("File not found!");
				e.printStackTrace();
			}

			// magnitudes DB
			print("Starting to create magnitudesDB!");
			int corpusSize = allURLs.size();
			print("Updating magnitudesDB");
			while (postingList2.hasNextLine()) {
				String[] splitLine = postingList2.nextLine().split("\t");
				JSONArray jsonArray = (JSONArray) parser.parse(splitLine[1]);
				ListIterator<Object> jsonIterator = jsonArray.listIterator(2);

				while (jsonIterator.hasNext()) {
					ArrayList<Object> posting = (ArrayList<Object>) jsonIterator.next();

					// calculate tfidf for document
					double tfidf = tfidf((int) posting.get(2), (int) jsonArray.get(0), corpusSize);

					// if document is already in DB, overwrite score
					Object oldScore = magnitudesDB.get(posting.get(0)).getValue();
					if (oldScore != null) {
						try {
							double newScore = (double) oldScore + Math.pow(tfidf, 2);
							magnitudesDB.put(new DAO(posting.get(0), newScore));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					// else add new document
					else {
						try {
							magnitudesDB.put(new DAO(posting.get(0), Math.pow(tfidf, 2)));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			print("Finished filling magnitudesDB!");
			postingList2.close();

		} else {
			print("postingListDB exists...");
			print("initializing postingListDB...");
			postingListDB = new DataAccess(new Database(sample + "PostingListDB").db);
			magnitudesDB = new DataAccess(new Database(sample + "MagnitudesDB").db);
			print("postingListDB initialized!");
			print("magnitudesDB initialized!");
		}

		// initialize some constants
		final int corpusSize = (int) postingListDB.get(0).getValue();
		print("Corpus size: " + corpusSize + " documents");

		Scanner scanner = new Scanner(System.in);

		// main control loop
		while (true) {
			System.out.println();
			print("Query: ");
			String input = scanner.nextLine().toLowerCase();
			print("");

			// quit
			if (input.equals("quit")) {
				break;
			}

			// get array representing tokenized sentence
			String[] queryRaw = pattern.split(input);

			// map terms to frequencies in query
			TreeMap<String, Integer> query = new TreeMap<String, Integer>();
			for (String term : queryRaw) {
				if (query.containsKey(term)) {
					query.put(term, query.get(term) + 1);
				} else {
					query.put(term, 1);
				}
			}

			// calculate scores
			TreeSet<String> queryKeys = new TreeSet<String>(query.keySet());
			HashMap<String, Double> docScores = new HashMap<String, Double>();
			for (String term : queryKeys) {
				if (postingListDB.get(term).getValue() == null) {
					continue;
				}
				// get posting list for term
				JSONArray postingList = (JSONArray) postingListDB.get(term).getValue();
				// get document frequency of term
				int df_t = (int) postingList.get(0) + 1;
				// alpha = calculate tfidf of term in query (query as a document)
				double alpha = tfidf(query.get(term), df_t, corpusSize + 1);
				// loop through docs
				ListIterator<Object> docs = postingList.listIterator(2);
				while (docs.hasNext()) {
					// Score[document] += alpha * (tfidf of term in document)
					JSONArray doc = (JSONArray) docs.next();
					String key = (String) doc.get(0);
					double score = tfidf((int) doc.get(2), df_t, corpusSize + 1);
					double newScore = alpha * score;
					if (docScores.get(key) != null) {
						double oldScore = docScores.get(key);
						docScores.put(key, oldScore + newScore);
					} else {
						docScores.put(key, newScore);
					}
				}
			}

			HashSet<String> scoreKeys = new HashSet<String>(docScores.keySet());
			for (String doc : scoreKeys) {
				// normalize score with magnitude of corresponding doc
				double score = docScores.get(doc);
				double magnitude = (Double) magnitudesDB.get(doc).getValue();
				double normalizedScore = score / Math.sqrt(magnitude);
				docScores.put(doc, normalizedScore);
			}

			// beautify top K doc URLs and pair with scores
			HashMap<String, Double> printingScores = new HashMap<String, Double>();
			scoreKeys = new HashSet<String>(docScores.keySet());
			for (String doc : scoreKeys) {
				printingScores.put(doc, (double) docScores.get(doc));
			}

			// print top K docs with scores
			printingScores = (HashMap<String, Double>) valueSort(printingScores, DESC);
			printMap(printingScores, 10);
		}

		scanner.close();
		print("Goodbye!");
		System.exit(0);
	}

}
