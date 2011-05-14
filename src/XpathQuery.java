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
		int firstCondition = 0;

		String searchPattern = new String();
		char tmpC;
		char tarC;
		char careC;
		char conditionC = ' ';
		char searchTmpC;
		char preC = ' ';
		ArrayList<Integer> goodList = new ArrayList<Integer>();
		ArrayList<Integer> skipList = new ArrayList<Integer>();
		int checkModePosMark = 0;
		int fetchModeStackSize = 0;
		int tmpPosition = 0;

		Algo algo = new Algo();

		int tmpStackSize = 0;
		int tmpGoodPosition = 0;

		boolean searchResult = false;

		String currentPredicates = new String();
		Stack<Character> stack = new Stack<Character>();
		Stack<Character> tmpStack = new Stack<Character>();
		for (position = 0; position < length; position++) {
			
			if(skipList.contains(position)) {
				tmpStack.clear();
				tmpStack.push(fileContent[position++]);
				while(tmpStack.size() > 0) {
					char c = fileContent[position++];
					if(Character.isLetter(c))
						tmpStack.push(c);
					if(c == '.') 
						tmpStack.pop();
				}
			}
			
			// the char from file
			tmpC = fileContent[position];

			// the char from querySequence
			tarC = querySequence.get(queryIndex);

			if (Character.isLetter(tmpC)) {
				stack.push(tmpC);
			}

			// if the expected char shows up
			if (tmpC == tarC || tarC == '*') {
				currentPredicates = predicates.get(tarC);
				if (currentPredicates != null) {
					// need to check the nodes
					checkMode = true;
					tmpGoodPosition = position;
					tmpStackSize = stack.size();
					careC = tmpC;
					String[] currentPredicatesChunk = currentPredicates
							.split("[\\[\\]]");
					for (int i = 0; i < currentPredicatesChunk.length; i++) {
						if (currentPredicatesChunk[i].length() != 0) {
							firstCondition = i;
							break;
						}
					}
					for (int i = 0; i < currentPredicatesChunk.length; i++) {
						if (currentPredicatesChunk[i].length() == 0)
							continue;
						String[] currentPredicateChunk = currentPredicatesChunk[i]
								.split("[\\~\"]");
						for (int j = 0; j < currentPredicateChunk.length; j++) {
							if(currentPredicateChunk[j].length() == 0)
								continue;
							if(currentPredicateChunk[j].length() == 1) {
								conditionC = currentPredicateChunk[j].charAt(0);
								continue;
							}
							
							searchPattern = currentPredicateChunk[j];
							checkModePosMark = position;

							while (stack.size() >= tmpStackSize - 1) {

								if (stack.size() == tmpStackSize) {
									careC = stack.peek();
								}

								searchTmpC = fileContent[++position];

								if(Character.isLetter(searchTmpC)) {
									
									stack.push(searchTmpC);
								}
								
								if (searchTmpC == '.') {
									stack.pop();
									if(stack.size() == tmpStackSize - 1) {
										if(searchResult && (i == firstCondition)) {
											// check if the first one 
											// direct add
											// otherwise intersect.
											goodList.add(tmpGoodPosition);
											searchResult = false;
										} else if( !searchResult ) {
											if(goodList.contains(tmpGoodPosition)) {
												goodList.remove(tmpGoodPosition);
											}
											skipList.add(tmpGoodPosition);
										}
										tmpGoodPosition = position+1;
									}
								}

								if (searchTmpC == conditionC) {
									if (fileContent[position + 2] != '.') {
										continue;
									}
									tmpPosition = position+2;
									while (fileContent[tmpPosition] != '$') {
										tmpPosition++;
									}
									if (algo.search("tiny.bwt", "["
											+ tmpPosition + "]", searchPattern)) {
										searchResult |= true;
									}
								}
							}

							position = checkModePosMark;
							stack.push(fileContent[position-1]);
							stack.push(fileContent[position]);
						}
					}
				}
				if(queryIndex == querySequence.size() - 1) {
					fetchMode = true;
					fetchModeStackSize = stack.size();
				} else {
					queryIndex++;
				}
					
			}

			if (tmpC == '.') {
				stack.pop();
				if(stack.size() == fetchModeStackSize - 2) {
					fetchMode = false;
				}
			}

			// store the char in this loop
			preC = tmpC;
		}

		return ret;

	}
}
