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
package io.goobi.viewer.servlets.rest.iiif.discovery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import de.intranda.api.iiif.discovery.Activity;
import de.intranda.api.iiif.discovery.OrderedCollection;
import de.intranda.api.iiif.discovery.OrderedCollectionPage;
import io.goobi.viewer.api.rest.ViewerRestServiceBinding;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.api.rest.v1.ApiUrlManager;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;
import io.goobi.viewer.model.iiif.discovery.ActivityCollectionBuilder;

/**
 * Provides REST services according to the IIIF discovery API specfication (https://iiif.io/api/discovery/0.1/). This class implements two resources:
 * <ul>
 * <li>{@link #getAllChanges() /iiif/discovery/activities/}</li>
 * <li>{@link #getPage(int) /iiif/discovery/activities/&lt;pageNo&gt;/}</li>
 * </ul>
 *
 * This service supports activity types UPDATE, CREATE and DELETE. They are created from the SOLR fields DATEUPDATED, DATECREATED AND DATEDELETED
 * respectively
 *
 * @author Florian Alpers
 */
@Path("/iiif/discovery")
@ViewerRestServiceBinding
public class DiscoveryResource {

    private final static String[] CONTEXT = { "http://iiif.io/api/discovery/0/context.json", "https://www.w3.org/ns/activitystreams" };

    @Context
    protected HttpServletRequest servletRequest;
    @Context
    protected HttpServletResponse servletResponse;

    /**
     * Provides a view of the entire list of all activities by linking to the first and last page of the collection. The pages contain the actual
     * activity entries and are provided by {@link #getPage(int) /iiif/discovery/activities/&lt;pageNo&gt;/}. This resource also contains a count of
     * the total number of activities
     *
     * @return An {@link de.intranda.api.iiif.discovery.OrderedCollection} of {@link Activity Activities}
     * @throws io.goobi.viewer.exceptions.PresentationException if any.
     * @throws io.goobi.viewer.exceptions.IndexUnreachableException if any.
     */
    @GET
    @Path("/activities")
    @Produces({ MediaType.APPLICATION_JSON })
    public OrderedCollection<Activity> getAllChanges() throws PresentationException, IndexUnreachableException {
        ActivityCollectionBuilder builder = new ActivityCollectionBuilder(new ApiUrlManager(DataManager.getInstance().getConfiguration().getRestApiUrl()));;
        OrderedCollection<Activity> collection = builder.buildCollection();
        collection.setContext(CONTEXT);
        return collection;
    }

    /**
     * Provides a partial list of {@link Activity Activities} along with links to the preceding and succeeding page as well as the parent collection
     * as provided by {@link #getAllChanges() /iiif/discovery/activities/} The number of Activities on the page is determined by
     * {@link io.goobi.viewer.controller.Configuration#getIIIFDiscoveryAvtivitiesPerPage() Configuration#getIIIFDiscoveryAvtivitiesPerPage()}
     *
     * @param pageNo The page number, starting with 0
     * @return An {@link de.intranda.api.iiif.discovery.OrderedCollectionPage} of {@link Activity Activities}
     * @throws io.goobi.viewer.exceptions.PresentationException if any.
     * @throws io.goobi.viewer.exceptions.IndexUnreachableException if any.
     */
    @GET
    @Path("/activities/{pageNo}")
    @Produces({ MediaType.APPLICATION_JSON })
    public OrderedCollectionPage<Activity> getPage(@PathParam("pageNo") int pageNo) throws PresentationException, IndexUnreachableException {
        ActivityCollectionBuilder builder = new ActivityCollectionBuilder(new ApiUrlManager(DataManager.getInstance().getConfiguration().getRestApiUrl()));
        OrderedCollectionPage<Activity> page = builder.buildPage(pageNo);
        page.setContext(CONTEXT);
        return page;
    }
}
