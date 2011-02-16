package com.iqser.plugin.web.test;

import com.iqser.core.analyzer.AnalyzerTaskStarter;
import com.iqser.core.analyzer.ContentAnalyzer;
import com.iqser.core.category.CategoryBuilder;
import com.iqser.core.category.CategoryManager;
import com.iqser.core.client.AdminFacade;
import com.iqser.core.client.ClientFacade;
import com.iqser.core.event.EventPublisher;
import com.iqser.core.index.Index;
import com.iqser.core.locator.ServiceLocator;
import com.iqser.core.plugin.ContentProvider;
import com.iqser.core.plugin.PluginManager;
import com.iqser.core.repository.Repository;
import com.iqser.core.security.SecurityManager;
import com.iqser.core.tracker.Tracker;

/**
 * Supports testing of classes of the iQser Web Content Provider Family.
 * 
 * @author Jšrg Wurzer
 *
 */
public class TestServiceLocator implements ServiceLocator {
	
	/** Repository mockup for testing */
	private Repository rep = null;
	
	/** AnalyzerTaskStarter mpckup for testing */
	private AnalyzerTaskStarter ats = null;

	@Override
	public AdminFacade getAdminFacade() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setAnalyzerTaskStarter(AnalyzerTaskStarter arg0) {
		ats = arg0;
	}

	@Override
	public AnalyzerTaskStarter getAnalyzerTaskStarter() {
		return ats;
	}

	@Override
	public CategoryBuilder getCategoryBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CategoryManager getCategoryManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientFacade getClientFacade() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentAnalyzer getContentAnalyzer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentProvider getContentProvider(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EventPublisher getEventPublisher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Index getIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PluginManager getPluginManager() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setRepository(Repository mockup) {
		rep = mockup;
	}

	@Override
	public Repository getRepository() {
		return rep;
	}

	@Override
	public SecurityManager getSecurityManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Tracker getTracker() {
		// TODO Auto-generated method stub
		return null;
	}

}
