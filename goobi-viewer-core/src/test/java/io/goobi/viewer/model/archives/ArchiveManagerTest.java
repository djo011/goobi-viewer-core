/*
 * This file is part of the Goobi viewer - a content presentation and management
 * application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goobi.viewer.model.archives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.goobi.viewer.AbstractSolrEnabledTest;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;

class ArchiveManagerTest extends AbstractSolrEnabledTest {

    SolrEADParser eadParser;
    List<ArchiveResource> possibleDatabases;

    @BeforeEach
    void before() {
        try {
            SolrEADParser tempParser = new SolrEADParser(DataManager.getInstance().getSearchIndex());
            tempParser.readConfiguration(DataManager.getInstance().getConfiguration().getArchiveMetadataForTemplate(""));
            ArchiveResource resource = new ArchiveResource("", "", "", "", "");
            ArchiveEntry root = tempParser.loadDatabase(resource);

            possibleDatabases = new ArrayList<>();
            possibleDatabases.add(new ArchiveResource("database 1", "resource 1", "r1",
                    ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.systemDefault()).format(ArchiveResource.DATE_TIME_FORMATTER), "10"));
            possibleDatabases
                    .add(new ArchiveResource("database 1", "resource 2", "r2", ZonedDateTime.now().format(ArchiveResource.DATE_TIME_FORMATTER),
                            "10"));

            eadParser = new SolrEADParser(DataManager.getInstance().getSearchIndex()) {
                public List<ArchiveResource> getPossibleDatabases() {
                    return possibleDatabases;
                }

                public ArchiveEntry loadDatabase(ArchiveResource database) {
                    return root;
                }
            };

        } catch (PresentationException | IndexUnreachableException | ConfigurationException e) {
            fail(e.toString());
        }
    }

    @Test
    @Disabled("Test index contains no archives")
    void testGetDatabases() {
        ArchiveManager archiveManager = new ArchiveManager(eadParser, null);
        assertEquals(2, archiveManager.getDatabases().size());
    }

    @Test
    @Disabled("Test index contains no archives")
    void testGetDatabase() throws Exception {
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            ArchiveTree tree = archiveManager.getArchiveTree("database 1", "r1");
            assertNotNull(tree);
        }
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            ArchiveTree tree = archiveManager.getArchiveTree("database 1", "r2");
            assertNotNull(tree);
        }
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            ArchiveTree tree = archiveManager.getArchiveTree("database 1", "r3");
            assertNull(tree);
        }
    }

    @Test
    @Disabled("Test index contains no archives")
    void testUpdateDatabase() throws Exception {
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            archiveManager.getArchiveTree("database 1", "r1");
            archiveManager.getArchiveTree("database 1", "r1");
            Mockito.verify(archiveManager, Mockito.times(1)).loadDatabase(Mockito.any(), Mockito.any());
        }
        {
            //            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            //            archiveManager.getArchiveTree("database 1", "resource 2");
            //            archiveManager.getArchiveTree("database 1", "resource 2");
            //            Mockito.verify(archiveManager, Mockito.times(2)).loadDatabase(Mockito.any(), Mockito.any());
        }
    }

    @Test
    @Disabled("Test index contains no archives")
    void testAddNewArchive() {
        ArchiveManager archiveManager = new ArchiveManager(eadParser, null);

        ArchiveResource newArchive = new ArchiveResource("database 1", "resource 3", "r3",
                ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.systemDefault()).format(ArchiveResource.DATE_TIME_FORMATTER), "10");
        possibleDatabases.add(newArchive);
        assertNull(archiveManager.getArchive("database 1", "r3"));
        archiveManager.updateArchiveList();
        assertNotNull(archiveManager.getArchive("database 1", "r3"));
    }

    @Test
    @Disabled("Test index contains no archives")
    void testRemoveArchive() {
        ArchiveManager archiveManager = new ArchiveManager(eadParser, null);
        possibleDatabases.remove(1);
        assertNotNull(archiveManager.getArchive("database 1", "r2"));
        archiveManager.updateArchiveList();
        assertNull(archiveManager.getArchive("database 1", "r2"));
    }
}
