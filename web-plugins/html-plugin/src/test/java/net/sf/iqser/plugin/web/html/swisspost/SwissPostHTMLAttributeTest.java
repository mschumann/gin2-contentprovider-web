package net.sf.iqser.plugin.web.html.swisspost;

import java.util.Properties;

import junit.framework.TestCase;
import net.sf.iqser.plugin.web.html.HTMLContentProvider;

import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.model.Content;

public class SwissPostHTMLAttributeTest extends TestCase {
	
	/** Content provider to test */
	private HTMLContentProvider provider = null;

	protected void setUp() throws Exception {
		super.setUp();

		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/html-plugin/src/test/res/log4j.properties");
		
		Properties initParams = new Properties();
		initParams.setProperty("database", System.getProperty("database","localhost/crawler"));
		initParams.setProperty("username", System.getProperty("username", "root"));
		initParams.setProperty("password", System.getProperty("password", "master"));

		initParams.setProperty("charset", "UTF-8");
		
		overwriteParams(initParams);

		provider = new HTMLContentProvider();
		provider.setId("com.iqser.plugin.web.swisspost");
		provider.setType("Vendor");
		provider.setInitParams(initParams);
		provider.init();
	}

	protected void tearDown() throws Exception {
		provider.destroy();

		super.tearDown();
	}

	protected void overwriteParams(Properties initParams){
		initParams.setProperty("item-node-filter",	"*,id,(webInnerContentSmall|spalteContent|mainContentContainer|contentContainer),*");
		initParams.setProperty("attribute-node-filter", "h1,*,*,*;h2,*,*,*;div,class,paragraphTitle,*;");
		initParams.setProperty("key-attributes", "[TITLE] [SUBTITLE] [ABSTRACT]");
		initParams.setProperty("H1", "TITLE");
		initParams.setProperty("webTitle", "TITLE");
		initParams.setProperty("webTitle ", "TITLE");
		initParams.setProperty("H2", "SUBTITLE");
		initParams.setProperty("webTitleH2", "SUBTITLE");
		initParams.setProperty("paragraphTitle", "SUBTITLE");
		initParams.setProperty("webLead", "ABSTRACT");
	}
	
	protected void getContent(String url) {		
		Content c = provider.getContent(url);
		assertNotNull(c);
		assertEquals("Vendor", c.getType());
		assertEquals("com.iqser.plugin.web.swisspost", c.getProvider());		

		assertNotNull("TITLE", c.getAttributeByName("TITLE"));
				
		assertFalse(c.getFulltext().contains("webInnerContentSmall"));
		assertFalse(c.getFulltext().contains("spalteContent"));
		assertFalse(c.getFulltext().contains("mainContentContainer"));
		assertFalse(c.getFulltext().contains("contentContainer"));
	}
	
	public void testGetContentHomePage() {
		String url = "http://www.admin.ch/index.html?lang=en";
		getContent(url);
	}
	
	public void testGetContentDDPS() {
		String url = "http://www.vbs.admin.ch/internet/vbs/en/home/departement/organisation.html";
		getContent(url);
	}
	
	public void testGetContentDETEC() {
		String url = "http://www.uvek.admin.ch/themen/01268/01274/index.html?lang=en";
		getContent(url);
	}
	
	public void testGetContentFDEA() {
		String url = "http://www.evd.admin.ch/aktuell/00120/index.html?lang=en&msg-id=41007";
		getContent(url);
	}
	
	public void testGetContentFDF() {
		String url = "http://www.efd.admin.ch/aktuell/medieninformation/00462/index.html?lang=en&msg-id=41020";
		getContent(url);
	}
	
	public void testGetContentFDFA() {
		String url; 
		
		url = "http://www.eda.admin.ch/eda/en/home/dfa/policy.html";
		getContent(url);
		
		url = "http://www.eda.admin.ch/eda/en/home/topics.html";
		getContent(url);
		
		url = "http://www.eda.admin.ch/eda/en/home/reps.html";
		getContent(url);

		url = "http://www.eda.admin.ch/eda/en/home/doc.html";
		getContent(url);

		url = "http://www.eda.admin.ch/eda/en/home/serv.html";
		getContent(url);

		url = "http://www.eda.admin.ch/eda/en/home/dfa.html";
		getContent(url);
		
	}
	
	public void testGetContentFDHA() {
		String url = "http://www.edi.admin.ch/org/index.html?lang=en";
		getContent(url);
	}
	
	public void testGetContentFDJP() {
		String url = "http://www.ejpd.admin.ch/content/ejpd/en/home/themen/wirtschaft.html";
		getContent(url);
	}
	
	public void testGetContentFHC() {
		String url = "http://www.bk.admin.ch/themen/sprachen/00083/index.html?lang=en";
		getContent(url);
	}
	
}
