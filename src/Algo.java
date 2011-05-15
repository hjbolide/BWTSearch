
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Algo {

	// useful numbers
	public int length;
	public int sizeOfBucket;
	public int numberOfBuckets;
	public int sizeOfSuperblock;
	public int numberOfSuperblocks;
	public int numberOfChars;

	// map the original chars to existing chars
	public int[] mappingArray;

	// two tables for 2 structures
	public short[][] tablesForBucket;
	public int[][] tablesForSuperblock;

	// store the file content
	public ArrayList<Character> fileContent;

	// table C to store all the rank
	public int[] tableC;

	public Algo(ArrayList<Character> fileContent) {

			this.fileContent = fileContent;

			int length = fileContent.size();
		
			int[] tableCOriginal = new int[256];
			tableC = new int[256];

			Arrays.fill(tableCOriginal, (int) 0);
			Arrays.fill(tableC, (int) 0);

			Iterator<Character> iterator = fileContent.iterator();
			
			while (iterator.hasNext()) {
				int tempChar = iterator.next();
				tableCOriginal[tempChar]++;
			}

			// get all those useful numbers
			sizeOfBucket = (int) Math.ceil(Math.log(length) / Math.log(2));
			numberOfBuckets = (int) Math.ceil(length / sizeOfBucket);
			sizeOfSuperblock = sizeOfBucket * sizeOfBucket;
			numberOfSuperblocks = (int) Math.ceil(length / sizeOfSuperblock);

			// the loop of reading the file, construct tableC

			numberOfChars = 0;
			mappingArray = new int[256];

			// construct mappingArray
			if (tableCOriginal[0] != 0) {
				numberOfChars++;
				// the first one should be 0
				// if the tco[0] is not 0
				mappingArray[0] = 0;
			}

			// construct the mappingArray
			for (int i = 1; i < 256; i++) {
				if (tableCOriginal[i] != 0) {
					mappingArray[i] = numberOfChars++;
				}
				tableC[i] = tableCOriginal[i - 1] + tableC[i - 1];
			}

			// two tables for 2 structures
			tablesForBucket = new short[numberOfBuckets][numberOfChars];
			tablesForSuperblock = new int[numberOfSuperblocks][numberOfChars];

			// two counter
			int countForSuperblock = 0;
			int superblockIndex = 0;

			int counter = 0;
			while (counter < length) {
				for (int i = 0; i < numberOfBuckets; i++) {

					// fill in content for every bucket
					for (int j = 0; j < sizeOfBucket && (counter < length); j++) {
						char c = fileContent.get(counter);
						counter++;
						int tempChar = mappingArray[c];

						// when i % sizeOfBucket == 0, that means a new
						// superblock starts
						// when j == 0, that means a new bucket starts.
						if (i % sizeOfBucket != 0 && j == 0) {
							for (int k = 0; k < numberOfChars; k++) {
								tablesForBucket[i][k] = tablesForBucket[i - 1][k];
							}
						}

						// count for the chars
						tablesForBucket[i][tempChar]++;

						// fill in content for superblock
						if (++countForSuperblock == sizeOfSuperblock) {
							if (superblockIndex == 0) {
								for (int k = 0; k < numberOfChars; k++) {
									tablesForSuperblock[superblockIndex][k] = tablesForBucket[i][k];
								}
							} else {
								for (int k = 0; k < numberOfChars; k++) {
									tablesForSuperblock[superblockIndex][k] = tablesForBucket[i][k]
											+ tablesForSuperblock[superblockIndex - 1][k];
								}
							}

							countForSuperblock = 0;
							superblockIndex++;
						}
					}
				}
			}
			/*
			 * while (bwt.ready()) { for (int i = 0; i < numberOfBuckets; i++) {
			 * 
			 * // fill in content for every bucket for (int j = 0; j <
			 * sizeOfBucket && bwt.ready(); j++) { int tempChar =
			 * mappingArray[bwt.read()];
			 * 
			 * // when i % sizeOfBucket == 0, that means a new // superblock
			 * starts // when j == 0, that means a new bucket starts. if (i %
			 * sizeOfBucket != 0 && j == 0) { for (int k = 0; k < numberOfChars;
			 * k++) { tablesForBucket[i][k] = tablesForBucket[i - 1][k]; } }
			 * 
			 * // count for the chars tablesForBucket[i][tempChar]++;
			 * 
			 * // fill in content for superblock if (++countForSuperblock ==
			 * sizeOfSuperblock) { if (superblockIndex == 0) { for (int k = 0; k
			 * < numberOfChars; k++) { tablesForSuperblock[superblockIndex][k] =
			 * tablesForBucket[i][k]; } } else { for (int k = 0; k <
			 * numberOfChars; k++) { tablesForSuperblock[superblockIndex][k] =
			 * tablesForBucket[i][k] + tablesForSuperblock[superblockIndex -
			 * 1][k]; } }
			 * 
			 * countForSuperblock = 0; superblockIndex++; } } } }
			 */
			
	}

	public int occ(char c, int pos) {
		int occ = 0;

		// get the current bucketNo and superblockNo
		int bucketNo = (int) Math.floor(pos / sizeOfBucket);
		int currentBucketWithinBlock = bucketNo % sizeOfBucket;
		int currentPosWithinBucket = pos % sizeOfBucket;
		int superblockNo = (int) Math.floor(pos / sizeOfSuperblock);

		// get the real representation of c
		int targetChar = mappingArray[c];

		// plus the former superblock
		if (superblockNo != 0) {
			occ += tablesForSuperblock[superblockNo - 1][targetChar];
		}

		// plus the former bucket
		if (currentBucketWithinBlock != 0) {
			occ += tablesForBucket[bucketNo - 1][targetChar];
		}

		// count within the bucket
		for (int i = 0; i <= currentPosWithinBucket; i++) {
			if (fileContent.get(pos - i) == c) {
				occ += 1;
			}
		}
		return occ;
	}

	public int search(String searchPattern) {

		int i = searchPattern.length() - 1;
		char c = searchPattern.charAt(i);
		int first = tableC[c];
		int last = tableC[c + 1] - 1;

		while ((first <= last) && (i >= 1)) {
			c = searchPattern.charAt(i - 1);
			first = tableC[c] + occ(c, first - 1);
			last = tableC[c] + occ(c, last) - 1;
			i--;
		}

		if (last < first) {
			System.out.println("No rows prefixed by " + searchPattern);
			return -1;
		}

		return first;
	}

	// search function
	public boolean match(String searchPattern, String matchPattern) {

		int first = search(searchPattern);

		if (first == -1) {
			return false;
		}

		StringBuffer buffer = new StringBuffer();
		while (true) {
			char tmpC = fileContent.get(first);
			if (tmpC == ']')
				break;
			buffer.append(fileContent.get(first));
			first = tableC[tmpC] + occ(tmpC, first) - 1;
		}
		if (buffer.reverse().toString().indexOf(matchPattern) != -1) {
			return true;
		} else {
			return false;
		}
	}

	public String recover(String searchPattern) {

		int position = search(searchPattern);

		StringBuffer buffer = new StringBuffer();
		while (true) {
			char tmpC = fileContent.get(position);
			if (tmpC == ']')
				break;
			buffer.append(fileContent.get(position));
			position = tableC[tmpC] + occ(tmpC, position) - 1;
		}
		return buffer.reverse().toString();
	}

}