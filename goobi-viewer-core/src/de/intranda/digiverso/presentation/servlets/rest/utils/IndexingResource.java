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
package de.intranda.digiverso.presentation.servlets.rest.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.intranda.digiverso.presentation.controller.DataManager;
import de.intranda.digiverso.presentation.controller.Helper;
import de.intranda.digiverso.presentation.controller.SolrConstants;
import de.intranda.digiverso.presentation.exceptions.IndexUnreachableException;
import de.intranda.digiverso.presentation.exceptions.PresentationException;
import de.intranda.digiverso.presentation.messages.ViewerResourceBundle;
import de.intranda.digiverso.presentation.servlets.rest.ViewerRestServiceBinding;

/**
 * Resource for index operations.
 */
@Path(IndexingResource.RESOURCE_PATH)
@ViewerRestServiceBinding
public class IndexingResource {

    private static final Logger logger = LoggerFactory.getLogger(IndexingResource.class);

    public static final String RESOURCE_PATH = "/index";

    private static Thread workerThread = null;

    @Context
    private HttpServletRequest servletRequest;
    @Context
    private HttpServletResponse servletResponse;

    public IndexingResource() {
    }

    /**
     * For testing
     * 
     * @param request
     */
    protected IndexingResource(HttpServletRequest request) {
        this.servletRequest = request;
    }

    /**
     * @param outputPath Output path for sitemap files
     * @param firstPageOnly
     * @param params
     * @return Short summary of files created
     */
    @SuppressWarnings("unchecked")
    @POST
    @Path("/deleterecord")
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON })
    public String deleteRecord(IndexingRequestParameters params) {
        if (servletResponse != null) {
            servletResponse.addHeader("Access-Control-Allow-Origin", "*");
        }

        JSONObject ret = new JSONObject();

        if (params == null || StringUtils.isEmpty(params.getPi())) {
            ret.put("status", HttpServletResponse.SC_BAD_REQUEST);
            ret.put("message", "Invalid JSON request object");
            return ret.toJSONString();
        }

        if (workerThread == null || !workerThread.isAlive()) {
            workerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (DataManager.getInstance().getSearchIndex().getHitCount(SolrConstants.PI_PARENT + ":" + params.getPi()) > 0) {
                            ret.put("status", HttpServletResponse.SC_FORBIDDEN);
                            ret.put("message", ViewerResourceBundle.getTranslation("deleteRecord_failure_volumes_present", null));
                        }
                        if (Helper.deleteRecord(params.getPi(), params.isCreateTraceDocument())) {
                            ret.put("status", HttpServletResponse.SC_OK);
                            ret.put("message", ViewerResourceBundle.getTranslation("deleteRecord_success", null));
                        } else {
                            ret.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            ret.put("message", ViewerResourceBundle.getTranslation("deleteRecord_failure", null));
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                        ret.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        ret.put("message", e.getMessage());
                    } catch (IndexUnreachableException e) {
                        logger.debug("IndexUnreachableException thrown here: {}", e.getMessage());
                        ret.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        ret.put("message", e.getMessage());
                    } catch (PresentationException e) {
                        logger.debug("PresentationException thrown here: {}", e.getMessage());
                        ret.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        ret.put("message", e.getMessage());
                    }
                }
            });

            workerThread.start();
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            ret.put("status", HttpServletResponse.SC_FORBIDDEN);
            ret.put("message", "Record deletion currently in progress");
        }

        return ret.toJSONString();
    }
}
