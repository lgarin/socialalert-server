package com.bravson.socialalert.app.utilities;

import org.apache.commons.lang3.StringUtils;

public class SolrUtils {

	// Set of characters / Strings SOLR treats as having special meaning in a query, and the corresponding Escaped versions.
    // Note that the actual operators '&&' and '||' don't show up here - we'll just escape the characters '&' and '|' wherever they occur.
    private static final String[] SOLR_SPECIAL_CHARACTERS = new String[] {"+", "-", "&", "|", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\\"};
    private static final String[] SOLR_REPLACEMENT_CHARACTERS = new String[] {"\\+", "\\-", "\\&", "\\|", "\\!", "\\(", "\\)", "\\{", "\\}", "\\[", "\\]", "\\^", "\\\"", "\\~", "\\*", "\\?", "\\:", "\\\\"};


    public static String escapeSolrCharacters(String value) {
        return StringUtils.replaceEach(value, SOLR_SPECIAL_CHARACTERS, SOLR_REPLACEMENT_CHARACTERS);
    } 
}
