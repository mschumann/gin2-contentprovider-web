package net.sf.iqser.plugin.web.base;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;

import net.sf.iqser.plugin.web.base.CrawlerContentProvider;
import net.sf.iqser.plugin.web.test.MockContentProviderFacade;
import net.sf.iqser.plugin.web.test.MockCrawlerContentProvider;
import net.sf.iqser.plugin.web.test.TestServiceLocator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.config.Configuration;
import com.iqser.core.exception.IQserException;
import com.iqser.core.model.Content;
import com.iqser.core.plugin.ContentProviderFacade;

/**
 * A general test case for the Crawler Content Provider
 * 
 * @author Joerg Wurzer
 *
 */
public class CrawlerContentProviderTest extends TestCase {
	
	/** The provider to test */
	private CrawlerContentProvider provider = null;
	
	/** The database connection to cache */
	private Connection conn = null;
	
	/** The Logger */
	private static Logger logger = Logger.getLogger( CrawlerContentProviderTest.class );
	

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
		initParams.setProperty("start-server", "http://www.designerfashion.de");
		initParams.setProperty("start-path", "/Seiten/index_svg.html");
		initParams.setProperty("server-filter", "http://www.designerfashion.de");
		initParams.setProperty("path-filter", "/Seiten/");
		initParams.setProperty("maxdepth-filter", "6");
		initParams.setProperty("item-filter", "(.*\\.html$)||(.*\\.htm$)");
		
		provider.setInitParams(initParams);
		provider.setType("HTML Page");
		provider.setId("net.sf.iqser.plugin.web.crawler");
		provider.init();
				
		Configuration.configure(new File(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/iqser-config.xml"));
		
		TestServiceLocator sl = (TestServiceLocator)Configuration.getConfiguration().getServiceLocator();
		sl.setContentProviderFacade(new MockContentProviderFacade());
		
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
		
		ContentProviderFacade cpf = Configuration.getConfiguration().getServiceLocator().getContentProviderFacade();

		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT url FROM documents");
			
			while (rs.next()) {
				Content c = new Content();
				c.setContentUrl(rs.getString("url"));
				c.setProvider(provider.getId());
				c.setType(provider.getType());
				cpf.addContent(c);
			}
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		} catch (IQserException e) {
			fail("Could not add content to repository -" + e.getLocalizedMessage());
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
			assertEquals(27, rs.getInt("Count(url)"));	
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
