package net.sf.iqser.plugin.web.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.torunski.crawler.filter.ILinkFilter;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.parser.IParser;
import com.torunski.crawler.parser.PageData;
import com.torunski.crawler.util.StringUtils;

/**
 * A simple parser based on SourceForge HTMLParser to show how to implement a own parser.
 * This parser downloads the content and parses only the links.
 * 
 * http://htmlparser.sourceforge.net/
 * 
 * @author Lars Torunski
 * @version $Revision: 1.9 $
 */
public class ExtendedHtmlParser implements IParser {

    private static transient final Log log = LogFactory.getLog(ExtendedHtmlParser.class);
    
    private NodeFilter nodeFilter;
    
    private String encoding;

	/**
	 * @return the current NodeFilter for this parser.
	 */
	public NodeFilter getNodeFilter() {
		return nodeFilter;
	}

	/**
	 * Sets the NodeFilter for this parser, by default a LinkTag NodeFilter is used for the page data.
	 * @param nodeFilter set the NodeFilter for this parser.
	 */
	public void setNodeFilter(NodeFilter nodeFilter) {
		this.nodeFilter = nodeFilter;
	}
    
	/**
     * @see com.torunski.crawler.parser.IParser#load(com.torunski.crawler.link.Link)
     */
    public PageData load(Link link) {
        String uri = link.getURI();
        log.info("download: " + uri);
        try {
            Parser parser = new Parser(uri);
            
            if (encoding != null) {
            	parser.setEncoding(encoding);
            }
            
            NodeFilter filter = nodeFilter == null ? new NodeClassFilter(LinkTag.class) : nodeFilter;
            NodeList list = parser.extractAllNodesThatMatch(filter);
            
            return new PageDataHtmlParser(link, list);
        } catch (ParserException e) {
            log.warn("Failed to load " + uri + " - " + e.getLocalizedMessage());
            return new PageDataHtmlParser(link, PageData.ERROR);
        }
    }

    /**
     * @see com.torunski.crawler.parser.IParser#parse(com.torunski.crawler.parser.PageData, com.torunski.crawler.filter.ILinkFilter)
     */
    public Collection<String> parse(PageData pageData, ILinkFilter linkFilter) {
    	if (!(pageData instanceof PageDataHtmlParser)) {
    		log.warn("Type mismatch in " + this.getClass().getName());
    		return Collections.emptySet();
    	}
    	
        Collection<String> links = new HashSet<String>(); // use HashSet to avoid duplicates

        NodeList list = (NodeList) pageData.getData();
        for (int i = 0; i < list.size(); i++) {
            String link = getLink((Node) list.elementAt(i));
            // if no filter is set or a set filter accepts the link, then add it to the list
            if (StringUtils.hasLength(link) && ((linkFilter == null) || (linkFilter.accept(pageData.getLink().getURI(), link)))) {
            	
            	// Workaround for wrong parsed links
            	if (link.contains("&amp;")) {
    				link = link.replaceAll("&amp;", "&");
    			}
            	
            	links.add(link);
            } 
        }
        
        return links;
    }

	/**
	 * @param node the node of the link.
	 * @return the extracted and purged link of the node
	 */
	public static String getLink(Node node) {
    	if (LinkTag.class.isAssignableFrom(node.getClass())) {
    		String link = ((LinkTag) node).extractLink();
    		int k = link.indexOf('#');
    		if (k >= 0) {
    			link = link.substring(0, k);
    		}
    		while (link.endsWith("/")) {
    			link = link.substring(0, link.length()-1);
    		}
    		return link;
    	}
    	return null;
	}
    
    // --- PageData implementation ---

    private static class PageDataHtmlParser extends PageData {

        /** the data of the page */
        private NodeList data;
        
        /**
         * @param uri the uri of the data
         * @param data the data of the uri
         */
        public PageDataHtmlParser(Link link, NodeList data) {
            super(link, PageData.OK);
            this.data = data;
        }

        /**
         * @param uri the uri of the data
         * @param status the status
         */
        public PageDataHtmlParser(Link link, int status) {
            super(link, status);
        }

        /**
         * @see com.torunski.crawler.parser.PageData#getData()
         */
        public Object getData() {
            return data;
        }
    }
    
    public void setEncoding(String pageEncoding) {
    	this.encoding = pageEncoding;
    }
    
    public String getEncoding() {
    	return encoding;
    }

}
