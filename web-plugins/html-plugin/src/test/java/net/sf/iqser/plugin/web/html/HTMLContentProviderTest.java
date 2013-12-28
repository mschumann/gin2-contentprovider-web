package net.sf.iqser.plugin.web.html;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import net.sf.iqser.plugin.web.html.HTMLContentProvider;

import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.model.Content;
import com.iqser.gin.developer.test.plugin.provider.ContentProviderTestCase;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * A general test case for HTML Content Provider
 * 
 * @author Jörg Wurzer
 *
 */
public class HTMLContentProviderTest extends ContentProviderTestCase {

	@Test
	public void testCreateContentString() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/html-plugin/src/test/res/log4j.properties");
		
		Properties initParams = new Properties();
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		initParams.setProperty("item-node-filter", "TABLE,*,*,*");
		initParams.setProperty("attribute-node-filter", "LI,*,*,*;P,*,*,*");  
		initParams.setProperty("P", "Name");
		initParams.setProperty("LI", "Description");
		
		HTMLContentProvider provider = new HTMLContentProvider();
		provider.setName("net.sf.iqser.plugin.web.html");
		provider.setInitParams(initParams);
		
		// initialize the test
		prepare(); 
		provider.init();		

		// execute the method(s) under test
		Content c = provider.createContent("http://www.designerfashion.de/Seiten/r2-Felljacke.html");
		assertNotNull(c);
		assertEquals("Web Page", c.getType());
		assertEquals("net.sf.iqser.plugin.web.html", c.getProvider());
		assertTrue(c.getContentUrl().endsWith(".html") || c.getContentUrl().endsWith(".htm"));
		assertEquals(6, c.getAttributes().size());
		assertEquals("NAME", c.getAttributes().iterator().next().getName());
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 

	}

	@Test
	public void testCreateContentInputStream() throws Exception{
		Properties initParams = new Properties();
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		initParams.setProperty("item-node-filter", "TABLE,*,*,*");
		initParams.setProperty("attribute-node-filter", "LI,*,*,*;P,*,*,*");  
		initParams.setProperty("P", "Name");
		initParams.setProperty("LI", "Description");
		
		HTMLContentProvider provider = new HTMLContentProvider();
		provider.setName("net.sf.iqser.plugin.web.html");
		provider.setInitParams(initParams);
		
		// initialize the test
		prepare(); 
		provider.init();
		
		// execute test
		try {
			URL url = new URL("http://www.designerfashion.de/Seiten/r2-Felljacke.html");
			InputStream in = url.openStream();
			Content c = provider.createContent(in);
			assertNotNull(c);
			assertEquals("Web Page", c.getType());
			assertEquals("net.sf.iqser.plugin.web.html", c.getProvider());
			assertEquals(6, c.getAttributes().size());
			assertEquals("NAME", c.getAttributes().iterator().next().getName());
		} catch (MalformedURLException e) {
			fail("Malformed URL - " + e.getMessage());
		} catch (IOException e) {
			fail("Couldn't read source - " + e.getMessage());
		}
	
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

	@Test
	public void testGetBinaryData() throws Exception {
		Properties initParams = new Properties();
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		initParams.setProperty("item-node-filter", "TABLE,*,*,*");
		initParams.setProperty("attribute-node-filter", "LI,*,*,*;P,*,*,*");  
		initParams.setProperty("P", "Name");
		initParams.setProperty("LI", "Description");
		
		HTMLContentProvider provider = new HTMLContentProvider();
		provider.setName("net.sf.iqser.plugin.web.html");
		provider.setInitParams(initParams);
		
		// initialize the test
		prepare(); 
		provider.init();
		
		// execute test
		Content c = new Content();
		c.setContentUrl("http://www.designerfashion.de/Seiten/r2-Felljacke.html");
		byte[] byteArr = provider.getBinaryData(c);
		assertNotNull(byteArr);
		
		String page = new String(byteArr);
		
		assertTrue(page.startsWith("<HTML>  \n<HEAD>\n  " +
				"<META NAME=\"GENERATOR\" CONTENT=\"Adobe PageMill 3.0 Macintosh\">\n  " +
				"<TITLE>+++ streetnightwear +++</TITLE>\n</HEAD>"));
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

}
