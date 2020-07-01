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
package io.goobi.viewer.api.rest.v1.index;

import static io.goobi.viewer.api.rest.v1.ApiUrls.RECORDS_INDEX;
import static io.goobi.viewer.api.rest.v1.ApiUrls.RECORDS_QUERY;
import static io.goobi.viewer.api.rest.v1.ApiUrls.RECORDS_STATISTICS;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.unigoettingen.sub.commons.contentlib.exceptions.ContentNotFoundException;
import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ServiceNotAllowedException;
import de.unigoettingen.sub.commons.contentlib.servlet.rest.CORSBinding;
import io.goobi.viewer.api.rest.ViewerRestServiceBinding;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.controller.JsonTools;
import io.goobi.viewer.controller.SolrSearchIndex;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;
import io.goobi.viewer.exceptions.ViewerConfigurationException;
import io.goobi.viewer.model.search.SearchHelper;
import io.goobi.viewer.model.viewer.StringPair;
import io.goobi.viewer.servlets.rest.content.RecordsRequestParameters;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * @author florian
 *
 */
@Path(RECORDS_INDEX)
@CORSBinding
@ViewerRestServiceBinding
public class IndexResource {

    private static final Logger logger = LoggerFactory.getLogger(IndexResource.class);

    @Context
    private HttpServletRequest servletRequest;
    @Context
    private HttpServletResponse servletResponse;

    
    @GET
    @Path(RECORDS_STATISTICS)
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(
            tags = { "index" },
            summary = "Statistics about indexed records")
    public String getStatistics(
            @Parameter(description="SOLR Query to filter results (optional)") @QueryParam("query") String query) throws MalformedURLException, ContentNotFoundException,
            ServiceNotAllowedException, IndexUnreachableException, PresentationException, ViewerConfigurationException, DAOException,
            IllegalRequestException {
        
        if(query == null) {
            query = "+(ISWORK:*) ";
        }
        
        String finalQuery =
                new StringBuilder().append(query).append(SearchHelper.getAllSuffixes(servletRequest, null, true, true, false)).toString();
        logger.debug("q: {}", finalQuery);
        long count = DataManager.getInstance().getSearchIndex().search(query, 0, 0, null, null, null).getResults().getNumFound();
        JSONObject json = new JSONObject();
        json.put("count", count);
        return json.toString();
    }
    
    @POST
    @Path(RECORDS_QUERY)
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(
            tags = { "index" },
            summary = "Post a query directly the SOLR index")
    @ApiResponse(responseCode = "406", description = "Illegal query or query parameters")
    public String getRecordsForQuery(RecordsRequestParameters params) throws MalformedURLException, ContentNotFoundException,
            ServiceNotAllowedException, IndexUnreachableException, PresentationException, ViewerConfigurationException, DAOException,
            IllegalRequestException {
        JSONObject ret = new JSONObject();
        if (params == null || params.query == null) {
            ret.put("status", HttpServletResponse.SC_BAD_REQUEST);
            ret.put("message", "Invalid JSON request object");
            return ret.toString();
        }
        
        // Custom query does not filter by the sub-theme discriminator value by default, it has to be added to the custom query via #{navigationHelper.subThemeDiscriminatorValueSubQuery}
//        String query =
//                new StringBuilder().append("+").append(params.getQuery()).append(SearchHelper.getAllSuffixes(servletRequest, null, true, true, false)).toString();
        String query = SearchHelper.buildFinalQuery(params.query, params.includeChildHits);

        logger.trace("query: {}", query);

        int count = params.count;
        if (count <= 0) {
            count = SolrSearchIndex.MAX_HITS;
        }

        List<StringPair> sortFieldList = new ArrayList<>();
        for (String sortField : params.sortFields) {
            if (StringUtils.isNotEmpty(sortField)) {
                sortFieldList.add(new StringPair(sortField, params.sortOrder));
            }
        }
        if (params.randomize) {
            sortFieldList.clear();
            // Solr supports dynamic random_* sorting fields. Each value represents one particular order, so a random number is required.
            Random random = new Random();
            sortFieldList.add(new StringPair("random_" + random.nextInt(Integer.MAX_VALUE), ("desc".equals(params.sortOrder) ? "desc" : "asc")));
        }
        try {
            List<String> fieldList = params.resultFields;
            
            Map<String, String> paramMap = new HashMap<>();
                        QueryResponse response = DataManager.getInstance().getSearchIndex().search(query, params.offset, count, sortFieldList, null, fieldList );
//            QueryResponse response = DataManager.getInstance().getSearchIndex().search(query, params.offset, count, sortFieldList, null, fieldList, null, paramMap );

            SolrDocumentList result = response.getResults();
            Map<String, SolrDocumentList> expanded = response.getExpandedResults();
            logger.trace("hits: {}", result.size());
            JSONArray jsonArray = null;
            if (params.jsonFormat != null) {
                switch (params.jsonFormat) {
                    case "datecentric":
                        jsonArray = JsonTools.getDateCentricRecordJsonArray(result, servletRequest);
                        break;
                    default:
                        jsonArray = JsonTools.getRecordJsonArray(result, expanded, servletRequest, params.language);
                        break;
                }
            } else {
                jsonArray = JsonTools.getRecordJsonArray(result, expanded, servletRequest, params.language);
            }
            if (jsonArray == null) {
                jsonArray = new JSONArray();
            }

            return jsonArray.toString();
        } catch (PresentationException e) {
            throw new IllegalRequestException(e.getMessage());
        }
    }
}
