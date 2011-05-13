import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class XpathQuery {
	@SuppressWarnings("unused")
	public ArrayList<Integer> getPositions(String fileName, String query) {

		// read the files
		File topFile = new File("files/" + fileName + ".top");
		File mapFile = new File("files/" + fileName + ".map");

		BufferedReader map;
		try {
			map = new BufferedReader(new FileReader(mapFile));

			// replace the original text with mapped characters
			while (map.ready()) {
				String pattern = map.readLine();
				String[] patternChunk = pattern.split("\\|");
				query = query.replaceAll(patternChunk[0], patternChunk[1]);
				// System.out.println(query);
			}

			map.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<Integer> ret = new ArrayList<Integer>();
		int length = (int) topFile.length();
		char[] fileContent = new char[length];

		// store the query sequence of labels
		ArrayList<Character> querySequence = new ArrayList<Character>();

		// store the predicates
		// key: character
		// value: query conditions.
		HashMap<Character, String> predicates = new HashMap<Character, String>();
		for (int i = 0; i < query.length(); i++) {
			if (query.charAt(i) == '/') {
				querySequence.add(query.charAt(++i));
			}
			if (query.charAt(i) == '[') {
				String tmpS = "";
				boolean closed = false;
				char tmpC;

				while (i < query.length()) {
					tmpC = query.charAt(i);
					if (tmpC == '/' && closed)
						break;
					if (tmpC == ']')
						closed = true;
					tmpS += tmpC;
					i++;
				}
				predicates.put(querySequence.get(querySequence.size() - 1),
						tmpS);
				i--;
			}
		}

		boolean[] markForSequence = new boolean[querySequence.size()];
		Arrays.fill(markForSequence, false);

		int queryLength = querySequence.size();
		int queryIndex = 0;
		int position = 0;
		boolean fetchMode = false;
		boolean checkMode = false;
		boolean needAll = false;
		int level = 0;
		char tmpC;
		char tarC;
		char preC = ' ';
		ArrayList<Integer> checkList = new ArrayList<Integer>();
		
		String currentPredicates = new String();
		Stack<Character> stack = new Stack<Character>();
		try {
			BufferedReader top = new BufferedReader(new FileReader(topFile));
			while (top.ready()) {

				// get the next Char
				tmpC = (char) top.read();
				// get the expected Char
				tarC = querySequence.get(queryIndex);

				
				// if the tmpC is a character, push it to stack
				if (Character.isLetter(tmpC)) {
					stack.push(tmpC);
				}

				// if the tmpC equals to tarC, or tarC is a wildcard which can match everything
				// or the third situation is we can't make * to match .
				if ((tmpC == tarC || tarC == '*') && tmpC != '.') {
					
					// if the query still remains
					// increase the index to make it further
					if (queryIndex != queryLength - 1) {
						if((currentPredicates = predicates.get(tarC)) != null) {
							checkMode = true;
						}
						queryIndex++;
					}
					
					//otherwise we set to fetch mode
					if (queryIndex == queryLength - 1) {
						fetchMode = true;
					}

				}

				// if the tmpC is ., we pop the first element from the stack
				if (tmpC == '.') {
					stack.pop();
					if (stack.size() < queryLength - 1) {
						queryIndex--;
						fetchMode = false;
					}
				}

				// is the tmpC is $, and mode is fetch, also the one we need
				// store the $ position.
				if (tmpC == '$' && fetchMode && (tarC == preC || tarC == '*')) {
					ret.add(position);
				}
				preC = tmpC;
				position++;

				// meet the expected Char
				/*
				 * if(tmpC == tarC && !fetchMode) {
				 * 
				 * need to add the predicate handle
				 * 
				 * 
				 * 
				 * // mark the expected one, looking forward to the next one
				 * markForSequence[queryIndex] = true;
				 * 
				 * // here we need to meet all leaves if(queryIndex ==
				 * querySequence.size()-1) { fetchMode = true; } else {
				 * queryIndex ++; }
				 * 
				 * if(fetchMode) {
				 * 
				 * // here to extract all the leaves level += 1;
				 * 
				 * } }
				 * 
				 * // if meet the wildcard if(tarC == '*') {
				 * 
				 * need to add the predicate handle
				 * 
				 * 
				 * queryIndex++; }
				 * 
				 * // if fetchMode is true, then we have to extract $s if(tmpC
				 * == '$' && fetchMode && preC == tarC) { ret.add(position); }
				 * 
				 * if(tmpC == '.' && fetchMode && ret.contains(position-1)) {
				 * level -= 1; if(level == 0) { fetchMode = false; } }
				 * 
				 * if(tmpC == '.' && level == 0) {
				 * 
				 * }
				 * 
				 * position ++; preC = tmpC;
				 */
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ret;

	}
}
