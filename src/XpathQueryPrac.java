import java.util.ArrayList;
import java.util.Iterator;


public class XpathQueryPrac {
	public static void main(String[] args) {
		XpathQuery xpath = new XpathQuery();
		ArrayList<Integer> positions = new ArrayList<Integer>();
		positions = xpath.getPositions("tiny", "/dblp/*[author~\"Manola\"][title~\"Journal\"]/title");
		Iterator<Integer> iterator = positions.iterator();
		Algo algo = new Algo();
		while(iterator.hasNext()) {
			algo.search("tiny.bwt", "[" + iterator.next() + "]");
		}
		
		System.exit(0);
	}
}
