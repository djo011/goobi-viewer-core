package io.goobi.viewer.model.citation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.undercouch.citeproc.csl.CSLType;
import io.goobi.viewer.AbstractTest;

public class CitationDataProviderTest extends AbstractTest {

    /**
     * @see CitationDataProvider#addItemData(String,Map,CSLType)
     * @verifies add item data correctly
     */
    @Test
    public void addItemData_shouldAddItemDataCorrectly() throws Exception {
        Map<String, List<String>> fields = new HashMap<>();
        fields.put(Citation.AUTHOR, Arrays.asList(new String[] { "Zahn, Timothy" }));
        fields.put(Citation.TITLE, Collections.singletonList("Thrawn"));
        fields.put(Citation.ISSUED, Collections.singletonList("2017-04-11"));
        fields.put(Citation.ISBN, Collections.singletonList("9780606412148"));

        CitationDataProvider provider = new CitationDataProvider();
        provider.addItemData("id", fields, CSLType.BOOK);
        Assert.assertNotNull(provider.retrieveItem("id"));
    }
}