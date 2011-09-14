package net.sf.iqser.plugin.web.html.filters;

import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;

/**
 * Extends HasAttributeFilter by matching attribute value against a regular expression
 *
 */
public class RegExHasAttributeFilter extends HasAttributeFilter {
	
	public RegExHasAttributeFilter() {
		super();
	}

	public RegExHasAttributeFilter(String attribute, String value) {
		super(attribute, value);
	}

	public RegExHasAttributeFilter(String attribute) {
		super(attribute);
	}

	public boolean accept(Node node)
    {
        boolean ret = false;
        if(node instanceof Tag)
        {
            Tag tag = (Tag)node;
            Attribute attribute = tag.getAttributeEx(mAttribute);
            if (attribute != null 
            		&& mValue != null
            		&& attribute.getValue() != null){
            	ret = attribute.getValue().matches(mValue);
            }                            
        }
        return ret;
    }
}
