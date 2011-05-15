


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
	public ArrayList<String> getPositions(String fileName, String query,
			ArrayList<Character> tops) {

		// read the files
		File topFile = new File("files/" + fileName + ".top");
		File mapFile = new File("files/" + fileName + ".map");
		ArrayList<String> ret = new ArrayList<String>();
		
		/*if (query.equals("/*") || query.indexOf('/') == query.lastIndexOf('/')) {
			System.out.println('f');
			// here to output the whole text
			// first need to get the first $
			try {
				BufferedReader top = new BufferedReader(new FileReader(topFile));
				int position = 0;
				while (top.ready()) {
					if ((char) top.read() == '$') {
						break;
					}
					position++;
				}
				
				// output the whole text
				return null;

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

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
		boolean[] queryConditionFlag = new boolean[queryLength];
		Arrays.fill(queryConditionFlag, false);

		int queryIndex = 0;
		int position = 0;
		boolean fetchMode = false;
		int firstCondition = 0;

		String searchPattern = new String();
		char tmpC;
		char tarC = ' ';
		char conditionC = ' ';
		char matchedC = ' ';
		char preC = ' ';
		int checkModePosMark = 0;
		int fetchModeStackSize = 0;
		boolean inStructure = false;
		
		int level = 0;

		Algo algo = new Algo(fileName);

		int tmpStackSize = 0;

		boolean searchResult = false;

		String currentPredicates = new String();
		Stack<Character> stack = new Stack<Character>();
		Stack<Character> tmpStack = new Stack<Character>();

		int tmpCheckPos = 0;
		int tmpPos = 0;
		int forCheckPos = 0;
		int checkPos = 0;

		for (position = 0; position < length; position++) {
			
			if(stack.size() + 1 == queryIndex) {
				queryIndex --;
				if(fetchMode && level >= 0) {
					while(level-- != 0) {
						ret.add(".");
					}
					level ++;
				}
				fetchMode = false;
			}
			
			tmpC = fileContent[position];
			if(queryIndex >= queryLength)
				tarC = querySequence.get(queryLength-1);
			else 
				tarC = querySequence.get(queryIndex);
			
			if (Character.isLetter(tmpC)) {
				stack.push(tmpC);
			}
			
			if ((tmpC == tarC || tarC == '*') && Character.isLetter(tmpC)) {
				//if(queryIndex != queryLength - 1) {
				if(queryIndex < queryLength)
					queryIndex++;
				
				if(queryIndex == queryLength - 1 || queryIndex >= queryLength) 
					fetchMode = true;
				//}

				
				currentPredicates = predicates.get(tarC);
				if (currentPredicates == null) {
					preC = tmpC;
					continue;
				}
				
				
				
				String[] currentPredicatesChunk = currentPredicates
						.split("[\\[\\]]");
				checkPos = position;
				boolean finalSearchResult = true;
				// get all the predicates
				for (int predicatesIndex = 0; predicatesIndex < currentPredicatesChunk.length; predicatesIndex++) {
					forCheckPos = position;
					if (currentPredicatesChunk[predicatesIndex].length() == 0)
						continue;
					String[] currentPredicateChunk = currentPredicatesChunk[predicatesIndex]
							.split("[\\~\"]");
					// get the conditionC and pattern
					boolean checkState = false;
					for (int predicateIndex = 0; predicateIndex < currentPredicateChunk.length; predicateIndex++) {
						if (currentPredicateChunk[predicateIndex].length() == 0)
							continue;
						if (currentPredicateChunk[predicateIndex].length() == 1 && !checkState) {
							conditionC = currentPredicateChunk[predicateIndex]
									.charAt(0);
							checkState = true;
						}
						else {
							searchPattern = currentPredicateChunk[predicateIndex];

							// record the current position
							tmpCheckPos = position;
							tmpStack.push(tmpC);
							position++;
							searchResult = false;
							tmpPos = 0;
							while (tmpStack.size() > 0) {
								char c = fileContent[position];
								if (c == '.') {
									tmpStack.pop();
								}
								if (Character.isLetter(c)) {
									tmpStack.push(c);
								}
								if (c == conditionC) {
									if (fileContent[position + 1] == '$'
											&& fileContent[position + 2] == '.') {
										// find the next $
										tmpPos = position + 2;
										while (fileContent[++tmpPos] != '$')
											;
										searchResult |= algo.match("[" + tmpPos
												+ "]", searchPattern);
									}
								}
								position++;
							}
						}
					}
					finalSearchResult &= searchResult;
					tmpPos = position - 1;
					position = forCheckPos;
				}
				if (finalSearchResult) {
					position = checkPos;
					if(queryIndex < queryLength)
						queryIndex ++;
				} else {
					stack.pop();
					queryIndex--;
					fetchMode = false;
					position = tmpPos;
				}
			}

			if (tmpC == '.') {
				if(stack.size() > 0)
					stack.pop();
			}

			if (tmpC == '$' && fetchMode && (stack.contains(tarC) || tarC == '*')) {
				if (queryIndex >= queryLength - 1) {
					tmpPos = position + 1;
					while (fileContent[tmpPos++] != '$') {
						tmpPos %= length;
					}
					tmpPos--;
					if(queryIndex > stack.size()-1) {
						;
					} else {
						if(level == 0 && queryLength != 1) {
							for(int i = 1; i < stack.size()-1; i++) {
								ret.add("" + stack.get(i));
								level ++;
							}
						} else if (queryLength == 1 && level == 0) {
							for(int i = 1; i < stack.size()-1; i++) {
								ret.add("" + stack.get(i));
								level ++;
							}
						}
					}
					ret.add("" + preC + tmpPos + ".");
				}
			}
			preC = tmpC;
		}

		return ret;

	}
}
