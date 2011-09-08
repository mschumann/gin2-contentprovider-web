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
			link = StringEscapeUtils.unescapeHtml(link);
			links.add(link);
		}
						
		return links;
	}

}
