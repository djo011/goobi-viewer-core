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
package io.goobi.viewer.managedbeans;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.intranda.api.annotation.ITypedResource;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.controller.SolrConstants;
import io.goobi.viewer.controller.StringTools;
import io.goobi.viewer.controller.SolrConstants.DocType;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.model.crowdsourcing.DisplayUserGeneratedContent;
import io.goobi.viewer.model.crowdsourcing.DisplayUserGeneratedContent.ContentType;
import io.goobi.viewer.model.security.AccessConditionUtils;
import io.goobi.viewer.model.security.IPrivilegeHolder;
import io.goobi.viewer.model.viewer.PhysicalElement;

/**
 * Supplies additional content for records (such contents produced by the crowdsourcing module).
 */
@Named
@SessionScoped
public class ContentBean implements Serializable {

    private static final long serialVersionUID = 3811544515374503924L;

    private static final Logger logger = LoggerFactory.getLogger(ContentBean.class);

    /**
     * PI for which {@link #userGeneratedContentsForDisplay} is loaded
     */
    private String pi;
    /** User generated contents to display on this page. */
    private List<DisplayUserGeneratedContent> userGeneratedContentsForDisplay;

    /**
     * Empty Constructor.
     */
    public ContentBean() {
        // the emptiness inside
    }

    /**
     * <p>
     * init.
     * </p>
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Resets loaded content. Use when logging in/out.
     */
    public void resetContentList() {
        logger.trace("resetContentList");
        userGeneratedContentsForDisplay = null;
    }

    /**
     * <p>
     * Getter for the field <code>userGeneratedContentsForDisplay</code>.
     * </p>
     *
     * @return User-generated contents for the given page
     * @param page a {@link io.goobi.viewer.model.viewer.PhysicalElement} object.
     * @throws io.goobi.viewer.exceptions.PresentationException if any.
     * @throws io.goobi.viewer.exceptions.IndexUnreachableException if any.
     * @throws DAOException
     */
    public List<DisplayUserGeneratedContent> getUserGeneratedContentsForDisplay(String pi)
            throws PresentationException, IndexUnreachableException, DAOException {
        // logger.trace("getUserGeneratedContentsForDisplay");
        if (pi != null && (userGeneratedContentsForDisplay == null || !pi.equals(this.pi))) {
            loadUserGeneratedContentsForDisplay(pi, BeanUtils.getRequest());
        }
        if (userGeneratedContentsForDisplay != null && userGeneratedContentsForDisplay.size() > 0) {
            return userGeneratedContentsForDisplay;
        }

        return Collections.emptyList();
    }

    /**
     * @param page
     * @return
     * @throws IndexUnreachableException
     * @throws PresentationException
     * @throws DAOException
     */
    private List<DisplayUserGeneratedContent> getUserGeneratedContentsForDisplay(PhysicalElement page)
            throws PresentationException, IndexUnreachableException, DAOException {
        return getUserGeneratedContentsForDisplay(page.getPi()).stream().filter(ugc -> ugc.isOnThisPage(page)).collect(Collectors.toList());
    }

    /**
     * <p>
     * loadUserGeneratedContentsForDisplay.
     * </p>
     *
     * @param pi Record identifier
     * @param request
     * @throws io.goobi.viewer.exceptions.PresentationException if any.
     * @throws io.goobi.viewer.exceptions.IndexUnreachableException if any.
     * @throws DAOException
     */
    public void loadUserGeneratedContentsForDisplay(String pi, HttpServletRequest request)
            throws PresentationException, IndexUnreachableException, DAOException {
        logger.trace("loadUserGeneratedContentsForDisplay");
        if (pi == null) {
            logger.debug("pi is null, cannot load");
            return;
        }
        this.pi = pi;
        userGeneratedContentsForDisplay = new ArrayList<>();
        List<DisplayUserGeneratedContent> allContent =
                DataManager.getInstance().getSearchIndex().getDisplayUserGeneratedContentsForRecord(pi);
        for (DisplayUserGeneratedContent ugcContent : allContent) {
            // Do not add empty comments
            if (ugcContent.isEmpty()) {
                continue;
            }
            if (ugcContent.getAccessCondition() != null) {
                logger.trace("UGC access condition: {}", ugcContent.getAccessCondition());
                String query = "+" + SolrConstants.PI_TOPSTRUCT + ":" + pi + " +" + SolrConstants.DOCTYPE + ":" + DocType.UGC.name();
                if (!AccessConditionUtils.checkAccessPermission(Collections.singleton(ugcContent.getAccessCondition()),
                        IPrivilegeHolder.PRIV_VIEW_UGC, query, request)) {
                    logger.trace("User may not view UGC {}", ugcContent.getId());
                    continue;
                }
            }
            userGeneratedContentsForDisplay.add(ugcContent);
        }
        logger.trace("Loaded {} user generated contents for pi {}", userGeneratedContentsForDisplay.size(), this.pi);
    }

    /**
     * <p>
     * getCurrentUGCCoords.
     * </p>
     *
     * @param page a {@link io.goobi.viewer.model.viewer.PhysicalElement} object.
     * @return a {@link java.util.List} object.
     * @throws io.goobi.viewer.exceptions.IndexUnreachableException if any.
     * @throws io.goobi.viewer.exceptions.PresentationException if any.
     * @throws DAOException
     */
    public List<List<String>> getCurrentUGCCoords(PhysicalElement page) throws IndexUnreachableException, PresentationException, DAOException {
        List<DisplayUserGeneratedContent> currentContents;
        currentContents = getUserGeneratedContentsForDisplay(page);
        if (currentContents == null) {
            return Collections.emptyList();
        }

        List<List<String>> coords = new ArrayList<>(currentContents.size());
        for (DisplayUserGeneratedContent content : currentContents) {
            if (content.hasArea()) {
                String rect = StringTools.normalizeWebAnnotationCoordinates(content.getAreaString());

                String text = content.getLabel();
                text = text.replaceAll("[\r\n]+", "<br/>");
                rect += (",\"" + text + "\"");
                rect += (",\"" + content.getId() + "\"");
                coords.add(Arrays.asList(rect.split(",")));
            }
        }
        logger.trace("getting ugc coordinates {}", coords);
        return coords;
    }

    /**
     * Removes script tags from the given string.
     *
     * @param value a {@link java.lang.String} object.
     * @return value sans any script tags
     */
    public String cleanUpValue(String value) {
        return StringTools.stripJS(value);
    }

    public String getEscapedBodyUrl(DisplayUserGeneratedContent content) {
        return Optional.ofNullable(content)
                .map(DisplayUserGeneratedContent::getAnnotationBody)
                .map(ITypedResource::getId)
                .map(URI::toString)
                .map(BeanUtils::escapeCriticalUrlChracters)
                .orElse("");
    }

    /**
     * @param persistentIdentifier
     * @return
     * @throws IndexUnreachableException
     * @throws PresentationException
     * @throws DAOException
     */
    public boolean hasGeoCoordinateAnnotations(String persistentIdentifier) throws PresentationException, IndexUnreachableException, DAOException {
        return getUserGeneratedContentsForDisplay(persistentIdentifier)
                .stream()
                .anyMatch(ugc -> ContentType.GEOLOCATION.equals(ugc.getType()));
    }
}
