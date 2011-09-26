package net.sf.iqser.plugin.web.base.filter;

import junit.framework.TestCase;

import com.torunski.crawler.filter.BeginningPathFilter;
import com.torunski.crawler.filter.ILinkFilter;
import com.torunski.crawler.filter.RegularExpressionFilter;
import com.torunski.crawler.filter.ServerFilter;

public class TestLinkFilter extends TestCase {

	public void testServerFilter(){		
		ILinkFilter filter = new ServerFilter("www.admin.ch");
		
		String[] positiveExamples = {"www.admin.ch","www.admin.ch/abc"};
		for (String link : positiveExamples) {
			assertTrue(link, filter.accept("", link));
		}		

		String[] negExanmples = {"admin.ch","www.ada.admin.ch"};
		for (String link : negExanmples) {
			assertFalse(link, filter.accept("", link));
		}
	}

	public void testBeginingPathFilter(){		
		ILinkFilter filter = new BeginningPathFilter("www.admin.ch");
		
		String[] positiveExamples = {"www.admin.ch","www.admin.ch/abc/de"};
		for (String link : positiveExamples) {
			assertTrue(link, filter.accept("", link));
		}		

		String[] negExanmples = {"admin.ch","www.ada.admin.ch"};
		for (String link : negExanmples) {
			assertFalse(link, filter.accept("", link));
		}
	}
	
	public void testRegExFilter(){		
		ILinkFilter filter = new RegularExpressionFilter("\\S+[.]admin.ch\\S*");
		
		String[] positiveExamples = {"www.admin.ch","www.admin.ch/abc/de","www.ada.admin.ch"};
		for (String link : positiveExamples) {
			assertTrue(link, filter.accept("", link));
		}		

		String[] negExanmples = {"qadmin.ch","www.another.com"};
		for (String link : negExanmples) {
			assertFalse(link, filter.accept("", link));
		}
	}

}
