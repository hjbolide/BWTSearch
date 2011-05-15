import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * BWTSearch stub file. This class has to be filled in with your code.
 */
public class BWTSearch {

	// three bufferedReader to read from file
	BufferedReader topBr, mapBr, bwtBr;

	ArrayList<String> topOutput;
	Stack<String> stack;
	Stack<Character> cstack;
	String topStructure;
	HashMap<Character, String> hm;

	ArrayList<String> mapContent;
	ArrayList<Character> topContent;
	ArrayList<Character> bwtContent;
	ArrayList<String> cdaContent;
	// BWTsorting and constructing
	BWT bwtTool;

	// tool class to search the bwt file
	Algo algo;
	XpathQuery XpathQueryTool;

	/**
	 * Public constructor for BWTSearch. Any initialization that you need to do
	 * in preparation for the search should be done here.
	 * 
	 * 
	 * @param top
	 *            a Reader for the topology file
	 * 
	 * @param map
	 *            a Reader for the mappings file
	 * 
	 * @param bwt
	 *            a Reader for the BWT file.
	 * 
	 */
	public BWTSearch(Reader top, Reader map, Reader bwt) {
		// Your code here.

		// allocate 3 bufferedReader to read 3 files
		topBr = new BufferedReader(top);
		mapBr = new BufferedReader(map);
		bwtBr = new BufferedReader(bwt);

		// store the content of 3 files
		mapContent = new ArrayList<String>();
		topContent = new ArrayList<Character>();
		bwtContent = new ArrayList<Character>();
		cdaContent = new ArrayList<String>();

		// store the output topStructure
		topStructure = new String();

		// XpathQuery tool to parse and get the topStructure
		XpathQueryTool = new XpathQuery();

		// used to help recover the text
		stack = new Stack<String>();
		cstack = new Stack<Character>();

		// another form of map file
		hm = new HashMap<Character, String>();
		try {

			// construct the mapContent
			// and also the HashMap
			while (mapBr.ready()) {
				String tmp = mapBr.readLine();
				char c = tmp.substring(tmp.lastIndexOf('|') + 1).charAt(0);
				String mapC = tmp.substring(0, tmp.lastIndexOf('|'));
				hm.put(c, mapC);
				mapContent.add(tmp);
			}

			// construct the topContent
			while (topBr.ready()) {
				topContent.add((char) topBr.read());
			}

			// construct the bwtContent
			while (bwtBr.ready()) {
				bwtContent.add((char) bwt.read());
			}

			// closing stream
			topBr.close();
			mapBr.close();
			bwtBr.close();

			// tool used to search and recover
			algo = new Algo(bwtContent);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * XPath search method. Writes out uncompressed XML results to a Writer.
	 * 
	 * 
	 * @param xpath
	 *            the XPath to be evaluated.
	 * 
	 * @param output
	 *            the Writer to write out the XPath XML results to.
	 * 
	 */
	public void search(String xpath, Writer output) {
		// Your code here.

		// parse the xquery, and get the topStructured output.
		topOutput = XpathQueryTool.getPositions(mapContent, topContent,
				bwtContent, xpath);

		// get the number used in .cda file
		int counter = 0;

		// iterate topOutput, to construct xml, cda, top files
		Iterator<String> iterator = topOutput.iterator();

		if (output == null) {
			// header
			topStructure = "z";
			
			counter ++;
			
			// body
			while (iterator.hasNext()) {
				String tmp = iterator.next();
				if(Character.isLetter(tmp.charAt(0))) {
					topStructure += tmp.charAt(0);
					cstack.push(tmp.charAt(0));
					cdaContent.add("[" + counter + "]");
				} 
				if(tmp.charAt(0) == '.') {
					topStructure += ".";
				}
				if(tmp.charAt(0) == '$') {
					String rec = algo.recover("[" + counter + "]");
					topStructure += '$';
					cdaContent.add(rec);
				}
				counter ++;
			}

			// footer
			topStructure += ".";
		} else

			try {

				// header
				output.write("<result>");
				topStructure = "z";
				
				counter = 0;
				
				// body
				while (iterator.hasNext()) {
					String tmp = iterator.next();
					if(Character.isLetter(tmp.charAt(0))) {
						output.write("<" + hm.get(tmp.charAt(0)) + ">");
						topStructure += tmp.charAt(0);
						cstack.push(tmp.charAt(0));
					} else if(tmp.charAt(0) == '.') {
						output.write("</" + hm.get(cstack.pop()) + ">");
						topStructure += ".";
					} else {
						String rec = algo.recover("[" + tmp + "]");
						output.write(rec);
						topStructure += '$';
						cdaContent.add(rec);
					}
					counter ++;
				}

				// footer
				output.write("</result>");
				topStructure += ".";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	/**
	 * XPath search method. Writes out compressed XML results to Writers.
	 * 
	 * 
	 * @param xpath
	 *            the XPath to be evaluated.
	 * 
	 * @param top
	 *            the Writer to write out the XPath results topology to. Uses
	 *            the mappings data provided in the constructor. The format of
	 *            the XPath results topology is the same as the topology data
	 *            provided in the constructor.
	 * 
	 * @param bwt
	 *            the Writer to write out the XPath results BWT compressed text
	 *            to.
	 * 
	 */
	public void search(String xpath, Writer top, Writer bwt) {
		// Your code here.
		this.search(xpath, null);
		bwtTool = new BWT();
		
	}
}