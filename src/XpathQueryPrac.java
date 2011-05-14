import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class XpathQueryPrac {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		XpathQuery xpath = new XpathQuery();
		ArrayList<Integer> positions = new ArrayList<Integer>();
		ArrayList<Character> tops = new ArrayList<Character>();
		String fileName = "yahoo";
		positions = xpath.getPositions(fileName, "/root/listing/seller_info[seller_rating~\"new\"]/seller_name", tops);	
		Algo algo = new Algo(fileName);
		File output = new File("files/output.xml");
		try {
			FileWriter fw = new FileWriter(output);
			BufferedReader br = new BufferedReader(new FileReader(new File("files/" + fileName + ".map")));
			HashMap<Character, String> hm = new HashMap<Character, String>();
			while(br.ready()) {
				String tmp = br.readLine();
				char c = tmp.substring(tmp.lastIndexOf('|') + 1).charAt(0);
				String mapC = tmp.substring(0, tmp.lastIndexOf('|'));
				hm.put(c, mapC);
			}
			br.close();
			Iterator<Integer> intIterator = positions.iterator();
			Iterator<Character> charIterator = tops.iterator();
			String outputStr = new String();
			outputStr = "<result>";
			while(intIterator.hasNext() && charIterator.hasNext()) {
				String outputTag = hm.get(charIterator.next());
				String outputSearchPattern = "[" + intIterator.next() + "]";
				outputStr += "<" + outputTag + ">";
				outputStr += algo.recover(outputSearchPattern);
				outputStr += "</" + outputTag + ">";
			}
			outputStr += "</result>";
			fw.write(outputStr);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
