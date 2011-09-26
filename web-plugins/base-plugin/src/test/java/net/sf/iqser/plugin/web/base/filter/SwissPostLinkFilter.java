package net.sf.iqser.plugin.web.base.filter;

import junit.framework.TestCase;

import com.torunski.crawler.filter.ILinkFilter;
import com.torunski.crawler.filter.RegularExpressionFilter;

public class SwissPostLinkFilter extends TestCase{
	
	public void test(){
		
		String anyChars = "\\S+";
		String zeroOrAnyChars = "\\S*";
		String or = "|";	
		String empty = "";
		
		//restrict to admin.ch or subdomains
		String domainFilter = anyChars + "\\.admin\\.ch";				
										
		String linkFilter= domainFilter +			
			"(("+anyChars+"/en/"+anyChars+"\\.html"+zeroOrAnyChars+")" + or + //path must contain language and must be html
			"("+anyChars+"\\.html"+anyChars+"lang=en"+ //html + lang attribute 
				"(" + empty + or + 					   // empty
					"(" + anyChars + "end_index=\\d{2,5})" + or + // end with end_index 
					"(" + anyChars + "id=\\d{5})"+				  //end with id
				 ")"+
			"))"; 
		
		System.out.println(linkFilter);
		    									
		ILinkFilter filter = new RegularExpressionFilter(linkFilter);
		
		String[] positiveExamples = 
		{
				"www.admin.ch/index.html?lang=en",
				"www.bk.admin.ch/index.html?lang=en",				
				"www.eda.admin.ch/eda/en/home.html",
				"www.eda.admin.ch/eda/en/home/topics.html",
				"www.vbs.admin.ch/internet/vbs/en/home.html",
				"www.ejpd.admin.ch/ejpd/en/home.html",
				"www.admin.ch/media/release/index.html?lang=en&msg-id=12345",
				"www.admin.ch/media/release/index.html?lang=en&end_index=12345"
		};
		
		for (String link : positiveExamples) {
			assertTrue(link, filter.accept("", link));
		}		

		
		String[] negExanmples = 
		{		
				"www.admin.ch/media/release/index.html?msg-id=12345",
				"www.admin.ch/media/release/index.html?lang=en&msg-id=12345&p1=v2",
				"qadmin.ch",				
				"www.another.com"				
		};
		for (String link : negExanmples) {
			assertFalse(link, filter.accept("", link));
		}
		
		
	}
}
