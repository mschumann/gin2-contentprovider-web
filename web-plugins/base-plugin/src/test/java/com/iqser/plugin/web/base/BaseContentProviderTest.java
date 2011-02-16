package com.iqser.plugin.web.base;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.config.Configuration;
import com.iqser.core.model.Content;
import com.iqser.plugin.web.test.MockAnalyzerTaskStarter;
import com.iqser.plugin.web.test.MockBaseContentProvider;
import com.iqser.plugin.web.test.MockRepository;
import com.iqser.plugin.web.test.TestServiceLocator;

import junit.framework.TestCase;

/**
 * A general test case for the Base Content Provider
 * 
 * @author Jšrg Wurzer
 *
 */
public class BaseContentProviderTest extends TestCase {

	/** The provider to test */
	private MockBaseContentProvider provider = null;
	
	/** The database connection to cache */
	private Connection conn = null;
	
	/** The Logger */
	private static Logger logger = Logger.getLogger( BaseContentProviderTest.class );
	

	protected void setUp() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
		
		logger.debug("setup() called");
		
		super.setUp();
		
		// Using an implementation of BaseContentProvider
		provider = new MockBaseContentProvider();
		
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
		initParams.setProperty("link-filter", "(.*\\.html$)||(.*\\.htm$)");
		
		provider.setInitParams(initParams);
		provider.setType("HTML Page");
		provider.setId("com.iqser.plugin.web.base");
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
			
			Statement stmt = conn.createStatement();
			
			// Insert new testdata to simulate a crawler job
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");
			
			stmt.executeUpdate("DELETE FROM documents");
			
			for (int i = 1; i < 6; i++) {
				Content c = new Content();
				c.setType(provider.getType());
				c.setContentUrl("http://www.some-server.com/page" + i + ".html");
				c.setProvider(provider.getId());
				rep.addContent(c);
			
				stmt.executeUpdate("INSERT INTO documents VALUES(" +
						"DEFAULT, '" + c.getContentUrl() + "', 123456789, " +
						"'" + c.getProvider() + "', " + System.currentTimeMillis() + ")");		
			}
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
			
			rs = stmt.executeQuery("SELECT COUNT(provider) FROM providers");
			assertTrue(rs.first());
			assertEquals(1, rs.getInt("Count(provider)"));
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
			assertEquals(5, rs.getInt("Count(url)"));
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
	}

	public void testDoHousekeeping() {
		logger.debug("tstDoHousekeeping() called");
		
		try {
			Statement stmt = conn.createStatement();;
			
			stmt.executeUpdate("DELETE FROM documents WHERE url='http://www.some-server.com/page5.html'");
			
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		} 

		provider.doHousekeeping();
		
		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM documents");
			assertTrue(rs.first());
			assertEquals(4, rs.getInt("Count(url)"));	
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	public void testGetContentUrls() {
		logger.debug("testGetContentUrls() called");
		
		Collection cl = provider.getContentUrls();
		assertEquals(5, cl.size());
		assertEquals("http://www.some-server.com/page1.html", cl.iterator().next());
	}

}
