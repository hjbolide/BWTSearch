import java.util.ArrayList;
import java.util.Iterator;


public class XpathQueryPrac {
	public static void main(String[] args) {
		XpathQuery xpath = new XpathQuery();
		ArrayList<Integer> positions = new ArrayList<Integer>();
		positions = xpath.getPositions("tiny", "/dblp/*[author~\"Manola\"]/title");		
		System.exit(0);
	}
}
