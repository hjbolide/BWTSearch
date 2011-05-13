import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

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
	public char[] fileContent;

	// table C to store all the rank
	public int[] tableC;

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
			if (fileContent[pos - i] == c) {
				occ += 1;
			}
		}
		return occ;
	}

	// search function
	public void search(String fileName, String searchPattern) {
		File bwtFile = new File("files/" + fileName);
		length = (int) bwtFile.length();

		// get all those useful numbers
		sizeOfBucket = (int) Math.ceil(Math.log(length) / Math.log(2));
		numberOfBuckets = (int) Math.ceil(length / sizeOfBucket);
		sizeOfSuperblock = sizeOfBucket * sizeOfBucket;
		numberOfSuperblocks = (int) Math.ceil(length / sizeOfSuperblock);

		// here to implement the occ function
		try {
			BufferedReader bwt = new BufferedReader(new FileReader(bwtFile));

			// allocate the memory for fileContent
			fileContent = new char[length];

			int[] tableCOriginal = new int[256];
			tableC = new int[256];

			Arrays.fill(tableCOriginal, (int) 0);
			Arrays.fill(tableC, (int) 0);

			int index = 0;

			// the loop of reading the file, construct tableC
			while (bwt.ready()) {
				int tempChar = bwt.read();
				tableCOriginal[tempChar]++;
				fileContent[index++] = (char) tempChar;
			}

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

			bwt = null;
			bwt = new BufferedReader(new FileReader(bwtFile));

			while (bwt.ready()) {
				for (int i = 0; i < numberOfBuckets; i++) {

					// fill in content for every bucket
					for (int j = 0; j < sizeOfBucket && bwt.ready(); j++) {
						int tempChar = mappingArray[bwt.read()];

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
			 * for (int i = 0; i < numberOfBuckets; i++) { for (int j = 0; j <
			 * numberOfChars; j++) System.out.print(tablesForBucket[i][j] +
			 * "\t"); System.out.println(); }
			 * 
			 * System.out.println();
			 * 
			 * for (int i = 0; i < numberOfSuperblocks; i++) { for (int j = 0; j
			 * < numberOfChars; j++) System.out.print(tablesForSuperblock[i][j]
			 * + "\t"); System.out.println(); }
			 */

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
			} else {
				System.out.println("First: " + first + "\nLast: " + last);
			}

			System.out.println(fileContent[first]);
			System.out.println(tableC[fileContent[first]]);
			System.out.println(occ(fileContent[first], first)-1);
			
			File output = new File("files/dblp300k.cda");
			FileWriter fw = new FileWriter(output);
			BufferedWriter bw = new BufferedWriter(fw);
			
			char[] buffer = new char[128];
			int mark = 0;
			boolean score = true;
			do{
				for(i = 127; i >= 0; i -- ) {
					if(++mark == length) {
						score = false;
						break;
					}
					buffer[i] = fileContent[first];
					first = tableC[fileContent[first]] + occ(fileContent[first], first) - 1;
				}
				bw.write(buffer, 0, buffer.length);
			} while (score);
			bw.close();
			fw.close();
			
			System.out.println();
			
			System.out.println("The program takes: "
					+ (Runtime.getRuntime().totalMemory() - Runtime
							.getRuntime().freeMemory()) / 1048576
					+ " Mb to run");
			bwt.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}