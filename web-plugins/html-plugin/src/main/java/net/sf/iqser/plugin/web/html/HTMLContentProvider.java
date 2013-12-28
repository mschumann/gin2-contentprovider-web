package net.sf.iqser.plugin.web.html;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.sf.iqser.plugin.web.base.CrawlerContentProvider;
import net.sf.iqser.plugin.web.html.filters.RegExHasAttributeFilter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.RegexFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Source;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;

/**
 * HTML Content Provider of the iQser Web Content Provider Family
 * 
 * @author Joerg Wurzer
 * 
 */
public class HTMLContentProvider extends CrawlerContentProvider {
	
	/** Logger */
    private static Logger logger = Logger.getLogger( HTMLContentProvider.class );

	@Override
	public Content createContent(String url) {
		logger.debug("createContent(String) called for " + url);
		
		Content c = new Content();

		// Setting technical meta data
		c.setProvider(getName());
		c.setContentUrl(url);
		
		// Setting content type
		c.setType(getInitParams().getProperty("type", "Web Page"));
		
		try {
			Parser parser = new Parser(url);;
			parser.setEncoding(getInitParams().getProperty("charset", Page.DEFAULT_CHARSET));
			
			// Setting the modification date
			c.setModificationDate(parser.getConnection().getLastModified());
			
			NodeFilter itemFilter = createItemFilter();
			NodeList nodes = parser.parse(itemFilter);
			Node item = nodes.elementAt(0);
			
			// Setting the attributes including fulltext
			if (item != null) {
				createContentAttributes(c, item);
				
			} else {
				NodeList list = parser.parse(null);
				Node node = list.elementAt(0);
				
				if (node != null) {
					c.setFulltext(StringEscapeUtils.unescapeHtml(node.toPlainTextString()));
					
					NodeList title = node.getChildren().extractAllNodesThatMatch(
							new NodeClassFilter(TitleTag.class), true);
					
					if (title.size() > 0) {
						c.addAttribute(new Attribute(
								"Title", ((TitleTag)title.elementAt(0)).getTitle(), 
								Attribute.ATTRIBUTE_TYPE_TEXT, true));
					}
				}
			}
			
		} catch (ParserException e) {
			logger.error("Couldn't parse source - " + e.getMessage());
		} 
		
		return c;
	}

	@Override
	public Content createContent(InputStream in) {
		logger.debug("createContent(InputStream) called");
		
		Content c = new Content();

		// Setting technical metadata
		c.setProvider(getName());
		
		// Setting content type
		c.setType(getInitParams().getProperty("type", "Web Page"));
		
		try {
			Page page = new Page(in, getInitParams().getProperty("charset", Page.DEFAULT_CHARSET));

			Parser parser = new Parser(new Lexer(page));
			
			NodeFilter itemFilter = createItemFilter();
			NodeList nodes = parser.parse(itemFilter);
			Node item = nodes.elementAt(0);
			
			// Setting the attributes inlcuding fulltext
			createContentAttributes(c, item);

		} catch (UnsupportedEncodingException ce) {
			logger.error("Unsupported charset - " + ce.getMessage());
		} catch (ParserException pe) {
			logger.error("Couldn't parse source with default charset - " + pe.getMessage());
		}
		
		return c;
	}

	@Override
	public byte[] getBinaryData(Content c) {
		logger.debug("getBinaryData(Content) called for " + c.getContentUrl());
		
		try {
			URL url = new URL(c.getContentUrl());
			Page page = new Page(url.openConnection());
			Source source = page.getSource();
			boolean reading = true;
			
			while (reading) {
				reading = (source.read() != Source.EOF);
			}
			
			return page.getText().getBytes();
		} catch (MalformedURLException e) {
			logger.error("Marformed url - " + e.getMessage());
		} catch (IOException e) {
			logger.error("Couldm't read source - " + e.getMessage());
		} catch (ParserException e) {
			logger.error("Couldm't parse source - " + e.getMessage());
		}
		
		return null;
	}

	@Override
	public Collection<String> getActions(Content arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void performAction(String arg0, Collection<Parameter> arg1,
			Content arg2) {
		// TODO Auto-generated method stub
		
	}

	private NodeFilter createItemFilter() {
		String[] fParam = getInitParams().getProperty("item-node-filter", "html,*,*,*").split(",", 4);
		Collection<NodeFilter> fCol = new ArrayList<NodeFilter>();
		
		if (!fParam[0].equals("*")) {
			fCol.add(new TagNameFilter(fParam[0]));
		} 

		if (!fParam[1].equals("*") && fParam[2].equals("*")) {
			fCol.add(new RegExHasAttributeFilter(fParam[1]));
		} else if (!fParam[1].equals("*") && !fParam[2].equals("*")) {
			fCol.add(new RegExHasAttributeFilter(fParam[1], fParam[2]));
		} 
		
		if (!fParam[3].equals("*")) {
			fCol.add(new RegexFilter(fParam[3]));
		}
		
		if (fCol.size() > 1) {
			Iterator<NodeFilter> iter = fCol.iterator();
			NodeFilter[] fArray = new NodeFilter[fCol.size()];
			int counter = 0;
			
			while (iter.hasNext())
				fArray[counter++] = iter.next();

			return new AndFilter(fArray);
		}
		
		return fCol.iterator().next();
	}
	
	private NodeFilter createAttributeFilter() {
		String[] fItems = getInitParams().getProperty(
				"attribute-node-filter", "title,*,*,*;meta,*,*,*").split(";");
		Collection<NodeFilter> fOrCol = new ArrayList<NodeFilter>();
		
		for (int i = 0; i < fItems.length; i++) { 
			
			String[] fParam = fItems[i].split(",", 4);
			Collection<NodeFilter> fAndCol = new ArrayList<NodeFilter>();
		
			if (!fParam[0].equals("*")) {
				fAndCol.add(new TagNameFilter(fParam[0]));
			} 

			if (!fParam[1].equals("*") && fParam[2].equals("*")) {
				fAndCol.add(new RegExHasAttributeFilter(fParam[1]));
			} else if (!fParam[1].equals("*") && !fParam[2].equals("*")) {
				fAndCol.add(new RegExHasAttributeFilter(fParam[1], fParam[2]));
			} 
		
			if (!fParam[3].equals("*")) {
				fAndCol.add(new RegexFilter(fParam[3]));
			}
			
		
			if (fAndCol.size() > 1) {
				Iterator<NodeFilter> iter = fAndCol.iterator();
				NodeFilter[] fArray = new NodeFilter[fAndCol.size()];
				int counter = 0;
					
				while (iter.hasNext())
					fArray[counter++] = iter.next();

				fOrCol.add(new AndFilter(fArray));
			} else {
				fOrCol.add(fAndCol.iterator().next());
			}
		}
			
		if (fOrCol.size() > 1) {
			Iterator<NodeFilter> iter = fOrCol.iterator();
			NodeFilter[] fArray = new NodeFilter[fOrCol.size()];
			int counter = 0;
				
			while (iter.hasNext())
				fArray[counter++] = iter.next();

			return new OrFilter(fArray);
		}
						
		return fOrCol.iterator().next();
	}
	
	private void createContentAttributes(Content c, Node node) {
		NodeFilter attributeFilter = createAttributeFilter();
		NodeList attrNodes = node.getChildren().extractAllNodesThatMatch(attributeFilter, true);
		SimpleNodeIterator attrIter = attrNodes.elements();
		
		// Setting attributes of the content
		while (attrIter.hasMoreNodes()) {
			Node attrNode = attrIter.nextNode();
			String name = attrNode.toHtml();
			String value;
			
			// Recognize meta tags as attributes
			if (attrNode.toHtml().startsWith("<meta") && (((MetaTag)attrNode).getMetaTagName() != null)) {
				name = getInitParams().getProperty(
						((MetaTag)attrNode).getMetaTagName(), ((MetaTag)attrNode).getMetaTagName());
				value = StringEscapeUtils.unescapeHtml(((MetaTag)attrNode).getMetaContent());
			} else {
				if (((TagNode)attrNode).getAttribute("CLASS") != null)
					name = getInitParams().getProperty(
							((TagNode)attrNode).getAttribute("CLASS"), ((TagNode)attrNode).getTagName());
				else
					name = getInitParams().getProperty(
							((TagNode)attrNode).getTagName(), ((TagNode)attrNode).getTagName());
				value = new String(StringEscapeUtils.unescapeHtml(attrNode.toPlainTextString()).trim());
			}
						
			// To avoid empty attributes
			if (!value.equals(" ") && (value.length() > 1)) {
				
				// Considering key-value pairs
				if (value.indexOf(":") > -1) {
					String[] keyValuePair = value.split(":", 2);
					name = getInitParams().getProperty(keyValuePair[0].trim(), keyValuePair[0].trim());
					value = getInitParams().getProperty(keyValuePair[1].trim(), keyValuePair[1].trim());
				}
									
				int type = Attribute.ATTRIBUTE_TYPE_TEXT;
				boolean flag = true;
				
				// Considering key-attribute-definition if defined
				String keyAttr = getInitParams().getProperty("key-attributes");
				if (keyAttr != null && (keyAttr.indexOf("[" + name + "]") == -1))
					flag = false;
					
				if (!value.isEmpty()) {
					name = transformAttributeName(name);
					c.addAttribute(new Attribute(name, value.trim(), type, flag));
					logger.debug("Add attribute " + name + ": " + value.trim());
				}
			}
		}
		
		// Setting full text for for indexing
		node.getChildren().keepAllNodesThatMatch(new NotFilter(new TagNameFilter("SCRIPT")), true );
		node.getChildren().keepAllNodesThatMatch(new NotFilter(new TagNameFilter("SELECT")), true );
		node.getChildren().keepAllNodesThatMatch(new NotFilter(new TagNameFilter("FORM")), true );
		
		c.setFulltext(StringEscapeUtils.unescapeHtml(node.toPlainTextString()));
	}
	
	private String transformAttributeName(String name) {
		name = name.replaceAll("ö|Ö", "oe");
		name = name.replaceAll("ä|Ä", "ae");
		name = name.replaceAll("ü|Ü", "ue");
		name = name.replaceAll("ß", "ss");
		name = name.replaceAll("![A-Za-z]", "_");
		
		return name.toUpperCase();
	}

}
