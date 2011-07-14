package net.sf.iqser.plugin.web.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

import com.torunski.crawler.MultiThreadedCrawler;
import com.torunski.crawler.filter.LinkFilterUtil;
import com.torunski.crawler.filter.RegularExpressionFilter;
import com.torunski.crawler.filter.ServerFilter;
import com.torunski.crawler.model.MaxDepthModel;
import com.torunski.crawler.parser.htmlparser.SimpleHtmlParser;

/**
 * A crawler manager of the iQser Web Content Provider Family.
 * He starts external crawler for registered Content Provider and 
 * stores the results in a database.
 * 
 * @author Joerg Wurzer
 *
 */
public class CrawlerManager {

	/** The instance of the singleton */
	private static CrawlerManager instance = null;
	
	/** The Logger */
	private static Logger logger = Logger.getLogger( CrawlerManager.class );
	
	/** The databse Connection */
	private Connection conn = null;
	private String dbProtocol = null;
	private String dbPath = null;
	private String dbUser = null;
	private String dbPassword = null;
	
	/** runnung crawler */
	private MultiThreadedCrawler crawler = null;
	
	private CrawlerManager() {
		logger.info("Instantiation performed");
	}
	
	public static CrawlerManager getInstance() {
		logger.debug("getInstance() called");
		
        if (instance == null) {
            instance = new CrawlerManager();
        }
        return instance;
    }
	
	public void setDatabaseConnection(String protocol, String database, String user, String password) {
		logger.debug("setDatabaseConnection() called for " + database);

		dbProtocol = protocol;
		dbPath = database;
		dbUser = user;
		dbPassword = password;
		
		try {
			conn = DriverManager.getConnection(
					dbProtocol + "//" + dbPath + "?user=" + dbUser + "&password=" + dbPassword);
			
			Statement stmt = conn.createStatement();
			
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");
		} catch (SQLException e) {
			logger.error("Couldn't access database or create table - " + e.getMessage());
		}
	} 
	
	public void startAllCrawler() {
		logger.info("startAllCrawler() called");
		
		try {
			Statement stmt1 = conn.createStatement();
			Statement stmt2 = conn.createStatement();
			ResultSet rs = stmt1.executeQuery("SELECT * FROM providers");
			
			while (rs.next()) {
				String provider = rs.getString("provider");
				String startserver = rs.getString("startserver");
				String startpath = rs.getString("startpath");
				String serverfilter = rs.getString("serverfilter"); 
				String pathfilter = rs.getString("pathfilter");
				String itemfilter = rs.getString("itemfilter");
				String linkfilter = rs.getString("linkfilter");
				int maxdepth = rs.getInt("maxdepth");
								
				crawler = new MultiThreadedCrawler(); 
				crawler.setModel(new MaxDepthModel(maxdepth));
				crawler.setParser(new SimpleHtmlParser());
				
				if (!serverfilter.equalsIgnoreCase("none"))
					crawler.setLinkFilter(LinkFilterUtil.and(
							new ServerFilter(serverfilter + pathfilter ),
							new RegularExpressionFilter(linkfilter ) ) );
				
				ParserEventListener listener = new ParserEventListener(itemfilter, provider);
				listener.setDatabaseConnection(dbProtocol, dbPath, dbUser, dbPassword);
				crawler.addParserListener(listener);
		
				stmt2.executeUpdate("UPDATE providers SET lastcrawlerstart=" + System.currentTimeMillis() +
						" WHERE provider='" + provider + "'");
				
				logger.debug("Crawler for provider " + provider + " will start");
				crawler.start(startserver, startpath);
			}
		} catch (SQLException e) {
			logger.error("Couldn't access database - " + e.getMessage());
		}
		
		crawler = null;
		
		logger.debug("startAllCrawler() finished");
	}
	
	public void stopAllCrawler() {
		logger.info("stopAllCrawler() caled");
		
		crawler = null;
		
		logger.debug("stopAllCrawler() finished");
	}
	
	public boolean areRunning() {
		if (crawler == null)
			return false;
		else
			return true;
	}
	
	public MultiThreadedCrawler getCrawler() {
		return crawler;
	}
	
}
