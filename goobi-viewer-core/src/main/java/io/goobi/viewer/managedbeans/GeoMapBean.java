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
package io.goobi.viewer.managedbeans;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.url.URL;

import io.goobi.viewer.api.rest.v1.ApiUrls;
import io.goobi.viewer.controller.DataManager;
import io.goobi.viewer.exceptions.DAOException;
import io.goobi.viewer.managedbeans.utils.BeanUtils;
import io.goobi.viewer.messages.Messages;
import io.goobi.viewer.model.cms.pages.CMSPage;
import io.goobi.viewer.model.maps.FeatureSet;
import io.goobi.viewer.model.maps.GeoMap;
import io.goobi.viewer.model.maps.GeoMap.GeoMapType;
import io.goobi.viewer.model.maps.GeoMapMarker;
import io.goobi.viewer.model.maps.ManualFeatureSet;
import io.goobi.viewer.model.maps.SolrFeatureSet;

/**
 * Bean for managing {@link GeoMaps} in the admin Backend
 *
 * @author florian
 *
 */
@Named
@ViewScoped
public class GeoMapBean implements Serializable {

    private static final long serialVersionUID = 2602901072184103402L;

    private GeoMap currentMap = null;

    private String selectedLanguage;

    private List<GeoMap> loadedMaps = null;

    /**
     *
     */
    public GeoMapBean() {
        this.selectedLanguage = BeanUtils.getNavigationHelper().getLocaleString();
    }

    /**
     * @return the currentMap
     */
    public GeoMap getCurrentMap() {
        return currentMap;
    }

    /**
     *
     * Sets the current map to a clone of the given map
     *
     * @param currentMap the currentMap to set
     */
    public void setCurrentMap(GeoMap currentMap) {
        this.currentMap = new GeoMap(currentMap);
    }

    /**
     * If a GeoMap of the given mapId exists in the database, set the current map to a clone of that map
     *
     * @param mapId
     * @throws DAOException
     */
    public void setCurrentMapId(Long mapId) throws DAOException {
        GeoMap orig = DataManager.getInstance().getDao().getGeoMap(mapId);
        this.currentMap = new GeoMap(orig);
    }

    public Long getCurrentMapId() {
        if (this.currentMap != null) {
            return this.currentMap.getId();
        }

        return null;
    }

    /**
     * Save the current map. Either add it to database if it has no id yet, or otherwise update it in the database
     *
     * @throws DAOException
     */
    public void saveCurrentMap() throws DAOException {
        boolean saved = false;
        if (this.currentMap == null) {
            throw new IllegalArgumentException("No map selected. Cannot save");
        } else if (this.currentMap.getId() == null) {
            this.currentMap.setDateCreated(LocalDateTime.now());
            this.currentMap.setDateUpdated(LocalDateTime.now());
            this.currentMap.setCreator(BeanUtils.getUserBean().getUser());
            saved = DataManager.getInstance().getDao().addGeoMap(this.currentMap);
        } else {
            this.currentMap.setDateUpdated(LocalDateTime.now());
            saved = DataManager.getInstance().getDao().updateGeoMap(this.currentMap);
        }
        if (saved) {
            Messages.info("notify__save_map__success");
        } else {
            Messages.error("notify__save_map__error");
        }
        this.loadedMaps = null;
    }

    public void deleteMap(GeoMap map) throws DAOException {
        DataManager.getInstance().getDao().deleteGeoMap(map);
        this.loadedMaps = null;
    }

    public String getEditMapUrl(GeoMap map) {
        URL mappedUrl =
                PrettyContext.getCurrentInstance().getConfig().getMappingById("adminCmsGeoMapEdit").getPatternParser().getMappedURL(map.getId());
        return BeanUtils.getServletPathWithHostAsUrlFromJsfContext() + mappedUrl.toString();
    }

    /**
     * If the current map has an id, restore the map from the database, removing all unsaved changes. If the current map exists but has no id, set the
     * current map to a new empty map
     *
     * @throws DAOException
     *
     */
    public void resetCurrentMap() throws DAOException {
        if (getCurrentMap() != null) {
            if (getCurrentMap().getId() != null) {
                setCurrentMapId(getCurrentMap().getId());
            } else {
                createEmptyCurrentMap();
            }
        }
    }

    /**
     * Sets the currentMap to a new empty {@link GeoMap}
     *
     * @return the pretty url to creating a new GeoMap
     */
    public void createEmptyCurrentMap() {
        this.currentMap = new GeoMap();
    }

    /**
     * @return the selectedLanguage
     */
    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    /**
     * @param selectedLanguage the selectedLanguage to set
     */
    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    /**
     * Get a list of all {@link GeoMap}s from the databse. Note that the databse is queries at each method call
     *
     * @return a list of all stored GeoMaps
     * @throws DAOException
     */
    public List<GeoMap> getAllMaps() throws DAOException {
        if (this.loadedMaps == null) {
            this.loadedMaps = DataManager.getInstance().getDao().getAllGeoMaps();
        }
        return this.loadedMaps;
    }

    public Collection<GeoMapType> getPossibleMapTypes() {
        return EnumSet.allOf(GeoMapType.class);
    }

    public Collection<GeoMapMarker> getPossibleMarkers() {
        return DataManager.getInstance().getConfiguration().getGeoMapMarkers();
    }

    public boolean hasCurrentFeature() {
        return false;
    }

    public boolean isInUse(GeoMap map) throws DAOException {
        return !DataManager.getInstance().getDao().getPagesUsingMap(map).isEmpty();
    }

    public List<CMSPage> getEmbeddingCmsPages(GeoMap map) throws DAOException {
        return DataManager.getInstance().getDao().getPagesUsingMap(map);
    }

    public boolean isHasMaps() throws DAOException {
        return !getAllMaps().isEmpty();
    }

    public String getCoordinateSearchQueryTemplate(SolrFeatureSet featureSet) {
        String locationQuery = "WKT_COORDS:\"Intersects(POINT({lng} {lat})) distErrPct=0\"";
        String filterQuery = featureSet != null ? featureSet.getSolrQuery() : "";
        String query = locationQuery;
        if (StringUtils.isNotBlank(filterQuery)) {
            query = "(" + locationQuery + ") AND (" + filterQuery + ")";
        }
        URL mappedUrl = PrettyContext.getCurrentInstance()
                .getConfig()
                .getMappingById("newSearch5")
                .getPatternParser()
                .getMappedURL(query, "1", "-", "-");
        return BeanUtils.getServletPathWithHostAsUrlFromJsfContext() + mappedUrl.toString();
    }

    public String getHeatmapUrl() {
        return DataManager.getInstance()
                .getRestApiManager()
                .getDataApiManager()
                .map(urls -> urls.path(ApiUrls.INDEX, ApiUrls.INDEX_SPATIAL_HEATMAP).build())
                .orElse("");
    }

    public String getFeatureUrl() {
        return DataManager.getInstance()
                .getRestApiManager()
                .getDataApiManager()
                .map(urls -> urls.path(ApiUrls.INDEX, ApiUrls.INDEX_SPATIAL_SEARCH).build())
                .orElse("");
    }
    
    public void addFeatureSet(GeoMap map, String type) {
        if(map != null && type != null) {            
            switch(type) {
                case "MANUAL":
                    map.addFeatureSet(new ManualFeatureSet());
                    break;
                case "SOLR_QUERY":
                    map.addFeatureSet(new SolrFeatureSet());
                    break;
            }
        }
    }
    
    public boolean isSolrQueryMap(GeoMap map) {
        if(map != null && !map.getFeatureSets().isEmpty()) {
            return map.getFeatureSets().get(0).isQueryResultSet();
        } else {
            return false;
        }
    }
    
    public GeoMapType getCurrentGeoMapType() {
        return Optional.ofNullable(currentMap).map(this::isSolrQueryMap).map(b -> b ? GeoMapType.SOLR_QUERY : GeoMapType.MANUAL).orElse(null);
    }
    
    public void setCurrentGeoMapType(GeoMapType type) {
        
        if(currentMap != null) {
            FeatureSet featureSet = null;
            switch(type) {
                case MANUAL:
                    featureSet = new ManualFeatureSet();
                    break;
                case SOLR_QUERY:
                    featureSet = new SolrFeatureSet();
            }
            currentMap.setFeatureSets(Collections.singletonList(featureSet));
        }
    }
}
