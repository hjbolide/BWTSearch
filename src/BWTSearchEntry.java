import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class BWTSearchEntry {

	// public static String CONDITION = "__condition__";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String XPathQuery = "/dblp/article/author";

		String XPathQuery1 = "/dblp/article/*";
		String XPathQuery2 = "/dblp/*[author~\"Manola\"]/title";
		String XPathQuery3 = "/dblp/*[author~\"Manola\"][title~\"Multimedia\"]";

		String[] XPathQueryChunk = XPathQuery.split("/");
		boolean hasCondition = false;
		int searchIndex = 0;
		String[] conditions = null;
		try {
			BufferedReader top = new BufferedReader(new FileReader(new File(
					"files/dblp300k.top")));
			BufferedReader map = new BufferedReader(new FileReader(new File(
					"files/dblp300k.map")));
			BufferedReader bwt = new BufferedReader(new FileReader(new File(
					"files/dblp300k.bwt")));

			BWTSearch bwtSearch = new BWTSearch(top, map, bwt);
			char abbreviation = 'a';

			for (int i = 1; i < XPathQueryChunk.length; i++) {
				// System.out.println(i + ": " + XPathQueryChunk[i]);
				if (!XPathQueryChunk[i].matches("^[a-zA-Z0-9_]+$")) {
					if (XPathQueryChunk[i].equals("*")) {
						hasCondition = true;
						searchIndex = i - 1;
					} else {
						conditions = XPathQueryChunk[i]
								.split("[\\*\\~\"\"\\[\\]]");
						for (int j = 0; j < conditions.length; j++) {
							// get like author~"Manola"
						}
					}
				}

				if (!hasCondition && (i == XPathQueryChunk.length - 1)) {
					// use XPathQueryChunk[i] to get the top position, then
					// fetch
					String tempStr = null;
					
					// get the abbreviation of the searching node
					while (null != (tempStr = map.readLine())) {
						if (tempStr.startsWith(XPathQueryChunk[i])) {
							abbreviation = tempStr.substring(
									tempStr.indexOf("|") + 1).charAt(0);
							System.out.println(abbreviation);
							break;
						}
					}
					int tempChar = -1;
					ArrayList<Integer> topPositions = new ArrayList<Integer>();
					int position = 0;
					
					// record all the position we need to search for
					while ((tempChar = top.read()) != -1) {
						if (abbreviation == tempChar) {
							if('$' == top.read()) {
								topPositions.add(++position);
							} else {
								
							}
						}
						position++;
					}
					/*
					 * Iterator<Integer> iterator = topPositions.iterator();
					 * while(iterator.hasNext()) {
					 * System.out.println(iterator.next()); }
					 */
					
				}
			}
			top.close();
			map.close();
			bwt.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
