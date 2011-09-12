package net.sf.iqser.plugin.web.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

import com.torunski.crawler.filter.ILinkFilter;
import com.torunski.crawler.parser.PageData;
import com.torunski.crawler.parser.htmlparser.SimpleHtmlParser;

/**
 * Extend the SimpleHtmlParser by unescaping HTML links. 
 *
 */
public class ExtendedHtmlParser extends SimpleHtmlParser{
	 
	/**
	 * Overwrite the default implementation and unescapes HTML links.
	 * 
	 * @param pageData
	 * @param linkFilter
	 * @return a collection of links
	 */
	public Collection parse(PageData pageData, ILinkFilter linkFilter) {
		Collection<String> oldLinks = super.parse(pageData, linkFilter);
		
		Set<String> links = new HashSet<String>();
		
		for ( String link : oldLinks) {			
			String newLink = StringEscapeUtils.unescapeHtml(link);
			/**
			 * Workaround:  StringEscapeUtils.unescapeHtml does not handle well the following link
			 * http://www.bk.admin.ch/aktuell/media/index.html?lang=en&org-nr=101&kind=M&type=A&amp;flexid=3_6&amp;start_index=3&amp;end_index=6
			 * The result is 
			 * http://www.bk.admin.ch/aktuell/media/index.html?lang=en&org-nr=101&kind=M&type=A&amp;flexid=3_6&start_index=3&end_index=6		
			 * which contains &amp;
			 */
			if (newLink.contains("&amp;")){
				newLink = newLink.replaceAll("&amp;", "&");
			}
			links.add(newLink);
		}
						
		return links;
	}

}
