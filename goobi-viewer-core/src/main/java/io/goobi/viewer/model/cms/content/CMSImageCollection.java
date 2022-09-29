/*
 * This file is part of the Goobi viewer - a content presentation and management
 * application for digitized objects.
 *
 * Visit these websites for more information.
 *          - http://www.intranda.com
 *          - http://digiverso.com
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.goobi.viewer.model.cms.content;

import static io.goobi.viewer.api.rest.v1.ApiUrls.CMS_MEDIA;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.dao.converter.StringListConverter;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.exceptions.ViewerConfigurationException;
import io.goobi.viewer.managedbeans.CmsBean;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.model.cms.CMSCategory;
import io.goobi.viewer.model.jsf.CheckboxSelectable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "cms_content_imagecollection")
public class CMSImageCollection extends CMSContent {

    private static final String COMPONENT_NAME = "imagecollection";
    private static final int DEFAULT_IMAGES_PER_VIEW = 10;
    private static final int DEFAULT_IMPORTANT_IMAGES_PER_VIEW = 0;
    
    @Column(name="categories", columnDefinition = "MEDIUMTEXT")
    @Convert(converter = StringListConverter.class)
    private List<CMSCategory> categories;
    
    @Column(name="images_per_view")
    private int imagesPerView;
    @Column(name="important_images_per_view")
    private int importantImagesPerView;
    
    @Transient List<CheckboxSelectable<CMSCategory>> selectableCategories = null;
    
    public CMSImageCollection() {
        this.categories = new ArrayList<>();
        imagesPerView = DEFAULT_IMAGES_PER_VIEW;
        importantImagesPerView = DEFAULT_IMPORTANT_IMAGES_PER_VIEW;
    }
    
    private CMSImageCollection(CMSImageCollection orig) {
        this.categories = orig.categories;
        this.imagesPerView = orig.imagesPerView;
        this.importantImagesPerView = orig.importantImagesPerView;
    }
    
    @Override
    public String getBackendComponentName() {
        return COMPONENT_NAME;
    }
    
    public List<CMSCategory> getCategories() {
        return categories;
    }

    public List<CheckboxSelectable<CMSCategory>> getSelectableCategories() throws DAOException {
        if(this.selectableCategories == null) {
            createSelectableCategories();
        }
        return this.selectableCategories;
        
    }

    private void createSelectableCategories() throws DAOException {
        this.selectableCategories = DataManager.getInstance().getDao().getAllCategories()
            .stream()
           .map(cat -> new CheckboxSelectable<>(this.categories, cat, c -> c.getName()))
           .collect(Collectors.toList());
    }
    
    public int getImagesPerView() {
        return imagesPerView;
    }
    
    public void setImagesPerView(int imagesPerView) {
        this.imagesPerView = imagesPerView;
    }
    
    public int getImportantImagesPerView() {
        return importantImagesPerView;
    }
    
    public void setImportantImagesPerView(int importantImagesPerView) {
        this.importantImagesPerView = importantImagesPerView;
    }

    /**
     * <p>
     * getTileGridUrl.
     * </p>
     *
     * @param itemId a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws de.unigoettingen.sub.commons.contentlib.exceptions.IllegalRequestException if any.
     */
    public String getTileGridUrl() throws IllegalRequestException {

            String tags = this.getCategories().stream().map(CMSCategory::getName).collect(Collectors.joining(","));

            String url = DataManager.getInstance()
                    .getRestApiManager()
                    .getDataApiManager()
                    .map(urls -> urls.path(CMS_MEDIA)
                            .query("tags", tags)
                            .query("max", this.getImagesPerView())
                            .query("prioritySlots", this.getImportantImagesPerView())
                            .query("random", "true")
                            .build())
                    .orElse(getLegacyTileGridUrl());

            return url;
    }

    /**
     * @return
     */
    private String getLegacyTileGridUrl() {
        StringBuilder sb = new StringBuilder(BeanUtils.getServletPathWithHostAsUrlFromJsfContext());
        sb.append("/rest/tilegrid/")
                .append(CmsBean.getCurrentLocale().getLanguage())
                .append("/")
                .append(this.getImagesPerView())
                .append("/")
                .append(this.getImportantImagesPerView())
                .append("/")
                .append(this.getCategories().stream().map(CMSCategory::getName).collect(Collectors.joining("$")))
                .append("/");
        return sb.toString();
    }

    @Override
    public CMSContent copy() {
        return new CMSImageCollection(this);
    }

    @Override
    public List<File> exportHtmlFragment(String outputFolderPath, String namingScheme) throws IOException, ViewerConfigurationException {
        return Collections.emptyList();
    }

}
