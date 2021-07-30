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
package io.goobi.viewer.model.citation;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;
import io.goobi.viewer.model.viewer.ViewManager;
import io.goobi.viewer.solr.SolrConstants;
import io.goobi.viewer.solr.SolrConstants.DocType;

/**
 * 
 */
public class CitationLink {

    public enum CitationLinkType {
        URL,
        INTERNAL;

        /**
         * 
         * @param name
         * @return
         */
        protected static CitationLinkType getByName(String name) {
            if (name == null) {
                return null;
            }

            for (CitationLinkType type : CitationLinkType.values()) {
                if (type.name().equals(name.toUpperCase())) {
                    return type;
                }
            }

            return null;
        }

    }

    public enum CitationLinkLevel {
        RECORD,
        DOCSTRUCT,
        IMAGE;

        /**
         * 
         * @param name
         * @return
         */
        public static CitationLinkLevel getByName(String name) {
            if (name == null) {
                return null;
            }

            for (CitationLinkLevel level : CitationLinkLevel.values()) {
                if (level.name().equals(name.toUpperCase())) {
                    return level;
                }
            }

            return null;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(CitationLink.class);

    private final CitationLinkType type;
    private final CitationLinkLevel level;
    private final String label;
    private String field;
    private String value;
    private String prefix;
    private String suffix;
    private boolean appendImageNumberToSuffix = false;

    /**
     * 
     * @param type
     * @param level
     * @param label
     */
    public CitationLink(String type, String level, String label) {
        this.type = CitationLinkType.getByName(type);
        if (this.type == null) {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        this.level = CitationLinkLevel.getByName(level);
        if (this.level == null) {
            throw new IllegalArgumentException("Unknown level: " + level);
        }
        this.label = label;
    }

    /**
     * 
     * @return
     * @throws IndexUnreachableException
     * @throws PresentationException
     * @throws DAOException
     * @should construct internal record url correctly
     * @should construct internal docstruct url correctly
     * @should construct internal image url correctly
     * @should construct external url correctly
     */
    public String getUrl(ViewManager viewManager) throws PresentationException, IndexUnreachableException, DAOException {
        logger.trace("getUrl: {}/{}", level, field);
        if (viewManager == null) {
            return null;
        }

        if (CitationLinkType.INTERNAL.equals(type)) {
            switch (level) {
                case RECORD:
                    return viewManager.getCiteLinkWork();
                case DOCSTRUCT:
                    return viewManager.getCiteLinkDocstruct();
                case IMAGE:
                    return viewManager.getCiteLinkPage();
            }
        }

        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }
        sb.append(getValue(viewManager));
        if (suffix != null) {
            sb.append(suffix);
            if (appendImageNumberToSuffix) {
                sb.append(viewManager.getCurrentImageOrder());
            }
        }

        return sb.toString();
    }

    /**
     * @return the type
     */
    public CitationLinkType getType() {
        return type;
    }

    /**
     * @return the level
     */
    public CitationLinkLevel getLevel() {
        return level;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @return the value
     * @throws IndexUnreachableException
     * @throws PresentationException
     * @should return correct value for record type
     * @should return correct value for docstruct type
     * @should return correct value for image type
     */
    public String getValue(ViewManager viewManager) throws IndexUnreachableException, PresentationException {
        if (!CitationLinkType.URL.equals(type)) {
            return null;
        }

        if (StringUtils.isEmpty(this.value)) {
            logger.trace("Loading value: {}/{}", level, field);
            String query = null;
            switch (level) {
                case RECORD:
                    query = SolrConstants.PI + ":" + viewManager.getPi();
                    break;
                case DOCSTRUCT:
                    query = "+" + SolrConstants.IDDOC + ":" + viewManager.getCurrentStructElement().getLuceneId();
                    break;
                case IMAGE:
                    query = "+" + SolrConstants.PI_TOPSTRUCT + ":" + viewManager.getPi() + " +" + SolrConstants.ORDER + ":"
                            + viewManager.getCurrentImageOrder() + " +" + SolrConstants.DOCTYPE
                            + ":" + DocType.PAGE.name();
                    break;
            }

            SolrDocument doc = DataManager.getInstance().getSearchIndex().getFirstDoc(query, Collections.singletonList(field));
            if (doc == null) {
                return null;
            }

            if (doc.get(field) != null) {
                this.value = String.valueOf(doc.get(field));
            }
        }

        return value;
    }

    /**
     * @param field the field to set
     * @return this
     */
    public CitationLink setField(String field) {
        this.field = field;
        return this;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @param prefix the prefix to set
     * @return this
     */
    public CitationLink setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    /**
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * @param suffix the suffix to set
     * @return this
     */
    public CitationLink setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * @return the appendImageNumberToSuffix
     */
    public boolean isAppendImageNumberToSuffix() {
        return appendImageNumberToSuffix;
    }

    /**
     * @param appendImageNumberToSuffix the appendImageNumberToSuffix to set
     * @return this
     */
    public CitationLink setAppendImageNumberToSuffix(boolean appendImageNumberToSuffix) {
        this.appendImageNumberToSuffix = appendImageNumberToSuffix;
        return this;
    }

}
