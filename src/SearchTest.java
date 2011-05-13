import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class SearchTest {
	
	public static int occ(char c, long pos, int lengthOfBucket, int sizeOfSuperblock, short[][] tablesForBucket, int[][] tablesForSuperblock, int[] mappingArray) {
		int bucketNO = (int)Math.ceil(pos/lengthOfBucket);
		int superblockNO = (int)Math.floor(pos/sizeOfSuperblock);
		if(superblockNO != 0) {
			return tablesForSuperblock[superblockNO][mappingArray[c]] + tablesForBucket[bucketNO][mappingArray[c]];
		} else {
			return tablesForBucket[bucketNO][mappingArray[c]];
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File bwtFile = new File("files/dblp300k.bwt");
		long length = bwtFile.length();
		int lengthOfBuckets = (int) Math.ceil(Math.log(length) / Math.log(2));
		int numberOfBuckets = (int) Math.ceil(length/lengthOfBuckets);
		int sizeOfSuperblock = lengthOfBuckets * lengthOfBuckets;
		int numberOfSuperblocks = (int) Math.ceil(length/sizeOfSuperblock);

		// here to implement the occ function
		try {
			BufferedReader bwt = new BufferedReader(new FileReader(bwtFile));
			
			// save the whole bwt file
			
			char[] fileContent = new char[(int)length];
			
			// initial the original table c.
			int[] tableCOriginal = new int[256];
			int[] tableC = new int[256];
			
			Arrays.fill(tableCOriginal, (int)0);
			Arrays.fill(tableC, (int)0);
			
			int index = 0;
			// the first loop of reading file, construct table c
			while(bwt.ready()) {
				int tmpChar = bwt.read();
				tableCOriginal[tmpChar] ++;
				fileContent[index++] = (char)tmpChar;
			}
			
			short numberOfChars = 0;
			int[] mappingArray = new int[256];
			
			// construct mappingArray
			if(tableCOriginal[0] != 0) {
				numberOfChars++;
				mappingArray[0] = 0;
			}
			for(int i = 1; i < 256; i ++) {
				if(tableCOriginal[i] != 0) {
					mappingArray[i] = numberOfChars++;
				}
				tableC[i] = tableCOriginal[i-1] + tableC[i-1];
			}
			
			bwt = null;
			bwt = new BufferedReader(new FileReader(bwtFile));
			
			// two tables for 2 structures
			short[][] tablesForBucket = new short[numberOfBuckets][numberOfChars];
			int[][] tablesForSuperblock = new int[numberOfSuperblocks][numberOfChars];

			int countForSuperblock = 0;
			int superblockIndex = 0;
			
			while(bwt.ready()) {
				// fill in content for tableForBucket
				for(int i = 0; i < numberOfBuckets; i ++) {
					
					// fill in content for every bucket
					for(int j = 0; j < lengthOfBuckets && bwt.ready(); j ++) {
						int tempChar = mappingArray[bwt.read()];
						if(i%lengthOfBuckets != 0 && j == 0) {
							for(int k = 0; k < numberOfChars; k ++) {
								tablesForBucket[i][k] = tablesForBucket[i-1][k];
							}
						}
						tablesForBucket[i][tempChar]++;
						// fill in content for superblock
						if(++countForSuperblock == sizeOfSuperblock) {
							if(superblockIndex == 0) {
								for(int k = 0; k < numberOfChars; k ++) {
									tablesForSuperblock[superblockIndex][k] = tablesForBucket[i][k];
								}
							} else {
								for(int k = 0; k < numberOfChars; k ++) {
									tablesForSuperblock[superblockIndex][k] = tablesForBucket[i][k] + tablesForSuperblock[superblockIndex-1][k];
								}
							}
							countForSuperblock = 0;
							superblockIndex++;
						}
					}
				}
			}
			
			String pattern = "Sandra";
			
			int i = pattern.length()-1;
			char c = pattern.charAt(i);
			int first = tableC[c] + 1;
			int last = tableC[c+1];
			
			while((first <= last) && (i >= 2)) {
				c = pattern.charAt(i-1);
				first = tableC[c] + occ(c, first-1, lengthOfBuckets, sizeOfSuperblock, tablesForBucket, tablesForSuperblock, mappingArray) + 1;
				last = tableC[c] + occ(c, last, lengthOfBuckets, sizeOfSuperblock, tablesForBucket, tablesForSuperblock, mappingArray);
				i--;
			}
			
			if(last < first) {
				System.out.println("Not found");
			} else {
				System.out.println("First: " + first + "\nLast:  " + last);
			}
			
			System.out.println("The program takes: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576 + " Mb to run");
			bwt.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
