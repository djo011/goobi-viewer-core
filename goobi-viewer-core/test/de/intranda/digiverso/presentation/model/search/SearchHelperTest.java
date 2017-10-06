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
package de.intranda.digiverso.presentation.model.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import de.intranda.digiverso.presentation.AbstractDatabaseAndSolrEnabledTest;
import de.intranda.digiverso.presentation.controller.Configuration;
import de.intranda.digiverso.presentation.controller.DataManager;
import de.intranda.digiverso.presentation.controller.SolrConstants;
import de.intranda.digiverso.presentation.managedbeans.ContextMocker;
import de.intranda.digiverso.presentation.managedbeans.NavigationHelper;
import de.intranda.digiverso.presentation.model.search.FacetItem;
import de.intranda.digiverso.presentation.model.search.SearchHelper;
import de.intranda.digiverso.presentation.model.search.SearchHit;
import de.intranda.digiverso.presentation.model.search.SearchQueryGroup.SearchQueryGroupOperator;
import de.intranda.digiverso.presentation.model.search.SearchQueryItem.SearchItemOperator;
import de.intranda.digiverso.presentation.model.user.IPrivilegeHolder;
import de.intranda.digiverso.presentation.model.user.LicenseType;
import de.intranda.digiverso.presentation.model.user.User;
import de.intranda.digiverso.presentation.model.viewer.StringPair;

public class SearchHelperTest extends AbstractDatabaseAndSolrEnabledTest {

    public static final String LOREM_IPSUM =
            "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    @BeforeClass
    public static void setUpClass() throws Exception {
        AbstractDatabaseAndSolrEnabledTest.setUpClass();
        // Initialize the instance with a custom config file
        DataManager.getInstance().injectConfiguration(new Configuration("resources/test/config_viewer.test.xml"));
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        SearchHelper.docstrctWhitelistFilterSuffix = null;
        SearchHelper.collectionBlacklistFilterSuffix = null;
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies return true if required access conditions empty
     */
    @Test
    public void checkAccessPermission_shouldReturnTrueIfRequiredAccessConditionsEmpty() throws Exception {
        Assert.assertTrue(SearchHelper.checkAccessPermission(new ArrayList<LicenseType>(), new HashSet<String>(), IPrivilegeHolder.PRIV_VIEW_IMAGES,
                null, null, null));
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies return true if ip range allows access
     */
    @Test
    public void checkAccessPermission_shouldReturnTrueIfIpRangeAllowsAccess() throws Exception {
        Assert.assertTrue(SearchHelper.checkAccessPermission(DataManager.getInstance().getDao().getAllLicenseTypes(), new HashSet<>(Collections
                .singletonList("license type 3 name")), IPrivilegeHolder.PRIV_LIST, null, "127.0.0.1", null));
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies return true if required access conditions contain only open access
     */
    @Test
    public void checkAccessPermission_shouldReturnTrueIfRequiredAccessConditionsContainOnlyOpenAccess() throws Exception {
        List<LicenseType> licenseTypes = new ArrayList<>();
        LicenseType lt = new LicenseType();
        licenseTypes.add(lt);
        lt.setName("type1");
        lt = new LicenseType();
        licenseTypes.add(lt);
        lt.setName("type2");

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add(SolrConstants.OPEN_ACCESS_VALUE);
        Assert.assertTrue(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_VIEW_IMAGES, null, null,
                null));

        recordAccessConditions.add("type1");
        recordAccessConditions.add("type2");
        Assert.assertFalse(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_VIEW_IMAGES, null, null,
                null));
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies return true if all license types allow privilege by default
     */
    @Test
    public void checkAccessPermission_shouldReturnTrueIfAllLicenseTypesAllowPrivilegeByDefault() throws Exception {
        List<LicenseType> licenseTypes = new ArrayList<>();
        LicenseType lt = new LicenseType();
        licenseTypes.add(lt);
        lt.setName("type1");
        lt.getPrivileges().add(IPrivilegeHolder.PRIV_VIEW_IMAGES);
        lt = new LicenseType();
        licenseTypes.add(lt);
        lt.setName("type2");
        lt.getPrivileges().add(IPrivilegeHolder.PRIV_VIEW_IMAGES);

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add("type1");
        recordAccessConditions.add("condition2");

        Assert.assertTrue(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_VIEW_IMAGES, null, null,
                null));
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies return false if not all license types allow privilege by default
     */
    @Test
    public void checkAccessPermission_shouldReturnFalseIfNotAllLicenseTypesAllowPrivilegeByDefault() throws Exception {
        List<LicenseType> licenseTypes = new ArrayList<>();
        LicenseType lt = new LicenseType();
        licenseTypes.add(lt);
        lt.setName("type1");
        lt.getPrivileges().add(IPrivilegeHolder.PRIV_VIEW_IMAGES);
        lt = new LicenseType();
        licenseTypes.add(lt);
        lt.setName("type2");

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add("type1");
        recordAccessConditions.add("type2");

        Assert.assertFalse(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_VIEW_IMAGES, null, null,
                null));
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies return true if ip range allows access to all conditions
     */
    @Test
    public void checkAccessPermission_shouldReturnTrueIfIpRangeAllowsAccessToAllConditions() throws Exception {
        List<LicenseType> licenseTypes = new ArrayList<>();
        {
            // 'license type 1 name' doesn't allow anything by default
            LicenseType lt = new LicenseType();
            lt.setName("license type 1 name");
            licenseTypes.add(lt);
        }
        {
            // IP range has an explicit license for PRIV_LIST for 'license type 3 name'
            LicenseType lt = new LicenseType();
            lt.setName("license type 3 name");
            licenseTypes.add(lt);
        }

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add("license type 3 name");
        Assert.assertTrue(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_LIST, null, "127.0.0.1",
                null));

        recordAccessConditions.add("license type 1 name");
        Assert.assertFalse(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_LIST, null, "127.0.0.1",
                null));
    }

    /**
     * @see SearchHelper#checkAccessPermission(List,Set,String,User,String,String)
     * @verifies not return true if no ip range matches
     */
    @Test
    public void checkAccessPermission_shouldNotReturnTrueIfNoIpRangeMatches() throws Exception {
        List<LicenseType> licenseTypes = new ArrayList<>();
        {
            // 'license type 1 name' doesn't allow anything by default
            LicenseType lt = new LicenseType();
            lt.setName("license type 1 name");
            licenseTypes.add(lt);
        }

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add("license type 1 name");
        Assert.assertFalse(SearchHelper.checkAccessPermission(licenseTypes, recordAccessConditions, IPrivilegeHolder.PRIV_LIST, null, "11.22.33.44",
                null));
    }

    /**
     * @see SearchHelper#getRelevantLicenseTypesOnly(List,Set,String)
     * @verifies remove license types whose names do not match access conditions
     */
    @Test
    public void getRelevantLicenseTypesOnly_shouldRemoveLicenseTypesWhoseNamesDoNotMatchAccessConditions() throws Exception {
        List<LicenseType> allLicenseTypes = new ArrayList<>();

        LicenseType lt = new LicenseType();
        allLicenseTypes.add(lt);
        lt.setName("type1");

        lt = new LicenseType();
        allLicenseTypes.add(lt);
        lt.setName("type2");

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add("type2");

        List<LicenseType> ret = SearchHelper.getRelevantLicenseTypesOnly(allLicenseTypes, recordAccessConditions, SolrConstants.PI_TOPSTRUCT
                + ":PPN517154005");
        Assert.assertEquals(1, ret.size());
        Assert.assertEquals("type2", ret.get(0).getName());
    }

    /**
     * @see SearchHelper#getRelevantLicenseTypesOnly(List,Set,String)
     * @verifies remove license types whose condition query excludes the given pi
     */
    @Test
    public void getRelevantLicenseTypesOnly_shouldRemoveLicenseTypesWhoseConditionQueryExcludesTheGivenPi() throws Exception {
        List<LicenseType> allLicenseTypes = new ArrayList<>();

        LicenseType lt = new LicenseType();
        allLicenseTypes.add(lt);
        lt.setName("type1");
        lt.setConditions(SolrConstants.PI_TOPSTRUCT + ":unknownidentifier");

        lt = new LicenseType();
        allLicenseTypes.add(lt);
        lt.setName("type2");
        lt.setConditions(SolrConstants.PI_TOPSTRUCT + ":PPN517154005");

        lt = new LicenseType();
        allLicenseTypes.add(lt);
        lt.setName("type3");

        Set<String> recordAccessConditions = new HashSet<>();
        recordAccessConditions.add("type1");
        recordAccessConditions.add("type2");
        recordAccessConditions.add("type3");

        List<LicenseType> ret = SearchHelper.getRelevantLicenseTypesOnly(allLicenseTypes, recordAccessConditions, SolrConstants.PI_TOPSTRUCT
                + ":PPN517154005");
        Assert.assertEquals(2, ret.size());
        Assert.assertEquals("type2", ret.get(0).getName());
        Assert.assertEquals("type3", ret.get(1).getName());
    }

    /**
     * @see SearchHelper#searchAutosuggestion(String)
     * @verifies return autosuggestions correctly
     */
    @Test
    public void searchAutosuggestion_shouldReturnAutosuggestionsCorrectly() throws Exception {
        List<String> values = SearchHelper.searchAutosuggestion("klein", null, null);
        Assert.assertFalse(values.isEmpty());
    }

    /**
     * @see SearchHelper#searchAutosuggestion(String,String)
     * @verifies filter by collection correctly
     */
    @Test
    public void searchAutosuggestion_shouldFilterByCollectionCorrectly() throws Exception {
        FacetItem item = new FacetItem(SolrConstants.FACET_DC + ":varia", true);
        List<String> values = SearchHelper.searchAutosuggestion("kartenundplaene", Collections.singletonList(item), null);
        Assert.assertTrue(values.isEmpty());
    }

    /**
     * @see SearchHelper#searchAutosuggestion(String,List,List)
     * @verifies filter by facet correctly
     */
    @Test
    public void searchAutosuggestion_shouldFilterByFacetCorrectly() throws Exception {
        FacetItem item = new FacetItem(SolrConstants.TITLE + ":something", false);
        List<String> values = SearchHelper.searchAutosuggestion("kartenundplaene", null, Collections.singletonList(item));
        Assert.assertTrue(values.isEmpty());
    }

    /**
     * @see SearchHelper#findAllCollectionsFromField(String,String,boolean,boolean,boolean,boolean)
     * @verifies find all collections
     */
    @Test
    public void findAllCollectionsFromField_shouldFindAllCollections() throws Exception {
        // First, make sure the docstruct whitelist and the collection blacklist always come from the same config file;
        Map<String, Long> collections = SearchHelper.findAllCollectionsFromField(SolrConstants.DC, SolrConstants.DC, true, true, true, true);
        Assert.assertEquals(16, collections.size());
        List<String> keys = new ArrayList<>(collections.keySet());
        // Collections.sort(keys);
        for (String key : keys) {
            switch (key) {
                case ("a"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("a.b"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("a.b.c"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("a.b.c.d"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("alle"):
                    Assert.assertEquals(Long.valueOf(13), collections.get(key));
                    break;
                case ("mehrbaendigeswerk"):
                    Assert.assertEquals(Long.valueOf(2), collections.get(key));
                    break;
                case ("monographie"):
                    Assert.assertEquals(Long.valueOf(4), collections.get(key));
                    break;
                case ("multimedia"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("ocr"):
                    Assert.assertEquals(Long.valueOf(6), collections.get(key));
                    break;
                case ("ocr.antiqua"):
                    Assert.assertEquals(Long.valueOf(3), collections.get(key));
                    break;
                case ("ocr.fraktur"):
                    Assert.assertEquals(Long.valueOf(3), collections.get(key));
                    break;
                case ("paedagogik"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("sonstiges"):
                    Assert.assertEquals(Long.valueOf(2), collections.get(key));
                    break;
                case ("sonstiges.langestoc"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("sonstiges.querformat"):
                    Assert.assertEquals(Long.valueOf(1), collections.get(key));
                    break;
                case ("zeitschrift"):
                    Assert.assertEquals(Long.valueOf(2), collections.get(key));
                    break;
                default:
                    Assert.fail("Unknown collection name: " + key);
                    break;
            }
        }
    }

    /**
     * @see SearchHelper#getPersonalFilterQuerySuffix(User,String)
     * @verifies construct suffix correctly
     */
    @Test
    public void getPersonalFilterQuerySuffix_shouldConstructSuffixCorrectly() throws Exception {
        String suffix = SearchHelper.getPersonalFilterQuerySuffix(null, null);
        Assert.assertEquals(" -(" + SolrConstants.ACCESSCONDITION + ":\"license type 1 name\" AND YEAR:[* TO 3000]) -" + SolrConstants.ACCESSCONDITION
                + ":\"license type 3 name\"", suffix);
    }

    /**
     * @see SearchHelper#getPersonalFilterQuerySuffix(User,String)
     * @verifies construct suffix correctly if user has license privilege
     */
    @Test
    public void getPersonalFilterQuerySuffix_shouldConstructSuffixCorrectlyIfUserHasLicensePrivilege() throws Exception {
        User user = DataManager.getInstance().getDao().getUser(2);
        String suffix = SearchHelper.getPersonalFilterQuerySuffix(user, null);
        Assert.assertEquals(" -" + SolrConstants.ACCESSCONDITION + ":\"license type 3 name\"", suffix);
    }

    /**
     * @see SearchHelper#getPersonalFilterQuerySuffix(User,String)
     * @verifies construct suffix correctly if ip range has license privilege
     */
    @Test
    public void getPersonalFilterQuerySuffix_shouldConstructSuffixCorrectlyIfIpRangeHasLicensePrivilege() throws Exception {
        String suffix = SearchHelper.getPersonalFilterQuerySuffix(null, "127.0.0.1");
        Assert.assertEquals("", suffix);
    }

    /**
     * @see SearchHelper#getDocstrctWhitelistFilterSuffix()
     * @verifies construct suffix correctly
     */
    @Test
    public void getDocstrctWhitelistFilterSuffix_shouldConstructSuffixCorrectly() throws Exception {
        String suffix = SearchHelper.getDocstrctWhitelistFilterSuffix();
        Assert.assertEquals(" AND (" + SolrConstants.DOCSTRCT + ":Monograph OR " + SolrConstants.DOCSTRCT + ":MultiVolumeWork OR "
                + SolrConstants.DOCSTRCT + ":Periodical)", suffix);
    }

    /**
     * @see SearchHelper#truncateFulltext(List,String)
     * @verifies truncate string to 200 chars if no terms are given and add prefix and suffix
     */
    @Test
    public void truncateFulltext_shouldTruncateStringTo200CharsIfNoTermsAreGivenAndAddPrefixAndSuffix() throws Exception {
    }

    /**
     * @see SearchHelper#truncateFulltext(List,String)
     * @verifies make terms bold if found in text
     */
    @Test
    public void truncateFulltext_shouldMakeTermsBoldIfFoundInText() throws Exception {
        String original = LOREM_IPSUM;
        String[] terms = { "ipsum", "tempor", "labore" };
        List<String> truncated = SearchHelper.truncateFulltext(new HashSet<>(Arrays.asList(terms)), original, 200, true);
        Assert.assertFalse(truncated.isEmpty());
        System.out.println(truncated.get(0));
        //        Assert.assertTrue(truncated.get(0).contains("<span class=\"search-list--highlight\">ipsum</span>"));
        Assert.assertTrue(truncated.get(0).contains("<span class=\"search-list--highlight\">tempor</span>"));
        //        Assert.assertTrue(truncated.get(0).contains("<span class=\"search-list--highlight\">labore</span>"));
        // TODO The other two terms aren't highlighted when using random length phrase
    }

    /**
     * @see SearchHelper#truncateFulltext(List,String)
     * @verifies not add prefix and suffix to text
     */
    @Test
    public void truncateFulltext_shouldNotAddPrefixAndSuffixToText() throws Exception {
        String original = "text";
        List<String> truncated = SearchHelper.truncateFulltext(null, original, 200, true);
        Assert.assertFalse(truncated.isEmpty());
        Assert.assertEquals("text", truncated.get(0));
    }

    /**
     * @see SearchHelper#truncateFulltext(List,String)
     * @verifies truncate string to 200 chars if no terms are given
     */
    @Test
    public void truncateFulltext_shouldTruncateStringTo200CharsIfNoTermsAreGiven() throws Exception {
        String original = LOREM_IPSUM;
        List<String> truncated = SearchHelper.truncateFulltext(null, original, 200, true);
        Assert.assertFalse(truncated.isEmpty());
        Assert.assertEquals(original.substring(0, 200), truncated.get(0));
    }

    /**
     * @see SearchHelper#truncateFulltext(List,String)
     * @verifies truncate string to 200 chars if no term has been found
     */
    @Test
    public void truncateFulltext_shouldTruncateStringTo200CharsIfNoTermHasBeenFound() throws Exception {
        String original = LOREM_IPSUM;
        String[] terms = { "boogers" };
        List<String> truncated = SearchHelper.truncateFulltext(new HashSet<>(Arrays.asList(terms)), original, 200, true);
        Assert.assertFalse(truncated.isEmpty());
        Assert.assertEquals(original.substring(0, 200), truncated.get(0));
    }

    /**
     * @see SearchHelper#truncateFulltext(List,String)
     * @verifies remove unclosed HTML tags
     */
    @Test
    public void truncateFulltext_shouldRemoveUnclosedHTMLTags() throws Exception {
        List<String> truncated = SearchHelper.truncateFulltext(null, "Hello <a href", 200, true);
        Assert.assertFalse(truncated.isEmpty());
        Assert.assertEquals("Hello", truncated.get(0));
        truncated = SearchHelper.truncateFulltext(null, "Hello <a href ...> and then <b", 200, true);
        Assert.assertEquals("Hello <a href ...> and then", truncated.get(0));
    }

    /**
     * @see SearchHelper#truncateFulltext(Set,String,int,boolean)
     * @verifies return multiple match fragments correctly
     */
    @Test
    public void truncateFulltext_shouldReturnMultipleMatchFragmentsCorrectly() throws Exception {
        String original = LOREM_IPSUM;
        String[] terms = { "in" };
        List<String> truncated = SearchHelper.truncateFulltext(new HashSet<>(Arrays.asList(terms)), original, 50, false);
        Assert.assertEquals(7, truncated.size());
        for (String fragment : truncated) {
            Assert.assertTrue(fragment.contains("<span class=\"search-list--highlight\">in</span>"));
        }
    }

    /**
     * @see SearchHelper#extractSearchTermsFromQuery(String)
     * @verifies extract all values from query except from NOT blocks
     */
    @Test
    public void extractSearchTermsFromQuery_shouldExtractAllValuesFromQueryExceptFromNOTBlocks() throws Exception {
        Map<String, Set<String>> result = SearchHelper.extractSearchTermsFromQuery(
                "(MD_X:value1 OR MD_X:value2 OR (SUPERDEFAULT:value3 AND :value4:)) AND SUPERFULLTEXT:\"hello-world\" AND NOT(MD_Y:value_not)", null);
        Assert.assertEquals(3, result.size());
        {
            Set<String> terms = result.get("MD_X");
            Assert.assertNotNull(terms);
            Assert.assertEquals(2, terms.size());
            Assert.assertTrue(terms.contains("value1"));
            Assert.assertTrue(terms.contains("value2"));
        }
        {
            Set<String> terms = result.get(SolrConstants.DEFAULT);
            Assert.assertNotNull(terms);
            Assert.assertEquals(2, terms.size());
            Assert.assertTrue(terms.contains("value3"));
            Assert.assertTrue(terms.contains(":value4:"));
        }
        {
            Set<String> terms = result.get(SolrConstants.FULLTEXT);
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("hello-world"));
        }
        Assert.assertNull(result.get("MD_Y"));

    }

    /**
     * @see SearchHelper#extractSearchTermsFromQuery(String)
     * @verifies handle multiple phrases in query correctly
     */
    @Test
    public void extractSearchTermsFromQuery_shouldHandleMultiplePhrasesInQueryCorrectly() throws Exception {
        Map<String, Set<String>> result = SearchHelper.extractSearchTermsFromQuery(
                "(MD_A:\"value1\" OR MD_B:\"value1\" OR MD_C:\"value2\" OR MD_D:\"value2\")", null);
        Assert.assertEquals(4, result.size());
        {
            Set<String> terms = result.get("MD_A");
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("value1"));
        }
        {
            Set<String> terms = result.get("MD_B");
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("value1"));
        }
        {
            Set<String> terms = result.get("MD_C");
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("value2"));
        }
        {
            Set<String> terms = result.get("MD_D");
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("value2"));
        }
    }

    /**
     * @see SearchHelper#extractSearchTermsFromQuery(String,String)
     * @verifies skip discriminator value
     */
    @Test
    public void extractSearchTermsFromQuery_shouldSkipDiscriminatorValue() throws Exception {
        Map<String, Set<String>> result = SearchHelper.extractSearchTermsFromQuery(
                "(MD_A:\"value1\" OR MD_B:\"value1\" OR MD_C:\"value2\" OR MD_D:\"value3\")", "value1");
        Assert.assertEquals(2, result.size());
        {
            Set<String> terms = result.get("MD_C");
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("value2"));
        }
        {
            Set<String> terms = result.get("MD_D");
            Assert.assertNotNull(terms);
            Assert.assertEquals(1, terms.size());
            Assert.assertTrue(terms.contains("value3"));
        }
    }

    /**
     * @see SearchHelper#extractSearchTermsFromQuery(String)
     * @verifies throw IllegalArgumentException if query is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void extractSearchTermsFromQuery_shouldThrowIllegalArgumentExceptionIfQueryIsNull() throws Exception {
        SearchHelper.extractSearchTermsFromQuery(null, null);
    }

    /**
     * @see SearchHelper#generateCollectionBlacklistFilterSuffix()
     * @verifies construct suffix correctly
     */
    @Test
    public void generateCollectionBlacklistFilterSuffix_shouldConstructSuffixCorrectly() throws Exception {
        String suffix = SearchHelper.generateCollectionBlacklistFilterSuffix(SolrConstants.DC);
        Assert.assertEquals(" -" + SolrConstants.DC + ":collection1 -" + SolrConstants.DC + ":collection2", suffix);
    }

    /**
     * @see SearchHelper#populateCollectionBlacklistFilterSuffixes(String)
     * @verifies populate all mode correctly
     */
    @Test
    public void populateCollectionBlacklistFilterSuffixes_shouldPopulateAllModeCorrectly() throws Exception {
    }

    /**
     * @see SearchHelper#checkCollectionInBlacklist(String,List)
     * @verifies match simple collections correctly
     */
    @Test
    public void checkCollectionInBlacklist_shouldMatchSimpleCollectionsCorrectly() throws Exception {
        {
            Set<String> blacklist = new HashSet<>(Collections.singletonList("a"));
            Assert.assertTrue(SearchHelper.checkCollectionInBlacklist("a", blacklist));
            Assert.assertFalse(SearchHelper.checkCollectionInBlacklist("z", blacklist));
        }
        {
            Set<String> blacklist = new HashSet<>(Collections.singletonList("a.b.c.d"));
            Assert.assertTrue(SearchHelper.checkCollectionInBlacklist("a.b.c.d", blacklist));
            Assert.assertFalse(SearchHelper.checkCollectionInBlacklist("a.b.c.z", blacklist));
        }
    }

    /**
     * @see SearchHelper#checkCollectionInBlacklist(String,List)
     * @verifies match subcollections correctly
     */
    @Test
    public void checkCollectionInBlacklist_shouldMatchSubcollectionsCorrectly() throws Exception {
        Set<String> blacklist = new HashSet<>(Collections.singletonList("a.b"));
        Assert.assertTrue(SearchHelper.checkCollectionInBlacklist("a.b.c.d", blacklist));
        Assert.assertFalse(SearchHelper.checkCollectionInBlacklist("a.z", blacklist));
    }

    /**
     * @see SearchHelper#checkCollectionInBlacklist(String,List)
     * @verifies throw IllegalArgumentException if dc is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkCollectionInBlacklist_shouldThrowIllegalArgumentExceptionIfDcIsNull() throws Exception {
        SearchHelper.checkCollectionInBlacklist(null, new HashSet<>(Collections.singletonList("a*")));
    }

    /**
     * @see SearchHelper#checkCollectionInBlacklist(String,List)
     * @verifies throw IllegalArgumentException if blacklist is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void checkCollectionInBlacklist_shouldThrowIllegalArgumentExceptionIfBlacklistIsNull() throws Exception {
        SearchHelper.checkCollectionInBlacklist("a", null);
    }

    /**
     * @see SearchHelper#getDiscriminatorFieldFilterSuffix(String)
     * @verifies construct subquery correctly
     */
    @Test
    public void getDiscriminatorFieldFilterSuffix_shouldConstructSubqueryCorrectly() throws Exception {
        NavigationHelper nh = new NavigationHelper();
        nh.setSubThemeDiscriminatorValue("val");
        Assert.assertEquals(" AND fie:val", SearchHelper.getDiscriminatorFieldFilterSuffix(nh, "fie"));
    }

    /**
     * @see SearchHelper#getDiscriminatorFieldFilterSuffix(String)
     * @verifies return empty string if discriminator value is empty or hyphen
     */
    @Test
    public void getDiscriminatorFieldFilterSuffix_shouldReturnEmptyStringIfDiscriminatorValueIsEmptyOrHyphen() throws Exception {
        NavigationHelper nh = new NavigationHelper();
        Assert.assertEquals("", SearchHelper.getDiscriminatorFieldFilterSuffix(nh, "fie"));
        nh.setSubThemeDiscriminatorValue("-");
        Assert.assertEquals("", SearchHelper.getDiscriminatorFieldFilterSuffix(nh, "fie"));
    }

    /**
     * @see SearchHelper#defacetifyField(String)
     * @verifies defacetify correctly
     */
    @Test
    public void defacetifyField_shouldDefacetifyCorrectly() throws Exception {
        Assert.assertEquals(SolrConstants.DC, SearchHelper.defacetifyField(SolrConstants.FACET_DC));
        Assert.assertEquals("MD_TITLE", SearchHelper.defacetifyField("FACET_TITLE"));
    }

    /**
     * @see SearchHelper#facetifyList(List)
     * @verifies facetify correctly
     */
    @Test
    public void facetifyList_shouldFacetifyCorrectly() throws Exception {
        List<String> result = SearchHelper.facetifyList(Arrays.asList(new String[] { SolrConstants.DC, "MD_TITLE_UNTOKENIZED" }));
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(SolrConstants.FACET_DC, result.get(0));
        Assert.assertEquals("FACET_TITLE", result.get(1));
    }

    /**
     * @see SearchHelper#getAllSuffixes(HttpSession,boolean,boolean)
     * @verifies add static suffix
     */
    @Test
    public void getAllSuffixes_shouldAddStaticSuffix() throws Exception {
        String suffix = SearchHelper.getAllSuffixes(null, false, false);
        Assert.assertNotNull(suffix);
        Assert.assertTrue(suffix.contains(DataManager.getInstance().getConfiguration().getStaticQuerySuffix()));
    }

    /**
     * @see SearchHelper#getAllSuffixes(HttpSession,boolean,boolean)
     * @verifies add collection blacklist suffix
     */
    @Test
    public void getAllSuffixes_shouldAddCollectionBlacklistSuffix() throws Exception {

        String suffix = SearchHelper.getAllSuffixes(false);
        Assert.assertNotNull(suffix);
        Assert.assertTrue(suffix.contains(" -" + SolrConstants.DC + ":collection1 -" + SolrConstants.DC + ":collection2"));
    }

    /**
     * @see SearchHelper#getAllSuffixes(HttpSession,boolean,boolean)
     * @verifies add discriminator value suffix
     */
    @Test
    public void getAllSuffixes_shouldAddDiscriminatorValueSuffix() throws Exception {

        FacesContext facesContext = ContextMocker.mockFacesContext();
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        Map<String, Object> sessionMap = new HashMap<>();
        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        Mockito.when(externalContext.getSessionMap()).thenReturn(sessionMap);

        try {
            NavigationHelper nh = new NavigationHelper();
            nh.setSubThemeDiscriminatorValue("dvalue");
            sessionMap.put("navigationHelper", nh);

            String suffix = SearchHelper.getAllSuffixes(null, false, true);
            Assert.assertNotNull(suffix);
            System.out.println(suffix);
            Assert.assertTrue(suffix.contains(" AND " + DataManager.getInstance().getConfiguration().getSubthemeDiscriminatorField() + ":dvalue"));
        } finally {
            // Reset the mock because otherwise the discriminator value will persist for other tests
            Mockito.reset(externalContext);
        }
    }

    /**
     * @see SearchHelper#generateAccessCheckQuery(String,String)
     * @verifies use correct field name for AV files
     */
    @Test
    public void generateAccessCheckQuery_shouldUseCorrectFieldNameForAVFiles() throws Exception {
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.tif");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME + ":\"00000001.tif\"", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME, result[1]);
        }
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.webm");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME_WEBM + ":\"00000001.webm\"", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME_WEBM, result[1]);
        }
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.mp4");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME_MP4 + ":\"00000001.mp4\"", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME_MP4, result[1]);
        }
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.mp3");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME_MPEG3 + ":\"00000001.mp3\"", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME_MPEG3, result[1]);
        }
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.ogg");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME_OGG + ":\"00000001.ogg\"", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME_OGG, result[1]);
        }
    }

    /**
     * @see SearchHelper#generateAccessCheckQuery(String,String)
     * @verifies use correct file name for text files
     */
    @Test
    public void generateAccessCheckQuery_shouldUseCorrectFileNameForTextFiles() throws Exception {
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.txt");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME + ":00000001.*", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME, result[1]);
        }
        {
            String[] result = SearchHelper.generateAccessCheckQuery("PPN123456789", "00000001.xml");
            Assert.assertEquals(SolrConstants.PI_TOPSTRUCT + ":PPN123456789 AND " + SolrConstants.FILENAME + ":00000001.*", result[0]);
            Assert.assertEquals(SolrConstants.FILENAME, result[1]);
        }
    }

    /**
     * @see SearchHelper#generateExpandQuery(List,Set)
     * @verifies generate query correctly
     */
    @Test
    public void generateExpandQuery_shouldGenerateQueryCorrectly() throws Exception {
        List<String> fields = Arrays.asList(new String[] { SolrConstants.DEFAULT, SolrConstants.FULLTEXT, SolrConstants.NORMDATATERMS,
                SolrConstants.UGCTERMS, SolrConstants.OVERVIEWPAGE_DESCRIPTION, SolrConstants.OVERVIEWPAGE_PUBLICATIONTEXT });
        Map<String, Set<String>> searchTerms = new HashMap<>();
        searchTerms.put(SolrConstants.DEFAULT, new HashSet<>(Arrays.asList(new String[] { "one", "two" })));
        searchTerms.put(SolrConstants.FULLTEXT, new HashSet<>(Arrays.asList(new String[] { "two", "three" })));
        searchTerms.put(SolrConstants.NORMDATATERMS, new HashSet<>(Arrays.asList(new String[] { "four", "five" })));
        searchTerms.put(SolrConstants.UGCTERMS, new HashSet<>(Arrays.asList(new String[] { "six" })));
        searchTerms.put(SolrConstants.OVERVIEWPAGE_DESCRIPTION, new HashSet<>(Arrays.asList(new String[] { "seven" })));
        searchTerms.put(SolrConstants.OVERVIEWPAGE_PUBLICATIONTEXT, new HashSet<>(Arrays.asList(new String[] { "eight" })));
        Assert.assertEquals(
                " +(DEFAULT:(one OR two) OR FULLTEXT:(two OR three) OR NORMDATATERMS:(four OR five) OR UGCTERMS:(six) OR OVERVIEWPAGE_DESCRIPTION:(seven) OR OVERVIEWPAGE_PUBLICATIONTEXT:(eight))",
                SearchHelper.generateExpandQuery(fields, searchTerms));
    }

    /**
     * @see SearchHelper#generateExpandQuery(List,Map)
     * @verifies return empty string if no fields match
     */
    @Test
    public void generateExpandQuery_shouldReturnEmptyStringIfNoFieldsMatch() throws Exception {
        List<String> fields = Arrays.asList(new String[] { SolrConstants.DEFAULT, SolrConstants.FULLTEXT, SolrConstants.NORMDATATERMS,
                SolrConstants.UGCTERMS, SolrConstants.OVERVIEWPAGE_DESCRIPTION, SolrConstants.OVERVIEWPAGE_PUBLICATIONTEXT });
        Map<String, Set<String>> searchTerms = new HashMap<>();
        searchTerms.put("MD_TITLE", new HashSet<>(Arrays.asList(new String[] { "one", "two" })));

        Assert.assertEquals("", SearchHelper.generateExpandQuery(fields, searchTerms));
    }

    /**
     * @see SearchHelper#generateExpandQuery(List,Map)
     * @verifies skip reserved fields
     */
    @Test
    public void generateExpandQuery_shouldSkipReservedFields() throws Exception {
        List<String> fields = Arrays.asList(new String[] { SolrConstants.DEFAULT, SolrConstants.FULLTEXT, SolrConstants.NORMDATATERMS,
                SolrConstants.UGCTERMS, SolrConstants.OVERVIEWPAGE_DESCRIPTION, SolrConstants.OVERVIEWPAGE_PUBLICATIONTEXT,
                SolrConstants.PI_TOPSTRUCT, SolrConstants.DC, SolrConstants.DOCSTRCT });
        Map<String, Set<String>> searchTerms = new HashMap<>();
        searchTerms.put(SolrConstants.DEFAULT, new HashSet<>(Arrays.asList(new String[] { "one", "two" })));
        searchTerms.put(SolrConstants.FULLTEXT, new HashSet<>(Arrays.asList(new String[] { "two", "three" })));
        searchTerms.put(SolrConstants.NORMDATATERMS, new HashSet<>(Arrays.asList(new String[] { "four", "five" })));
        searchTerms.put(SolrConstants.UGCTERMS, new HashSet<>(Arrays.asList(new String[] { "six" })));
        searchTerms.put(SolrConstants.OVERVIEWPAGE_DESCRIPTION, new HashSet<>(Arrays.asList(new String[] { "seven" })));
        searchTerms.put(SolrConstants.OVERVIEWPAGE_PUBLICATIONTEXT, new HashSet<>(Arrays.asList(new String[] { "eight" })));
        Assert.assertEquals(
                " +(DEFAULT:(one OR two) OR FULLTEXT:(two OR three) OR NORMDATATERMS:(four OR five) OR UGCTERMS:(six) OR OVERVIEWPAGE_DESCRIPTION:(seven) OR OVERVIEWPAGE_PUBLICATIONTEXT:(eight))",
                SearchHelper.generateExpandQuery(fields, searchTerms));
    }

    /**
     * @see SearchHelper#generateExpandQuery(List,Map)
     * @verifies escape reserved characters
     */
    @Test
    public void generateExpandQuery_shouldEscapeReservedCharacters() throws Exception {
        List<String> fields = Arrays.asList(new String[] { SolrConstants.DEFAULT });
        Map<String, Set<String>> searchTerms = new HashMap<>();
        searchTerms.put(SolrConstants.DEFAULT, new HashSet<>(Arrays.asList(new String[] { "[one]", ":two:" })));
        Assert.assertEquals(" +(DEFAULT:(\\[one\\] OR \\:two\\:))", SearchHelper.generateExpandQuery(fields, searchTerms));
    }

    /**
     * @see SearchHelper#generateAdvancedExpandQuery(List,int)
     * @verifies generate query correctly
     */
    @Test
    public void generateAdvancedExpandQuery_shouldGenerateQueryCorrectly() throws Exception {
        List<SearchQueryGroup> groups = new ArrayList<>(2);
        {
            SearchQueryGroup group = new SearchQueryGroup(null, 2);
            group.setOperator(SearchQueryGroupOperator.AND);
            group.getQueryItems().get(0).setOperator(SearchItemOperator.AND);
            group.getQueryItems().get(0).setField(SolrConstants.DOCSTRCT);
            group.getQueryItems().get(0).setValue("Monograph");
            group.getQueryItems().get(1).setOperator(SearchItemOperator.AND);
            group.getQueryItems().get(1).setField(SolrConstants.TITLE);
            group.getQueryItems().get(1).setValue("foo bar");
            groups.add(group);
        }
        {
            SearchQueryGroup group = new SearchQueryGroup(null, 2);
            group.setOperator(SearchQueryGroupOperator.OR);
            group.getQueryItems().get(0).setField(SolrConstants.DOCSTRCT);
            group.getQueryItems().get(0).setValue("Volume");
            group.getQueryItems().get(1).setOperator(SearchItemOperator.OR);
            group.getQueryItems().get(1).setField("MD_SHELFMARK");
            group.getQueryItems().get(1).setValue("bla blup");
            groups.add(group);
        }

        String result = SearchHelper.generateAdvancedExpandQuery(groups, 0);
        Assert.assertEquals(" +((DOCSTRCT:Monograph AND MD_TITLE:(foo AND bar)) AND (DOCSTRCT:Volume OR MD_SHELFMARK:(bla OR blup)))", result);
    }

    /**
     * @see SearchHelper#generateAdvancedExpandQuery(List,int)
     * @verifies skip reserved fields
     */
    @Test
    public void generateAdvancedExpandQuery_shouldSkipReservedFields() throws Exception {
        List<SearchQueryGroup> groups = new ArrayList<>(1);

        SearchQueryGroup group = new SearchQueryGroup(null, 3);
        group.setOperator(SearchQueryGroupOperator.AND);
        group.getQueryItems().get(0).setOperator(SearchItemOperator.AND);
        group.getQueryItems().get(0).setField(SolrConstants.DOCSTRCT);
        group.getQueryItems().get(0).setValue("Monograph");
        group.getQueryItems().get(1).setOperator(SearchItemOperator.AND);
        group.getQueryItems().get(1).setField(SolrConstants.PI_TOPSTRUCT);
        group.getQueryItems().get(1).setValue("PPN123");
        group.getQueryItems().get(2).setOperator(SearchItemOperator.AND);
        group.getQueryItems().get(2).setField(SolrConstants.DC);
        group.getQueryItems().get(2).setValue("co1");
        groups.add(group);

        String result = SearchHelper.generateAdvancedExpandQuery(groups, 0);
        Assert.assertEquals(" +((DOCSTRCT:Monograph))", result);
    }

    /**
     * @see SearchHelper#exportSearchAsExcel(String,List,Map)
     * @verifies create excel workbook correctly
     */
    @Test
    public void exportSearchAsExcel_shouldCreateExcelWorkbookCorrectly() throws Exception {
        String query = "DOCSTRCT:Monograph AND MD_YEARPUBLISH:19*";
        SXSSFWorkbook wb = SearchHelper.exportSearchAsExcel(query + " AND NOT(DC:forbidden)", query, Collections.singletonList(new StringPair(
                "SORT_YEARPUBLISH", "asc")), null, null, new HashMap<String, Set<String>>(), Locale.ENGLISH, false);
        String[] cellValues0 = new String[] { "Persistent identifier", "PPN728566745", "b18029048", "AC01054587", "1592397" };
        String[] cellValues1 = new String[] { "Label", "Vaterländische Handels- und Verkehrsgeographie",
                "papers communicated to the first International Eugenics Congress held at the University of London, July 24th to 30th, 1912",
                "Oberösterreich im Weltkrieg", "Fama y obras póstumas" };
        Assert.assertNotNull(wb);
        Assert.assertEquals(1, wb.getNumberOfSheets());
        SXSSFSheet sheet = wb.getSheetAt(0);
        Assert.assertEquals(6, sheet.getPhysicalNumberOfRows());
        {
            SXSSFRow row = sheet.getRow(0);
            Assert.assertEquals(2, row.getPhysicalNumberOfCells());
            Assert.assertEquals("Query:", row.getCell(0).getRichStringCellValue().toString());
            Assert.assertEquals(query, row.getCell(1).getRichStringCellValue().toString());
        }
        for (int i = 1; i < 6; ++i) {
            SXSSFRow row = sheet.getRow(i);
            Assert.assertEquals(2, row.getPhysicalNumberOfCells());
            Assert.assertEquals(cellValues0[i - 1], row.getCell(0).getRichStringCellValue().toString());
            Assert.assertEquals(cellValues1[i - 1], row.getCell(1).getRichStringCellValue().toString());
        }
    }

    /**
     * @see SearchHelper#getBrowseElement(String,int,List,Map,Set,Locale,boolean)
     * @verifies return correct hit for non-aggregated search
     */
    @Test
    public void getBrowseElement_shouldReturnCorrectHitForNonaggregatedSearch() throws Exception {
        String rawQuery = SolrConstants.IDDOC + ":*";
        List<SearchHit> hits = SearchHelper.searchWithFulltext(SearchHelper.buildFinalQuery(rawQuery, false), 0, 10, null, null, null, null, null,
                null, Locale.ENGLISH);
        Assert.assertNotNull(hits);
        Assert.assertEquals(10, hits.size());
        for (int i = 0; i < 10; ++i) {
            BrowseElement bi = SearchHelper.getBrowseElement(rawQuery, i, null, null, null, null, Locale.ENGLISH, false);
            Assert.assertEquals(hits.get(i).getBrowseElement().getIddoc(), bi.getIddoc());
        }
    }

    /**
     * @see SearchHelper#getBrowseElement(String,int,List,Map,Set,Locale,boolean)
     * @verifies return correct hit for aggregated search
     */
    @Test
    public void getBrowseElement_shouldReturnCorrectHitForAggregatedSearch() throws Exception {
        String rawQuery = SolrConstants.IDDOC + ":*";
        List<SearchHit> hits = SearchHelper.searchWithFulltext(SearchHelper.buildFinalQuery(rawQuery, true), 0, 10, null, null, null, null, null,
                null, Locale.ENGLISH);
        Assert.assertNotNull(hits);
        Assert.assertEquals(10, hits.size());
        for (int i = 0; i < 10; ++i) {
            BrowseElement bi = SearchHelper.getBrowseElement(rawQuery, i, null, null, null, null, Locale.ENGLISH, true);
            Assert.assertEquals(hits.get(i).getBrowseElement().getIddoc(), bi.getIddoc());
        }
    }

    /**
     * @see SearchHelper#applyHighlightingToPhrase(String,Set)
     * @verifies apply highlighting for all terms
     */
    @Test
    public void applyHighlightingToPhrase_shouldApplyHighlightingForAllTerms() throws Exception {
        String phrase = "FOO BAR Foo Bar foo bar";
        Set<String> terms = new HashSet<>();
        terms.add("foo");
        terms.add("bar");
        String highlightedPhrase = SearchHelper.applyHighlightingToPhrase(phrase, terms);
        Assert.assertEquals(SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "FOO" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "BAR" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "Foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "Bar" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "bar" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END, highlightedPhrase);
    }

    /**
     * @see SearchHelper#applyHighlightingToPhrase(String,Set)
     * @verifies skip single character terms
     */
    @Test
    public void applyHighlightingToPhrase_shouldSkipSingleCharacterTerms() throws Exception {
        String phrase = "FOO BAR Foo Bar foo bar";
        Set<String> terms = new HashSet<>();
        terms.add("o");
        String highlightedPhrase = SearchHelper.applyHighlightingToPhrase(phrase, terms);
        Assert.assertEquals(phrase, highlightedPhrase);
    }

    /**
     * @see SearchHelper#applyHighlightingToPhrase(String,String)
     * @verifies apply highlighting to all occurrences of term
     */
    @Test
    public void applyHighlightingToPhrase_shouldApplyHighlightingToAllOccurrencesOfTerm() throws Exception {
        String phrase = "FOO BAR Foo Bar foo bar";
        String highlightedPhrase1 = SearchHelper.applyHighlightingToPhrase(phrase, "foo");
        Assert.assertEquals(SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "FOO" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " BAR "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "Foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " Bar "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " bar", highlightedPhrase1);
        String highlightedPhrase2 = SearchHelper.applyHighlightingToPhrase(highlightedPhrase1, "bar");
        Assert.assertEquals(SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "FOO" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "BAR" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "Foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "Bar" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "bar" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END, highlightedPhrase2);
    }

    /**
     * @see SearchHelper#applyHighlightingToPhrase(String,String)
     * @verifies ignore special characters
     */
    @Test
    public void applyHighlightingToPhrase_shouldIgnoreSpecialCharacters() throws Exception {
        String phrase = "FOO BAR Foo Bar foo bar";
        String highlightedPhrase1 = SearchHelper.applyHighlightingToPhrase(phrase, "foo-bar");
        Assert.assertEquals(SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "FOO BAR" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "Foo Bar" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END + " "
                + SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "foo bar" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END, highlightedPhrase1);
    }

    /**
     * @see SearchHelper#applyHighlightingToTerm(String)
     * @verifies add span correctly
     */
    @Test
    public void applyHighlightingToTerm_shouldAddSpanCorrectly() throws Exception {
        Assert.assertEquals(SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END, SearchHelper
                .applyHighlightingToTerm("foo"));
    }

    /**
     * @see SearchHelper#replaceHighlightingPlaceholders(String)
     * @verifies replace placeholders with html tags
     */
    @Test
    public void replaceHighlightingPlaceholders_shouldReplacePlaceholdersWithHtmlTags() throws Exception {
        Assert.assertEquals("<span class=\"search-list--highlight\">foo</span>", SearchHelper.replaceHighlightingPlaceholders(
                SearchHelper.PLACEHOLDER_HIGHLIGHTING_START + "foo" + SearchHelper.PLACEHOLDER_HIGHLIGHTING_END));
    }
}