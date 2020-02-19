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
package io.goobi.viewer.servlets.rest.iiif.presentation;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import de.intranda.api.iiif.presentation.AbstractPresentationModelElement;
import de.intranda.api.iiif.search.AutoSuggestResult;
import de.intranda.api.iiif.search.SearchResult;

/**
 * <p>
 * IIIFPresentationResponseFilter class.
 * </p>
 *
 * @author Florian Alpers
 *
 *         Adds the @context property to all IIIF Presentation responses in the topmost json element
 */
@Provider
@IIIFPresentationBinding
public class IIIFPresentationResponseFilter implements ContainerResponseFilter {

    /** Constant <code>CONTEXT="http://iiif.io/api/presentation/2/conte"{trunked}</code> */
    public static final String CONTEXT = "http://iiif.io/api/presentation/2/context.json";
    /** Constant <code>CONTEXT_SEARCH="http://iiif.io/api/search/1/context.jso"{trunked}</code> */
    public static final String CONTEXT_SEARCH = "http://iiif.io/api/search/1/context.json";

    /* (non-Javadoc)
     * @see javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container.ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
     */
    /** {@inheritDoc} */
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {

        Object responseObject = response.getEntity();
        if (responseObject != null && responseObject instanceof AbstractPresentationModelElement) {
            AbstractPresentationModelElement element = (AbstractPresentationModelElement) responseObject;
            setResponseCharset(response, "UTF-8");
            element.setContext(CONTEXT);
        } else if (responseObject != null && responseObject instanceof SearchResult) {
            SearchResult element = (SearchResult) responseObject;
            setResponseCharset(response, "UTF-8");
            element.addContext(CONTEXT);
            if (!element.getHits().isEmpty()) {
                element.addContext(CONTEXT_SEARCH);
            }
        } else if (responseObject != null && responseObject instanceof AutoSuggestResult) {
            AutoSuggestResult element = (AutoSuggestResult) responseObject;
            setResponseCharset(response, "UTF-8");
            element.addContext(CONTEXT_SEARCH);
        }

    }

    /**
     * <p>
     * setResponseCharset.
     * </p>
     *
     * @param response a {@link javax.ws.rs.container.ContainerResponseContext} object.
     * @param charset a {@link java.lang.String} object.
     */
    public void setResponseCharset(ContainerResponseContext response, String charset) {
        String contentType = response.getHeaderString("Content-Type") + ";charset=" + charset;
        response.getHeaders().remove("Content-Type");
        response.getHeaders().add("Content-Type", contentType);
    }

}
