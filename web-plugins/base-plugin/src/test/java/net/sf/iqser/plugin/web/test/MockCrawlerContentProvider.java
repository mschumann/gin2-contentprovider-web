package net.sf.iqser.plugin.web.test;

import java.io.InputStream;
import java.util.Collection;

import net.sf.iqser.plugin.web.base.CrawlerContentProvider;

import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author Joerg Wurzer
 *
 */
public class MockCrawlerContentProvider extends CrawlerContentProvider {

	@Override
	public Collection<String> getActions(Content arg0) {
		return null;
	}

	@Override
	public Content createContent(String arg0) {
		Content c = new Content();
		c.setContentUrl(arg0);
		c.setProvider(getName());
		c.setType("Web Page");
		return c;
	}

	@Override
	public Content createContent(InputStream arg0) {
		return null;
	}

	@Override
	public byte[] getBinaryData(Content arg0) {
		return null;
	}

	@Override
	public void performAction(String arg0, Collection<Parameter> arg1, Content arg2) { 
	}
}
