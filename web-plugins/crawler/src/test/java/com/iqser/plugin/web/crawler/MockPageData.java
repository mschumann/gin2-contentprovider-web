package com.iqser.plugin.web.crawler;

import org.htmlparser.Node;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.NodeList;

import com.torunski.crawler.link.Link;
import com.torunski.crawler.parser.PageData;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author Jšrg Wurzer
 *
 */
public class MockPageData extends PageData {

	public MockPageData(Link link) {
		super(link);
	}

	@Override
	public Object getData() {
		Node node = new TextNode("Ist is a test page");
		return new NodeList(node);
	}

}
