package net.sf.iqser.plugin.web.base.swisspost;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import junit.framework.TestCase;
import net.sf.iqser.plugin.web.base.SwissPostCrawlerContentProviderTest;
import net.sf.iqser.plugin.web.test.MockContentProviderFacade;
import net.sf.iqser.plugin.web.test.MockCrawlerContentProvider;
import net.sf.iqser.plugin.web.test.TestServiceLocator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.config.Configuration;

public class SwissPostCrawlerBase extends TestCase {

	/** The provider to test */
	MockCrawlerContentProvider provider = null;
	
	/** The database connection to cache */
	Connection conn = null;
	
	/** Logger */
    private static Logger logger = Logger.getLogger( SwissPostCrawlerContentProviderTest.class );

    protected void updateInitParams(Properties initParams){    	
		initParams.setProperty("maxdepth-filter", "1");
    }
    
	protected void setUp() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
		
		logger.debug("setup() called");
		
		super.setUp();
		
		// Using an implementation of BaseContentProvider
		provider = new MockCrawlerContentProvider();
		
		Properties initParams = new Properties();
		initParams.setProperty("database", System.getProperty("database","localhost/crawler"));
		initParams.setProperty("username", System.getProperty("username", "root"));
		initParams.setProperty("password", System.getProperty("password", "master"));
				
		updateInitParams(initParams);
		
		provider.setInitParams(initParams);
		provider.setType("News");
		provider.setId("ch.admin.news");
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
			
			Statement stmt = conn.createStatement();;
			stmt.execute("DELETE FROM documents");			
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	protected void tearDown() throws Exception {
		logger.debug("tearDown() called");
		
		provider.destroy();
		super.tearDown();
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
}
