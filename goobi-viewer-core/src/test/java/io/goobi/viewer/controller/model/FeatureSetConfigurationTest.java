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
package io.goobi.viewer.controller.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.goobi.viewer.AbstractTest;
import io.goobi.viewer.controller.Configuration;
import io.goobi.viewer.controller.DataManager;

public class FeatureSetConfigurationTest extends AbstractTest {

    @Test
    void testReadConfiguation() {

        Configuration config = DataManager.getInstance().getConfiguration();
        List<FeatureSetConfiguration> configs = config.getRecordGeomapFeatureSetConfigs("");

        assertEquals(2, configs.size());

        assertEquals("metadata", configs.get(0).getType());
        assertEquals("set_metadata_1", configs.get(0).getName());
        assertEquals("maps_marker_1", configs.get(0).getMarker());
        assertEquals("MD_A:A MD_B:B*", configs.get(0).getQuery());
        assertEquals(1, configs.get(0).getFilters().size());
        assertEquals("", configs.get(0).getFilters().get(0).getLabel());
        assertEquals("MD_METADATATYPE", configs.get(0).getFilters().get(0).getValue());

        assertEquals("event", configs.get(1).getType());
        assertEquals("set_event_2", configs.get(1).getName());
        assertEquals("maps_marker_2", configs.get(1).getMarker());
        assertEquals("DOCTYPE:event", configs.get(1).getQuery());
        assertEquals(2, configs.get(1).getFilters().size());
        assertEquals("", configs.get(1).getFilters().get(0).getLabel());
        assertEquals("MD_EVENTTYPE", configs.get(1).getFilters().get(0).getValue());
        assertEquals("", configs.get(1).getFilters().get(1).getLabel());
        assertEquals("MD_TYPE", configs.get(1).getFilters().get(1).getValue());

    }
}
