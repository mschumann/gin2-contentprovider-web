package net.sf.iqser.plugin.web.html.filters;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class RegExHasAttrbuteFilterTest extends TestCase {
	
	
	public void test1() throws ParserException{		
		String url;
		
		String attribute = "id";
		String value = "webInnerContentSmall|spalteContent|webInnerContentBroad";
		NodeFilter filter = new RegExHasAttributeFilter(attribute, value);
		
		url = "http://www.admin.ch/portal/index.html?lang=en";
		parse(url, filter);
		
		url = "http://www.eda.admin.ch/eda/en/home/dfa/policy.html";
		parse(url, filter);
		
		url = "http://www.edi.admin.ch/dokumentation/00334/index.html?lang=en";
		parse(url, filter);
	}
	
	public void test2() throws ParserException{
		
		String url;
		
		String attribute = "id";
		String value = "webInnerContentSmall";
		NodeFilter filter = new RegExHasAttributeFilter(attribute, value);
		
		url = "http://www.admin.ch/portal/index.html?lang=en";
		parse(url, filter);		

	}

	public void test3() throws ParserException{
		
		String url;
		
		String attribute = "id";
		String value = "spalteContent";
		NodeFilter filter = new RegExHasAttributeFilter(attribute, value);
		
		url = "http://www.eda.admin.ch/eda/en/home/dfa/policy.html";
		parse(url, filter);		

	}
	
	public void testFailure() throws ParserException{
		
		String url;
		
		String attribute = "id";
		String value = "webInnerContentSmall";
		NodeFilter filter = new RegExHasAttributeFilter(attribute, value);
		
		try{
			url = "http://www.eda.admin.ch/eda/en/home/dfa/policy.html";
			parse(url, filter);
			fail();
		}catch (AssertionFailedError afe) {
		}
	}
	
	protected void parse(String url, NodeFilter filter) throws ParserException{
				
		Parser parser = new Parser(url);
		parser.setEncoding(Page.DEFAULT_CHARSET);
		
		NodeList nodes = parser.parse(filter);
		Node item = nodes.elementAt(0);
		
		assertNotNull(" Test failed for url="+url, item);
	}
	

}
