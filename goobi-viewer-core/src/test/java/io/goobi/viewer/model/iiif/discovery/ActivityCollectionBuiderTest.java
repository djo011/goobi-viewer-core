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
package io.goobi.viewer.model.iiif.discovery;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.goobi.viewer.AbstractSolrEnabledTest;
import io.goobi.viewer.exceptions.IndexUnreachableException;
import io.goobi.viewer.exceptions.PresentationException;
import io.goobi.viewer.solr.SolrConstants;
import io.goobi.viewer.solr.SolrTools;

class ActivityCollectionBuiderTest extends AbstractSolrEnabledTest {

    /**
     * Test method for
     * {@link io.goobi.viewer.model.iiif.search.SearchResultConverter#getAnnotationsFromFulltext(java.lang.String, java.lang.String, java.lang.Integer, java.lang.String, long, int, int)}.
     */
    /**
     * @throws IndexUnreachableException 
     * @throws PresentationException 
     * @see ActivityCollectionBuider#getDocs(Long,Long)
     * @verifies only return topstructs
     */
    @Test
    void getDocs_shouldOnlyReturnTopstructs() throws PresentationException, IndexUnreachableException {
        SolrDocumentList docs = ActivityCollectionBuilder.getDocs(null, null);
        Assertions.assertNotNull(docs);
        Assertions.assertFalse(docs.isEmpty());
        for (SolrDocument doc : docs) {
            Assertions.assertNotNull(SolrTools.getSingleFieldValue(doc, SolrConstants.PI));
            Assertions.assertNotNull(SolrTools.getSingleFieldValue(doc, SolrConstants.DATECREATED));
        }
    }
}
