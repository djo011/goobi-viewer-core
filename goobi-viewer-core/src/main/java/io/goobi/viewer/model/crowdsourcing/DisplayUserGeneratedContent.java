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
package io.goobi.viewer.model.crowdsourcing;

import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.intranda.api.annotation.ITypedResource;
import de.intranda.api.annotation.wa.TextualResource;
import de.intranda.api.annotation.wa.TypedResource;
import io.goobi.viewer.controller.DateTools;
import io.goobi.viewer.controller.HtmlParser;
import io.goobi.viewer.controller.SolrConstants;
import io.goobi.viewer.controller.SolrSearchIndex;
import io.goobi.viewer.controller.StringTools;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.model.security.user.User;
import io.goobi.viewer.model.viewer.PageType;
import io.goobi.viewer.model.viewer.PhysicalElement;
import io.goobi.viewer.model.viewer.StructElement;

/**
 * UserGeneratedContent stub class for displaying contents generated by the crowdsourcing module.
 */
public class DisplayUserGeneratedContent {

    public enum ContentType {

        PERSON,
        CORPORATION,
        ADDRESS,
        COMMENT,
        PICTURE,
        GEOLOCATION,
        NORMDATA;

        public String getName() {
            return this.name();
        }

        public static ContentType getByName(String name) {
            for (ContentType type : ContentType.values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }

            return null;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DisplayUserGeneratedContent.class);
    /** Constant <code>format</code> */
    public static final NumberFormat format = new DecimalFormat("00000000");

    private Long id;

    private ContentType type;

    private String pi;

    private Integer page = null;

    private String label;

    private String extendendLabel = null;

    private String displayCoordinates;

    private String areaString;

    private ITypedResource annotationBody = new TypedResource();

    private User updatedBy;

    private LocalDateTime dateUpdated;

    private String accessCondition;

    /**
     * Default constructor (needed for persistence).
     */
    public DisplayUserGeneratedContent() {
    }

    /**
     * <p>
     * Getter for the field <code>id</code>.
     * </p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getId() {
        return id;
    }

    /**
     * <p>
     * Setter for the field <code>id</code>.
     * </p>
     *
     * @param id a {@link java.lang.Long} object.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>
     * Getter for the field <code>type</code>.
     * </p>
     *
     * @return the type
     */
    public ContentType getType() {
        return type;
    }

    /**
     * <p>
     * Setter for the field <code>type</code>.
     * </p>
     *
     * @param type the type to set
     */
    public void setType(ContentType type) {
        this.type = type;
    }

    /**
     * <p>
     * Getter for the field <code>pi</code>.
     * </p>
     *
     * @return the pi
     */
    public String getPi() {
        return pi;
    }

    /**
     * <p>
     * Setter for the field <code>pi</code>.
     * </p>
     *
     * @param pi the pi to set
     */
    public void setPi(String pi) {
        this.pi = pi;
    }

    /**
     * <p>
     * Getter for the field <code>page</code>.
     * </p>
     *
     * @return the page
     */
    public Integer getPage() {
        return page;
    }

    /**
     * <p>
     * Setter for the field <code>page</code>.
     * </p>
     *
     * @param page the page to set
     */
    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * <p>
     * Getter for the field <code>label</code>.
     * </p>
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * <p>
     * Setter for the field <code>label</code>.
     * </p>
     *
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Returns the <code>label</code>, if set, otherwise <code>pi</code>.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisplayLabel() {
        return StringUtils.isNotEmpty(label) ? label : pi;
    }

    /**
     * @return the extendendLabel
     */
    public String getExtendendLabel() {
        if (StringUtils.isNotBlank(this.extendendLabel)) {
            return extendendLabel;
        }
        return label;
    }

    /**
     * @param extendendLabel the extendendLabel to set
     */
    public void setExtendendLabel(String extendendLabel) {
        this.extendendLabel = extendendLabel;
    }

    /**
     * <p>
     * Getter for the field <code>updatedBy</code>.
     * </p>
     *
     * @return the updatedBy
     */
    public User getUpdatedBy() {
        return updatedBy;
    }

    /**
     * <p>
     * Setter for the field <code>updatedBy</code>.
     * </p>
     *
     * @param updatedBy the updatedBy to set
     */
    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * <p>
     * Getter for the field <code>dateUpdated</code>.
     * </p>
     *
     * @return the dateUpdated
     */
    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    /**
     * <p>
     * getDateUpdatedAsString.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDateUpdatedAsString() {
        if (dateUpdated != null) {
            return DateTools.format(dateUpdated, DateTools.formatterDEDate, false);
        }
        return null;
    }

    /**
     * <p>
     * getTimeUpdatedAsString.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTimeUpdatedAsString() {
        if (dateUpdated != null) {
            return DateTools.format(dateUpdated, DateTools.formatterISO8601Time, false);
        }
        return null;
    }

    /**
     * <p>
     * Setter for the field <code>dateUpdated</code>.
     * </p>
     *
     * @param dateUpdated the dateUpdated to set
     */
    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    /**
     * @return the accessCondition
     */
    public String getAccessCondition() {
        return accessCondition;
    }

    /**
     * @param accessCondition the accessCondition to set
     */
    public void setAccessCondition(String accessCondition) {
        this.accessCondition = accessCondition;
    }

    /**
     * <p>
     * Getter for the field <code>areaString</code>.
     * </p>
     *
     * @return the areaString
     */
    public String getAreaString() {
        return areaString;
    }

    /**
     * <p>
     * Setter for the field <code>areaString</code>.
     * </p>
     *
     * @param areaString the areaString to set
     */
    public void setAreaString(String areaString) {
        this.areaString = areaString;
    }

    /**
     * <p>
     * hasArea.
     * </p>
     *
     * @return a boolean.
     */
    public boolean hasArea() {
        return (!(getAreaString() == null) && !getAreaString().isEmpty());
    }

    /**
     * <p>
     * mayHaveArea.
     * </p>
     *
     * @return a boolean.
     */
    public boolean mayHaveArea() {
        return true;
    }

    /**
     * <p>
     * convertToIntArray.
     * </p>
     *
     * @param coordinates an array of {@link double} objects.
     * @return an array of {@link int} objects.
     */
    public static int[] convertToIntArray(double[] coordinates) {
        int[] intCoords = new int[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            Double d = coordinates[i];
            intCoords[i] = (int) Math.round(d);
        }
        return intCoords;
    }

    /**
     * <p>
     * convertToDoubleArray.
     * </p>
     *
     * @param coordinates an array of {@link int} objects.
     * @return an array of {@link double} objects.
     */
    public static double[] convertToDoubleArray(int[] coordinates) {
        double[] doubleCoords = new double[coordinates.length];
        for (int i = 0; i < coordinates.length; i++) {
            Integer k = coordinates[i];
            doubleCoords[i] = k;
        }
        return doubleCoords;
    }

    /**
     * <p>
     * Getter for the field <code>displayCoordinates</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDisplayCoordinates() {
        return displayCoordinates;
    }

    /**
     * <p>
     * Setter for the field <code>displayCoordinates</code>.
     * </p>
     *
     * @param displayCoordinates a {@link java.lang.String} object.
     */
    public void setDisplayCoordinates(String displayCoordinates) {
        this.displayCoordinates = displayCoordinates;
    }

    /* (non-Javadoc)
     * @see io.goobi.viewer.model.crowdsourcing.AbstractCrowdsourcingUpdate#getDisplayPage()
     */
    /**
     * <p>
     * getDisplayPage.
     * </p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getDisplayPage() {
        return page;
    }

    public static class DateComparator implements Comparator<DisplayUserGeneratedContent> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(DisplayUserGeneratedContent o1, DisplayUserGeneratedContent o2) {
            return o1.dateUpdated.compareTo(o2.dateUpdated);
        }
    }

    /**
     * Check if the resource has either a label or an annotation body with a type
     *
     * @return true if neither label nor annotation body exist
     */
    public boolean isEmpty() {
        return StringUtils.isEmpty(getLabel())
                && (this.annotationBody == null || StringUtils.isBlank(this.annotationBody.getType()));
    }

    /**
     * <p>
     * getTypeAsString.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTypeAsString() {
        return getType().getName();
    }

    /**
     * @return the annotationBody
     */
    public ITypedResource getAnnotationBody() {
        return annotationBody;
    }

    /**
     * @param annotationBody the annotationBody to set
     */
    public void setAnnotationBody(ITypedResource annotationBody) {
        this.annotationBody = annotationBody;
    }

    public boolean setAnnotationBody(String json) {
        if (StringUtils.isNotBlank(json) && !"{}".equals(json)) {

            ObjectMapper mapper = new ObjectMapper();
            try {
                this.annotationBody = mapper.readValue(json, de.intranda.api.annotation.wa.TypedResource.class);
                if (this.annotationBody == null) {
                    throw new IllegalArgumentException("no content generated");
                }
                return true;
            } catch (JsonProcessingException | IllegalArgumentException e) {
                try {
                    this.annotationBody = mapper.readValue(json, de.intranda.api.annotation.oa.TypedResource.class);
                    return true;
                } catch (JsonProcessingException e1) {
                    
                    this.annotationBody = new TextualResource(json, HtmlParser.isHtml(json) ? "text/html" : "text/plain");
                }

            }
        }
        return false;
    }

    /**
     * <p>
     * buildFromSolrDoc.
     * </p>
     *
     * @param doc a {@link org.apache.solr.common.SolrDocument} object.
     * @return UserGeneratedContent generated from the given Solr document
     * @should construct content correctly
     * @throws io.goobi.viewer.exceptions.IndexUnreachableException if any.
     */
    public static DisplayUserGeneratedContent buildFromSolrDoc(SolrDocument doc) throws IndexUnreachableException {
        if (doc == null) {
            throw new IllegalArgumentException("doc may not be null");
        }

        String type = (String) doc.getFieldValue(SolrConstants.UGCTYPE);
        if (type == null || ContentType.getByName(type) == null) {
            logger.error("Cannot build UGC Solr doc, UGCTYPE '{}' not found.", type);
            return null;
        }
        DisplayUserGeneratedContent ret = new DisplayUserGeneratedContent();
        long iddoc = Long.valueOf((String) doc.getFieldValue(SolrConstants.IDDOC));
        ret.setId(iddoc);
        ret.setType(ContentType.getByName(type));
        ret.setAreaString((String) doc.getFieldValue(SolrConstants.UGCCOORDS));
        ret.setDisplayCoordinates((String) doc.getFieldValue(SolrConstants.UGCCOORDS));
        ret.setPi((String) doc.getFieldValue(SolrConstants.PI_TOPSTRUCT));
        ret.setAccessCondition(SolrSearchIndex.getSingleFieldStringValue(doc, SolrConstants.ACCESSCONDITION));
        if (doc.containsKey(SolrConstants.MD_BODY)) {
            Object body = doc.getFieldValue(SolrConstants.MD_BODY);
            if (body instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> features = (List<String>) body;
                if (features.size() == 1) {
                    ret.setAnnotationBody(features.get(0));
                } else {
                    String array = "[" + features.stream().collect(Collectors.joining(",")) + "]";
                    ret.setAnnotationBody(array);
                }
            } else if (body instanceof String) {
                ret.setAnnotationBody((String) body);
            }
            ret.setTypeFromBody();
        }
        Object pageNo = doc.getFieldValue(SolrConstants.ORDER);
        if (pageNo != null && pageNo instanceof Number) {
            ret.setPage(((Number) pageNo).intValue());
        }

        if (StringUtils.isNotBlank(ret.getAnnotationBody().getType())) {
            ret.setLabel(createLabelFromBody(ret.getType(), ret.getAnnotationBody()));
            ret.setExtendendLabel(createExtendedLabelFromBody(ret.getType(), ret.getAnnotationBody()));
        } else {
            StructElement se = new StructElement(iddoc, doc);
            ret.setLabel(generateUgcLabel(se));
        }

        return ret;
    }

    /**
     * @param type
     * @param body
     * @return the text if the body is a TextualResource. Otherwise return null
     */
    private static String createExtendedLabelFromBody(ContentType type, ITypedResource body) {
        switch (type) {
            case COMMENT:
                if (body instanceof TextualResource) {
                    return ((TextualResource) body).getText();
                }
            default:
                break;
        }

        return null;
    }

    /**
     * @param annotationBody2
     * @return
     */
    private static String createLabelFromBody(ContentType type, ITypedResource body) {
        switch (type) {
            case GEOLOCATION:
                return "admin__crowdsourcing_question_type_GEOLOCATION_POINT";
            case NORMDATA:
                return Paths.get(body.getId().getPath()).getFileName().toString();
            case COMMENT:
            default:
                if (body instanceof TextualResource) {
                    return HtmlParser.getPlaintext(((TextualResource) body).getText());
                }
                return "admin__crowdsourcing_question_type_" + type.toString();
        }
    }

    /**
     * If the annotation body has a type property of one of "Feature", "AuthorityResource" or "TextualBody" then the {@link #type} is set accordingly
     */
    private void setTypeFromBody() {
        ContentType type = this.type;
        if (StringUtils.isNotBlank(this.annotationBody.getType())) {
            switch (this.annotationBody.getType()) {
                case "Feature":
                    type = ContentType.GEOLOCATION;
                    break;
                case "AuthorityResource":
                    type = ContentType.NORMDATA;
                    break;
                case "TextualBody":
                    type = ContentType.COMMENT;
            }
        }
        this.type = type;
    }

    /**
     * Builds label out of user-generated content metadata.
     *
     * @param se a {@link io.goobi.viewer.model.viewer.StructElement} object.
     * @return the generated label
     * @should generate person label correctly
     * @should generate corporation label correctly
     * @should generate address label correctly
     * @should generate comment label correctly
     * @should return label field value if ugc type unknown
     * @should return text value for all types if no other fields exist
     */
    public static String generateUgcLabel(StructElement se) {
        if (se == null) {
            throw new IllegalArgumentException("se may not be null");
        }

        String text = StringTools.escapeHtmlChars(se.getMetadataValue("MD_TEXT"));
        if (se.getMetadataValue(SolrConstants.UGCTYPE) != null) {
            switch (se.getMetadataValue(SolrConstants.UGCTYPE)) {
                case "PERSON": {
                    StringBuilder sb = new StringBuilder();
                    String first = se.getMetadataValue("MD_FIRSTNAME");
                    String last = se.getMetadataValue("MD_LASTNAME");
                    if (StringUtils.isNotEmpty(last)) {
                        sb.append(last);
                    }
                    if (StringUtils.isNotEmpty(first)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(first);
                    }
                    return sb.toString();
                }
                case "CORPORATION": {
                    StringBuilder sb = new StringBuilder();
                    String address = se.getMetadataValue("MD_ADDRESS");
                    String corp = se.getMetadataValue("MD_CORPORATION");
                    if (StringUtils.isNotEmpty(corp)) {
                        sb.append(corp);
                    }
                    if (StringUtils.isNotEmpty(address)) {
                        sb.append(" (").append(corp).append(')');
                    }
                    return sb.toString();
                }
                case "ADDRESS": {
                    StringBuilder sb = new StringBuilder();
                    String street = se.getMetadataValue("MD_STREET");
                    String houseNumber = se.getMetadataValue("MD_HOUSENUMBER");
                    String district = se.getMetadataValue("MD_DISTRICT");
                    String city = se.getMetadataValue("MD_CITY");
                    String country = se.getMetadataValue("MD_COUNTRY");
                    if (StringUtils.isNotEmpty(street)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(street);
                        if (StringUtils.isNotEmpty(houseNumber)) {
                            sb.append(", ").append(houseNumber);
                        }
                    }
                    if (StringUtils.isNotEmpty(district)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(district);
                    }
                    if (StringUtils.isNotEmpty(city)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(city);
                    }
                    if (StringUtils.isNotEmpty(country)) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(country);
                    }

                    // Text fallback
                    if (sb.length() == 0 && StringUtils.isNotEmpty(text)) {
                        sb.append(text);
                    }
                    return sb.toString();
                }
                case "COMMENT":
                    return text;
                default:
                    return se.getMetadataValue(SolrConstants.LABEL);
            }
        }

        return se.getMetadataValue(SolrConstants.LABEL);
    }

    public boolean isOnThisPage(PhysicalElement page) {
        return this.page != null && this.page.equals(page.getOrder());
    }

    public boolean isOnOtherPage(PhysicalElement page) {
        return isOnAnyPage() && !isOnThisPage(page);
    }

    public boolean isOnAnyPage() {
        return this.page != null;
    }

    public String getIconClass() {
        switch (this.type) {
            case ADDRESS:
                return "fa fa-envelope";
            case PERSON:
                return "fa fa-user";
            case CORPORATION:
                return "fa fa-home";
            case PICTURE:
                return "fa fa-photo";
            case GEOLOCATION:
                return "fa fa-map-marker";
            case NORMDATA:
                return "fa fa fa-list-ul";
            case COMMENT:
            default:
                return "fa fa-comment";
        }
    }

    public String getPageUrl() {
        return getPageUrl(BeanUtils.getNavigationHelper().getCurrentPageType());
    }

    public String getPageUrl(PageType pageType) {

        String pageTypeUrl = BeanUtils.getNavigationHelper().getPageUrl(pageType); //no trailing slash
        String pageUrl = pageTypeUrl + "/" + getPi() + "/";
        if (getPage() != null) {
            pageUrl = pageUrl + getPage() + "/#ugc=" + getId();
        }
        return pageUrl;
    }

}
