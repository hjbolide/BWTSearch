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

		// store the whole top file
		BufferedReader top;
		try {
			top = new BufferedReader(new FileReader(topFile));
			int count = 0;
			while (top.ready()) {
				fileContent[count++] = (char) top.read();
			}

			top.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		boolean searchMode = false;
		
		char leaf = querySequence.get(queryLength-1);

		String searchPattern = new String();
		char tmpC;
		char tarC;
		char preC = ' ';
		ArrayList<Integer> checkList = new ArrayList<Integer>();
		int checkModePosMark = 0;
		int searchModePosMark = 0;
		ArrayList<Boolean> searchResults = new ArrayList<Boolean>();
		boolean searchResult = true;
		Stack<Character> checkStack = new Stack<Character>();

		String currentPredicates = new String();
		Stack<Character> stack = new Stack<Character>();
		for (position = 0; position < length; position++) {

			// get the next Char
			tmpC = fileContent[position];
			// get the expected Char
			tarC = querySequence.get(queryIndex);

			// if is the checkMode, we need to do some search and then
			// see if the $ is good or not
			if (checkMode) {

				// filter the currentPredicates
				String[] currentPredicatesChunk = currentPredicates
						.split("[\\[\\]]");

				// recursively doing the check thing.
				for (int i = 0; i < currentPredicatesChunk.length; i++) {

					// filter the empty string
					if (currentPredicatesChunk[i].length() == 0)
						continue;

					// get the currentPredicate and filter the ~ and "
					String[] currentPredicate = currentPredicatesChunk[i]
							.split("[\\~\"]");

					// loop to search for the qualified node
					for (int j = 0; j < currentPredicate.length; j++) {
						if (currentPredicate[j].length() == 0)
							continue;
						if (currentPredicate[j].length() == 1) {
							checkStack.add(currentPredicate[j].charAt(0));
							continue;
						}
						searchPattern = currentPredicate[j];
						searchMode = true;
					}
					if (searchMode) {
						preC = tmpC;
						searchModePosMark = position;
						tarC = checkStack.peek();
						while (position < length) {
							tmpC = fileContent[position];

							// if the $ shows, and is the related one
							// we search for it.
							if (tmpC == '$' && preC == tarC) {
								// see if here is OK
								searchResult = true;
							}
							
							if (tmpC == '$' && fetchMode && (preC == leaf || leaf == '*')) {
								if(!checkList.contains(position))
									checkList.add(position);
							}

							// store the coming one
							if (Character.isLetter(tmpC)) {
								checkStack.add(tmpC);
							}

							// encounter ., then we need to pop up
							if (tmpC == '.') {
								checkStack.pop();

								// if the stack is empty
								// end of the search, rewind to the starting
								// position
								if (checkStack.size() == 0) {
									searchMode = false;
									if(i != currentPredicatesChunk.length - 1)
										position = searchModePosMark;
									else {
										stack.pop();
									}
									break;
								}
							}
							position++;
							preC = tmpC;
						}
					}
					// store the searchResult, we need it to see if all the
					// conditions are satisfied
					searchResults.add(searchResult);
				}

				// if one of those conditions is not satisfied.
				// we don't need to rewind.
				// else we rewind it to fetch all the nodes.
				if (!searchResults.contains(false)) {
					checkMode = false;
					continue;
				} else {
					checkList.clear();
				}

				if (Character.isLetter(tmpC)) {
					stack.add(tmpC);
				}

				if (tmpC == '.') {
					stack.pop();
					if (stack.size() == checkModePosMark) {
						checkMode = false;
						queryIndex--;
					}
				}

				preC = tmpC;
			} else {
				// if the tmpC is a character, push it to stack
				if (Character.isLetter(tmpC)) {
					stack.push(tmpC);
				}

				// if the tmpC equals to tarC, or tarC is a wildcard which
				// can match everything
				// or the third situation is we can't make * to match .
				if ((tmpC == tarC || tarC == '*') && tmpC != '.') {

					// if the query still remains
					// increase the index to make it further
					// if (queryIndex != queryLength - 1) {
					if ((currentPredicates = predicates.get(tarC)) != null) {
						checkMode = true;
						checkModePosMark = queryIndex;
					}
					if (queryIndex != queryLength - 1) {
						queryIndex++;
					}
					// }

					// otherwise we set to fetch mode
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
			}
		}

		return ret;

	}
}
