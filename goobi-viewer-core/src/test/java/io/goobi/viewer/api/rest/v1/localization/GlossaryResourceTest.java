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
package io.goobi.viewer.api.rest.v1.localization;

import static io.goobi.viewer.api.rest.v1.ApiUrls.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.goobi.viewer.api.rest.AbstractRestApiTest;
import io.goobi.viewer.model.glossary.Glossary;

/**
 * @author florian
 *
 */
public class GlossaryResourceTest extends AbstractRestApiTest{

    @Test
    public void testGetGlossaries() throws JsonMappingException, JsonProcessingException {
        try(Response response = target(urls.path(LOCALIZATION, LOCALIZATION_VOCABS).build())
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals("Should return status 200", 200, response.getStatus());
            String entity = response.readEntity(String.class);
            List<Glossary> glossaries = new ArrayList<>();
            glossaries = mapper.readValue(entity, glossaries.getClass());
            assertNotNull(glossaries);
            assertEquals(2, glossaries.size());
            
        }
    }
    
    @Test
    public void testGetGlossaryFile() throws JsonMappingException, JsonProcessingException {
        try(Response response = target(urls.path(LOCALIZATION, LOCALIZATION_VOCABS_FILE).params("wiener.json").build())
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get()) {
            assertEquals("Should return status 200", 200, response.getStatus());
            String entity = response.readEntity(String.class);
            assertNotNull(entity);
            
        }
    }

}
