package net.sf.iqser.plugin.web.test;

import java.io.InputStream;
import java.util.Collection;

import net.sf.iqser.plugin.web.base.BaseContentProvider;

import com.iqser.core.event.Event;
import com.iqser.core.model.Content;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author Joerg Wurzer
 *
 */
public class MockBaseContentProvider extends BaseContentProvider {

	/** Serial ID */
	private static final long serialVersionUID = -1525130562258043583L;

	@Override
	public Collection getActions(Content arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Content getContent(String arg0) {
		Content c = new Content();
		c.setContentUrl(arg0);
		c.setProvider(getId());
		c.setType(getType());
		return c;
	}

	@Override
	public Content getContent(InputStream arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onChangeEvent(Event arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void performAction(String arg0, Content arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public byte[] getBinaryData(Content arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
