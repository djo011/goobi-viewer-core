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
package de.intranda.digiverso.presentation.servlets.rest.iiif.presentation;

import static org.junit.Assert.fail;

import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.intranda.digiverso.presentation.AbstractDatabaseAndSolrEnabledTest;
import de.intranda.digiverso.presentation.AbstractSolrEnabledTest;
import de.intranda.digiverso.presentation.exceptions.DAOException;
import de.intranda.digiverso.presentation.exceptions.IndexUnreachableException;
import de.intranda.digiverso.presentation.exceptions.PresentationException;
import de.intranda.digiverso.presentation.model.iiif.presentation.IPresentationModelElement;
import de.intranda.digiverso.presentation.model.iiif.presentation.Manifest;
import de.intranda.digiverso.presentation.model.iiif.presentation.Range;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;

/**
 * @author Florian Alpers
 *
 */
public class ManifestResourceTest extends AbstractDatabaseAndSolrEnabledTest{
    
    private ManifestResource resource;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        resource = new ManifestResource(request, response);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link de.intranda.digiverso.presentation.servlets.rest.iiif.presentation.ManifestResource#getManifest(java.lang.String)}.
     * @throws DAOException 
     * @throws URISyntaxException 
     * @throws IndexUnreachableException 
     * @throws PresentationException 
     * @throws ContentNotFoundException 
     * @throws ConfigurationException 
     */
    @Test
    public void testGetManifest() throws ConfigurationException, ContentNotFoundException, PresentationException, IndexUnreachableException, URISyntaxException, DAOException {
        IPresentationModelElement manifest = resource.getManifest("PPN517154005");
        Assert.assertTrue(manifest instanceof Manifest);
        System.out.println(manifest);
    }

    /**
     * Test method for {@link de.intranda.digiverso.presentation.servlets.rest.iiif.presentation.ManifestResource#getRange(java.lang.String, java.lang.String)}.
     * @throws DAOException 
     * @throws URISyntaxException 
     * @throws IndexUnreachableException 
     * @throws PresentationException 
     * @throws ContentNotFoundException 
     * @throws ConfigurationException 
     */
//    @Test
    public void testGetRange() throws ConfigurationException, ContentNotFoundException, PresentationException, IndexUnreachableException, URISyntaxException, DAOException {
        Range range = resource.getRange("PPN517154005", "LOG_0003");
        Assert.assertTrue(range instanceof Range);
        System.out.println(range);
    }

    /**
     * Test method for {@link de.intranda.digiverso.presentation.servlets.rest.iiif.presentation.ManifestResource#getCanvas(java.lang.String, int)}.
     */
//    @Test
    public void testGetCanvas() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.intranda.digiverso.presentation.servlets.rest.iiif.presentation.ManifestResource#getOtherContent(java.lang.String, int, java.lang.String)}.
     */
//    @Test
    public void testGetOtherContent() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link de.intranda.digiverso.presentation.servlets.rest.iiif.presentation.ManifestResource#getLayer(java.lang.String, java.lang.String)}.
     */
//    @Test
    public void testGetLayer() {
        fail("Not yet implemented");
    }

}
