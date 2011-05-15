


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;



public class XpathQueryPrac {
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		XpathQuery xpath = new XpathQuery();
		ArrayList<String> positions = new ArrayList<String>();
		ArrayList<Character> tops = new ArrayList<Character>();
		String fileName = "tiny";
		//positions = xpath.getPositions(fileName, "/*", tops);
		positions = xpath.getPositions(fileName, "/dblp/article/*", tops);
		Algo algo = new Algo(fileName);
		File output = new File("output.xml");
		File topOutput = new File("output.top");
		File cdaOutput = new File("output.cda");
		try {
			FileWriter fw = new FileWriter(output);
			FileWriter tfw = new FileWriter(topOutput);
			FileWriter cfw = new FileWriter(cdaOutput);
			BufferedReader br = new BufferedReader(new FileReader(new File("files/" + fileName + ".map")));
			HashMap<Character, String> hm = new HashMap<Character, String>();
			while(br.ready()) {
				String tmp = br.readLine();
				char c = tmp.substring(tmp.lastIndexOf('|') + 1).charAt(0);
				String mapC = tmp.substring(0, tmp.lastIndexOf('|'));
				hm.put(c, mapC);
			}
			br.close();
			Iterator<String> iterator = positions.iterator();
			Stack<String> stack = new Stack<String>();
			Stack<Character> cstack = new Stack<Character>();
			int counter = 0;
			fw.write("<result>");
			tfw.write("R");
			counter ++;
			while(iterator.hasNext()) {
				String tmp = iterator.next();
				if(tmp.equals(".")) {
					fw.write("</" + stack.pop() + ">");
					tfw.write(".");
					cstack.pop();
					counter++;
				} else if (Character.isLetter(tmp.charAt(0)) && tmp.length() == 1) {
					fw.write("<" + hm.get(tmp.charAt(0)) + ">");
					tfw.write(tmp.charAt(0));
					
					stack.push(hm.get(tmp.charAt(0)));
					cstack.push(tmp.charAt(0));
					
					counter++;
				} else {
					char c = tmp.charAt(0);
					String str = tmp.substring(1, tmp.lastIndexOf('.'));
					fw.write("<" + hm.get(c) + ">");
					tfw.write(c);
					counter++;
					fw.write(algo.recover("[" + str + "]"));
					tfw.write("$");
					cfw.write("[" + counter + "]" + algo.recover("[" + str + "]"));
					fw.write("</" + hm.get(c) + ">");
					tfw.write(".");
					counter++;
				}
			}
			fw.write("</result>");
			tfw.write(".");
			fw.close();
			tfw.close();
			cfw.close();
			BWT bwt = new BWT();
			BufferedReader br1 = new BufferedReader(new FileReader(new File("files/output.cda")));
			String content = br1.readLine();
			byte[] byts = new byte[content.length()];
			bwt.compressBWT(content.getBytes(), byts);
			FileWriter bwtfw = new FileWriter(new File("files/output.bwt"));
			for(int i = 0; i < byts.length; i ++) {
				bwtfw.append((char)byts[i]);
			}
			bwtfw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}
}
