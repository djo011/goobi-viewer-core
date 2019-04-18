/**
 * This file is part of the Goobi viewer - a content presentation and management application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.intranda.digiverso.presentation.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StringToolsTest {

    /**
     * @see StringTools#escapeHtmlChars(String)
     * @verifies escape all characters correctly
     */
    @Test
    public void escapeHtmlChars_shouldEscapeAllCharactersCorrectly() throws Exception {
        Assert.assertEquals("&lt;i&gt;&quot;A&amp;B&quot;&lt;/i&gt;", StringTools.escapeHtmlChars("<i>\"A&B\"</i>"));
    }

    /**
     * @see StringTools#removeDiacriticalMarks(String)
     * @verifies remove diacritical marks correctly
     */
    @Test
    public void removeDiacriticalMarks_shouldRemoveDiacriticalMarksCorrectly() throws Exception {
        Assert.assertEquals("aaaaoooouuuueeeeßn", StringTools.removeDiacriticalMarks("äáàâöóòôüúùûëéèêßñ"));
    }

    /**
     * @see StringTools#removeLineBreaks(String,String)
     * @verifies remove line breaks correctly
     */
    @Test
    public void removeLineBreaks_shouldRemoveLineBreaksCorrectly() throws Exception {
        Assert.assertEquals("foobar", StringTools.removeLineBreaks("foo\r\nbar", ""));
    }

    /**
     * @see StringTools#removeLineBreaks(String,String)
     * @verifies remove html line breaks correctly
     */
    @Test
    public void removeLineBreaks_shouldRemoveHtmlLineBreaksCorrectly() throws Exception {
        Assert.assertEquals("foo bar", StringTools.removeLineBreaks("foo<br>bar", " "));
        Assert.assertEquals("foo bar", StringTools.removeLineBreaks("foo<br/>bar", " "));
        Assert.assertEquals("foo bar", StringTools.removeLineBreaks("foo<br />bar", " "));
    }

    /**
     * @see StringTools#stripJS(String)
     * @verifies remove JS blocks correctly
     */
    @Test
    public void stripJS_shouldRemoveJSBlocksCorrectly() throws Exception {
        Assert.assertEquals("foo  bar", StringTools.stripJS("foo <script type=\"javascript\">\nfunction f {\n alert();\n}\n</script> bar"));
        Assert.assertEquals("foo  bar", StringTools.stripJS("foo <SCRIPT>\nfunction f {\n alert();\n}\n</ScRiPt> bar"));
    }

    @Test
    public void testEscapeQuotes() {
        String original = "Das ist ein 'String' mit \"Quotes\".";
        String reference = "Das ist ein \\'String\\' mit \\\"Quotes\\\".";

        String escaped = StringTools.escapeQuotes(original);
        Assert.assertEquals(reference, escaped);

        escaped = StringTools.escapeQuotes(reference);
        Assert.assertEquals(reference, escaped);
    }

    /**
     * @see StringTools#isImageUrl(String)
     * @verifies return true for image urls
     */
    @Test
    public void isImageUrl_shouldReturnTrueForImageUrls() throws Exception {
        Assert.assertTrue(StringTools.isImageUrl("https://example.com/default.jpg"));
        Assert.assertTrue(StringTools.isImageUrl("https://example.com/MASTER.TIFF"));
    }

    /**
     * @see StringTools#renameIncompatibleCSSClasses(String)
     * @verifies rename classes correctly
     */
    @Test
    public void renameIncompatibleCSSClasses_shouldRenameClassesCorrectly() throws Exception {
        Path file = Paths.get("resources/test/data/text_example_bad_classes.htm");
        Assert.assertTrue(Files.isRegularFile(file));

        String html = FileTools.getStringFromFile(file.toFile(), Helper.DEFAULT_ENCODING);
        Assert.assertNotNull(html);
        Assert.assertTrue(html.contains(".20Formatvorlage"));
        Assert.assertTrue(html.contains("class=\"20Formatvorlage"));

        html = StringTools.renameIncompatibleCSSClasses(html);
        Assert.assertFalse(html.contains(".20Formatvorlage"));
        Assert.assertFalse(html.contains("class=\"20Formatvorlage"));
        Assert.assertTrue(html.contains(".Formatvorlage20"));
        Assert.assertTrue(html.contains("class=\"Formatvorlage20"));
    }

    /**
     * @see StringTools#getHierarchyForCollection(String,String)
     * @verifies create list correctly
     */
    @Test
    public void getHierarchyForCollection_shouldCreateListCorrectly() throws Exception {
        List<String> result = StringTools.getHierarchyForCollection("a.b.c.d", ".");
        Assert.assertEquals(4, result.size());
        Assert.assertEquals("a", result.get(0));
        Assert.assertEquals("a.b", result.get(1));
        Assert.assertEquals("a.b.c", result.get(2));
        Assert.assertEquals("a.b.c.d", result.get(3));
    }
}