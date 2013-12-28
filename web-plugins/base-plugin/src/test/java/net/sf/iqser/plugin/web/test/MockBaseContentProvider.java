package net.sf.iqser.plugin.web.test;

import java.io.InputStream;
import java.util.Collection;

import net.sf.iqser.plugin.web.base.BaseContentProvider;

import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author Joerg Wurzer
 *
 */
public class MockBaseContentProvider extends BaseContentProvider {


	@Override
	public Content createContent(String arg0) {
		Content c = new Content();
		c.setContentUrl(arg0);
		c.setProvider(getName());
		c.setType(getInitParams().getProperty("type", "Mock Content"));
		return c;
	}

	@Override
	public Content createContent(InputStream arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBinaryData(Content arg0) {
		// TODO Auto-generated method stub
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
	
}
