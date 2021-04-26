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
package io.goobi.viewer.api.rest.v1;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.intranda.monitoring.timer.TimingStatistics;
import io.goobi.viewer.api.rest.AbstractApiUrlManager;
import io.goobi.viewer.api.rest.AbstractApiUrlManager.ApiInfo;
import io.goobi.viewer.controller.DataManager;

/**
 * @author florian
 *
 */
@Path("/")
public class ApplicationResource {

    @Inject
    ApiUrls urls;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ApiInfo getApiInfo() {
        ApiInfo info = new ApiInfo("Goobi viewer REST API", "v1", urls.getApiUrl() + "/openapi.json");
        return info;
    }
    
    @GET
    @Path("timing")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTimeAnalysis() {
        List<TimingStatistics> times = DataManager.getInstance().getTiming().geStatistics();
        DataManager.getInstance().resetTiming();
        return times.stream().map(TimingStatistics::toString).collect(Collectors.joining("\n\n"));
    }
}
