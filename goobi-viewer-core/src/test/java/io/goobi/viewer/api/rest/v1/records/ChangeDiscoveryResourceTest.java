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
package io.goobi.viewer.api.rest.v1.records;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.intranda.api.iiif.discovery.Activity;
import de.intranda.api.iiif.discovery.OrderedCollection;
import de.intranda.api.iiif.discovery.OrderedCollectionPage;
import io.goobi.viewer.api.rest.AbstractRestApiTest;
import io.goobi.viewer.api.rest.v1.ApiUrls;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.model.rss.Channel;

import static io.goobi.viewer.api.rest.v1.ApiUrls.*;

/**
 * @author florian
 *
 */
public class ChangeDiscoveryResourceTest extends AbstractRestApiTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetChanges() throws JsonMappingException, JsonProcessingException {
        try(Response response = target(urls.path(RECORDS_CHANGES).build())
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals("Should return status 200", 200, response.getStatus());
            assertNotNull("Should return user object as json", response.getEntity());
            String entity = response.readEntity(String.class);
            OrderedCollection<Activity> activities = new OrderedCollection<>();
            activities = mapper.readValue(entity, activities.getClass());
            assertTrue(activities.getTotalItems() > 0);
        }
    }
    
    @Test
    public void testGetChangesPage() throws JsonMappingException, JsonProcessingException {
        try(Response response = target(urls.path(RECORDS_CHANGES, RECORDS_CHANGES_PAGE).params(0).build())
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals("Should return status 200", 200, response.getStatus());
            assertNotNull("Should return user object as json", response.getEntity());
            String entity = response.readEntity(String.class);
            OrderedCollectionPage<Activity> activities = new OrderedCollectionPage<>();
            activities = mapper.readValue(entity, activities.getClass());
            assertTrue(activities.getOrderedItems().size() == DataManager.getInstance().getConfiguration().getIIIFDiscoveryAvtivitiesPerPage());
        }
    }

}
