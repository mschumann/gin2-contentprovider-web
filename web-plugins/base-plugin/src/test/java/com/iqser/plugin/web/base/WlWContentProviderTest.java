package com.iqser.plugin.web.base;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.config.Configuration;
import com.iqser.core.exception.IQserTechnicalException;
import com.iqser.core.model.Content;
import com.iqser.plugin.web.test.MockAnalyzerTaskStarter;
import com.iqser.plugin.web.test.MockCrawlerContentProvider;
import com.iqser.plugin.web.test.MockRepository;
import com.iqser.plugin.web.test.TestServiceLocator;

import junit.framework.TestCase;

/**
 * A specific test case for the Crawler Content Provider and a German vendor search engine.
 * 
 * @author J�rg Wurzer
 *
 */
public class WlWContentProviderTest extends TestCase {

	/** The provider to test */
	MockCrawlerContentProvider provider = null;
	
	/** The database connection to cache */
	Connection conn = null;
	
	/** Logger */
    private static Logger logger = Logger.getLogger( WlWContentProviderTest.class );

	protected void setUp() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
		
		logger.debug("setup() called");
		
		super.setUp();
		
		// Using an implementation of BaseContentProvider
		provider = new MockCrawlerContentProvider();
		
		Properties initParams = new Properties();
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		initParams.setProperty("start-server", "http://www.wlw.de");
//		initParams.setProperty("start-path", "/sse/MainServlet?sprache=de&land=DE&anzeige=produkt&suchbegriff=Maschinen");
		initParams.setProperty("start-path", "/sse/MainServlet?anzeige=kurzliste&land=DE&sprache=de&klobjid=85340&suchbegriff=Maschinen");
		initParams.setProperty("server-filter", "http://www.wlw.de");
		initParams.setProperty("path-filter", "/sse/");
		initParams.setProperty("maxdepth-filter", "1");
		initParams.setProperty("link-filter", 
				"(\\S*MainServlet\\Sanzeige=kurzliste\\S*$)||(\\S*MainServlet\\Sanzeige=vollanzeige\\S*$)");
		initParams.setProperty("item-filter", 
				"\\S*MainServlet\\Sanzeige=vollanzeige\\S*$");
		
		provider.setInitParams(initParams);
		provider.setType("Lieferant");
		provider.setId("com.iqser.plugin.web.wlw");
		provider.init();
				
		Configuration.configure(new File(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/iqser-config.xml"));
		
		TestServiceLocator sl = (TestServiceLocator)Configuration.getConfiguration().getServiceLocator();
		MockRepository rep = new MockRepository();
		rep.init();
		
		sl.setRepository(rep);
		sl.setAnalyzerTaskStarter(new MockAnalyzerTaskStarter());
		
		try {
			conn = DriverManager.getConnection(
					provider.getInitParams().getProperty("protocol", "jdbc:mysql:") + 
					"//" + provider.getInitParams().getProperty("database") + 
					"?user=" + provider.getInitParams().getProperty("username") +
					"&password=" + provider.getInitParams().getProperty("password"));
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	protected void tearDown() throws Exception {
		logger.debug("tearDown() called");
		
		provider.destroy();
		super.tearDown();
	}

	public void testInit() {
		logger.debug("testInit() called");
		
		try {
			Statement stmt = conn.createStatement();;
			stmt.execute("DELETE FROM documents");
			ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM documents");
			assertTrue(rs.first());
			assertEquals(0, rs.getInt("Count(url)"));
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	public void testDoSynchonization() {
		logger.debug("testDoSynchronization() called");
		
		provider.doSynchonization();
		
		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM documents");
			assertTrue(rs.first());
			assertTrue(rs.getInt("Count(url)") > 0);
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
	}

	public void testDoHousekeeping() {
		logger.debug("tstDoHousekeeping() called");
		
		MockRepository rep = (MockRepository)Configuration.getConfiguration().getServiceLocator().getRepository();

		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT url FROM documents");
			
			while (rs.next()) {
				Content c = new Content();
				c.setContentUrl(rs.getString("url"));
				c.setProvider(provider.getId());
				c.setType(provider.getType());
				rep.addContent(c);
			}
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		} catch (IQserTechnicalException iqe) {
			fail("Could not add content to repository - " + iqe.getMessage());
		}

		Properties initParams = provider.getInitParams();
		initParams.setProperty("start-path", "/Seiten/impressum.html");
		provider.setInitParams(initParams);
		
		provider.doSynchonization();
		provider.doHousekeeping();
		
		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM documents");
			assertTrue(rs.first());
			assertTrue(rs.getInt("Count(url)") > 0);	
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	public void testGetContentUrls() {
		logger.debug("testGetContentUrls() called");
		
		// Not yet implemented in the tested class.
	}

	public void testParse() {
		logger.debug("testParse() called");
		
		// Not yet implemented
	}

}
