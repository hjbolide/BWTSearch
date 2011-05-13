import java.io.Reader;
import java.io.Writer;

/**
 * BWTSearch stub file.  This class has to be filled in with your code.
 */
public class BWTSearch {
    /**
     * Public constructor for BWTSearch.
     * Any initialization that you need to do in preparation
     * for the search should be done here.
     *
     *
     * @param top a Reader for the topology file
     *
     * @param map a Reader for the mappings file
     *
     * @param bwt a Reader for the BWT file.
     *
     */
    public BWTSearch(Reader top, Reader map, Reader bwt) {
        // Your code here.
    	
    }
    
    /**
     * XPath search method. Writes out uncompressed XML
     * results to a Writer.
     *
     *
     * @param xpath the XPath to be evaluated.
     *
     * @param output the Writer to write out the XPath XML results to.
     *
     */
    public void search(String xpath, Writer output) {
        // Your code here.
    }
    
    /**
     * XPath search method.  Writes out compressed XML
     * results to Writers.
     *
     *
     * @param xpath the XPath to be evaluated.
     *
     * @param top the Writer to write out the XPath results topology to.
     * Uses the mappings data provided in the constructor.  The 
     * format of the XPath results topology is the same as the topology
     * data provided in the constructor.
     *
     * @param bwt the Writer to write out the XPath results BWT compressed 
     * text to.
     *
     */
    public void search(String xpath, Writer top, Writer bwt) {
        // Your code here.
    }
}