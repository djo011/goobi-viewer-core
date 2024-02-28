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

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.goobi.viewer.AbstractTest;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.controller.XmlTools;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;

class ArchiveManagerTest extends AbstractTest {

    BasexEADParser eadParser;
    List<ArchiveResource> possibleDatabases;

    @BeforeEach
    void before() {
        try {
            Document doc = XmlTools.readXmlFile("src/test/resources/data/EAD_Export_Tektonik.XML");
            BasexEADParser tempParser = new BasexEADParser(null, null);
            tempParser.readConfiguration(DataManager.getInstance().getConfiguration().getArchiveMetadataConfig());
            ArchiveEntry root = tempParser.parseEadFile(doc);

            possibleDatabases = new ArrayList<>();
            possibleDatabases.add(new ArchiveResource("database 1", "resource 1", "r1",
                    ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.systemDefault()).format(ArchiveResource.DATE_TIME_FORMATTER), "10"));
            possibleDatabases
                    .add(new ArchiveResource("database 1", "resource 2", "r2", ZonedDateTime.now().format(ArchiveResource.DATE_TIME_FORMATTER),
                            "10"));

            eadParser = new BasexEADParser(null, null) {
                public List<ArchiveResource> getPossibleDatabases() {
                    return possibleDatabases;
                }

                public ArchiveEntry loadDatabase(ArchiveResource database) {
                    return root;
                }
            };

        } catch (IOException | JDOMException | PresentationException | IndexUnreachableException | ConfigurationException e) {
            fail(e.toString());
        }
    }

    @Test
    void testGetDatabases() {
        ArchiveManager archiveManager = new ArchiveManager(eadParser, null);
        assertEquals(2, archiveManager.getDatabases().size());
    }

    @Test
    void testGetDatabase() throws Exception {
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            ArchiveTree tree = archiveManager.getArchiveTree("database 1", "resource 1");
            assertNotNull(tree);
        }
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            ArchiveTree tree = archiveManager.getArchiveTree("database 1", "resource 2");
            assertNotNull(tree);
        }
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            ArchiveTree tree = archiveManager.getArchiveTree("database 1", "resource 3");
            assertNull(tree);
        }
    }

    @Test
    void testUpdateDatabase() throws Exception {
        {
            ArchiveManager archiveManager = Mockito.spy(new ArchiveManager(eadParser, null));
            archiveManager.getArchiveTree("database 1", "resource 1");
            archiveManager.getArchiveTree("database 1", "resource 1");
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
    void testAddNewArchive() {
        ArchiveManager archiveManager = new ArchiveManager(eadParser, null);

        ArchiveResource newArchive = new ArchiveResource("database 1", "resource 3", "r3",
                ZonedDateTime.of(2000, 1, 1, 1, 1, 1, 1, ZoneOffset.systemDefault()).format(ArchiveResource.DATE_TIME_FORMATTER), "10");
        possibleDatabases.add(newArchive);
        assertNull(archiveManager.getArchive("database 1", "resource 3"));
        archiveManager.updateArchiveList();
        assertNotNull(archiveManager.getArchive("database 1", "resource 3"));

    }

    @Test
    void testRemoveArchive() {
        ArchiveManager archiveManager = new ArchiveManager(eadParser, null);
        possibleDatabases.remove(1);
        assertNotNull(archiveManager.getArchive("database 1", "resource 2"));
        archiveManager.updateArchiveList();
        assertNull(archiveManager.getArchive("database 1", "resource 2"));

    }

}
