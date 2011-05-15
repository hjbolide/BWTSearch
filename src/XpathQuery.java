import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

public class XpathQuery {
	@SuppressWarnings("unused")
	public ArrayList<String> getPositions(ArrayList<String> mapContent,
			ArrayList<Character> topContent, ArrayList<Character> bwtContent,
			String query) {

		ArrayList<String> ret = new ArrayList<String>();

		Iterator<String> iterator = mapContent.iterator();

		// replace the original text with mapped characters
		while (iterator.hasNext()) {
			String pattern = iterator.next();
			String[] patternChunk = pattern.split("\\|");
			query = query.replaceAll(patternChunk[0], patternChunk[1]);
			// System.out.println(query);
		}

		int length = topContent.size();

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

		int stageFlag = 0;

		int checkModePosMark = 0;
		int fetchModeStackSize = 0;
		boolean inStructure = false;

		int level = 0;

		Algo algo = new Algo(bwtContent);

		int tmpStackSize = 0;

		boolean searchResult = false;

		String currentPredicates = new String();
		Stack<Character> stack = new Stack<Character>();
		Stack<Character> tmpStack = new Stack<Character>();

		int tmpCheckPos = 0;
		int tmpPos = 0;
		int forCheckPos = 0;
		int checkPos = 0;

		/*
		for (position = 0; position < length; position++) {
			
			tmpC = topContent.get(position);

			if (tmpC == '.') {
				if(stack.size() > 0)
					stack.pop();
				if(fetchMode) 
					ret.add("#" +  position + "#" + ".");
			}
			
			if(queryLength == 1) {
				querySequence.set(0, '*');
			}
			if(stack.size() + 1 == queryIndex) {
				queryIndex --;
				if(fetchMode && level >= 0) {
					while(level-- != 0) {
					//	ret.add(".");
					}
					level ++;
				}
				fetchMode = false;
			}
			
			if(queryIndex >= queryLength)
				;//tarC = querySequence.get(queryLength-1);
			else 
				tarC = querySequence.get(queryIndex);
			
			if (Character.isLetter(tmpC)) {
				stack.push(tmpC);
			}
			
			if ((tmpC == tarC || tarC == '*') && Character.isLetter(tmpC)) {
				
				//if(queryIndex != queryLength - 1) {
				if(queryIndex < queryLength)
					queryIndex++;
				
				if(queryIndex == queryLength || queryIndex >= queryLength) 
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
								char c = topContent.get(position);
								if (c == '.') {
									tmpStack.pop();
								}
								if (Character.isLetter(c)) {
									tmpStack.push(c);
								}
								if (c == conditionC) {
									if (topContent.get(position+1) == '$'
											&& topContent.get(position+2) == '.') {
										// find the next $
										tmpPos = position + 2;
										tmpPos %= length;
										while (topContent.get(tmpPos) != '$') {
											++tmpPos;
											tmpPos %= length;
										}
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
					if(queryIndex >= queryLength)
						tarC = ' ';
					stageFlag = 3;
				} else {
					stack.pop();
					queryIndex--;
					fetchMode = false;
					position = tmpPos;
				}
			}

			if (tmpC == '$' && fetchMode && (stack.contains(tarC) || tarC == '*' || tarC == ' ')) {
				if (queryIndex >= queryLength - 1) {
					tmpPos = position + 1;
					while (topContent.get(tmpPos++) != '$') {
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
							for(int i = 0; i < stack.size()-1; i++) {
								ret.add("" + stack.get(i));
								level ++;
							}
						}
						
					}
					ret.add("" + preC + tmpPos);
				}
			} 
			
			preC = tmpC;
		}
		*/
		
		int count = 0;
		
		for (position = 0; position < length; position++) {
			
			if(fetchMode) {
				ret.add(""+tmpStack.peek());
				while(tmpStack.size()>0) {
					int tmpPosi = 0;
					char c = topContent.get(position);
					if(Character.isLetter(c)) {
						tmpStack.push(c);
						ret.add(""+ c);
					}
					if(c == '$') {
						tmpPosi = position + 1;
						while(topContent.get(tmpPosi++) != '$') {
							tmpPosi %= length;
						}
						tmpPosi--;
						ret.add("" + tmpPosi);
					}
					if(c == '.') {
						ret.add(".");
						tmpStack.pop();
					}
					position++;
				}
				position --;
				fetchMode = false;
				queryIndex --;
				count = 0;
				continue;
			}
			
			tmpC = topContent.get(position);
			
			if(Character.isLetter(tmpC)) {
				count++;
			}
			
			if(tmpC == '.') {
				count--;
				if(count == -1) {
					queryIndex --;
					count = 0;
				}
			}
			
			if (queryIndex == queryLength)
				tarC = ' ';
			else
				tarC = querySequence.get(queryIndex);

			if (tmpC == '.' && queryIndex != stack.size()) {
				if (stack.size() > 0)
					stack.pop();
				else
					break;
			}

//			if (stack.size() + 1 == queryIndex) {
//				queryIndex --;
//				fetchMode = false;
//			}

			if ((Character.isLetter(tmpC)) && (tmpC == tarC || tarC == '*')) {
				currentPredicates = predicates.get(tarC);

				if (currentPredicates == null) {
					preC = tmpC;
					if (queryIndex < queryLength)
						queryIndex++;
					stack.push(tmpC);
					if (queryIndex == queryLength) {
						tmpStack.clear();
						tmpStack.push(stack.pop());
						fetchMode = true;
					}
					continue;
				} else {
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
							if (currentPredicateChunk[predicateIndex].length() == 1
									&& !checkState) {
								conditionC = currentPredicateChunk[predicateIndex]
										.charAt(0);
								checkState = true;
							} else {
								searchPattern = currentPredicateChunk[predicateIndex];

								// record the current position
								tmpCheckPos = position;
								tmpStack.push(tmpC);
								position++;
								searchResult = false;
								tmpPos = 0;
								while (tmpStack.size() > 0) {
									char c = topContent.get(position);
									if (c == '.') {
										tmpStack.pop();
									}
									if (Character.isLetter(c)) {
										tmpStack.push(c);
									}
									if (c == conditionC) {
										if (topContent.get(position + 1) == '$'
												&& topContent.get(position + 2) == '.') {
											// find the next $
											tmpPos = position + 2;
											tmpPos %= length;
											while (topContent.get(tmpPos) != '$') {
												++tmpPos;
												tmpPos %= length;
											}
											;
											searchResult |= algo.match("["
													+ tmpPos + "]",
													searchPattern);
										}
									}
									position++;
								}
							}
						}
						finalSearchResult &= searchResult;
						if(!finalSearchResult)
							break;
						tmpPos = position - 1;
						position = forCheckPos;
					}
					if(finalSearchResult) {
						position = checkPos;
						stack.push(topContent.get(position));
						if(queryIndex < queryLength)
							queryIndex ++;
						if(queryIndex >= queryLength) {
							tarC = ' ';
							fetchMode = true;
							tmpStack.clear();
							tmpStack.push(topContent.get(position));
							stack.pop();
						}
					} else {
						fetchMode = false;
						position = tmpPos;
					}
				}
			}

		}
		return ret;
	}
}
