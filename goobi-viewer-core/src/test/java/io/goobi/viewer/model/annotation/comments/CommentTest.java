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
package io.goobi.viewer.model.annotation.comments;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.goobi.viewer.AbstractSolrEnabledTest;

class CommentTest extends AbstractSolrEnabledTest {

    /**
    * @see Comment#isTargetPiRecordIndexed()
    * @verifies return true if record exists
    */
    @Test
    void isTargetPiRecordIndexed_shouldReturnTrueIfRecordExists() throws Exception {
        Comment comment = new Comment();
        comment.setTargetPI(AbstractSolrEnabledTest.PI_KLEIUNIV);
        Assertions.assertTrue(comment.isTargetPiRecordIndexed());
    }

    /**
    * @see Comment#isTargetPiRecordIndexed()
    * @verifies return false if record missing
    */
    @Test
    void isTargetPiRecordIndexed_shouldReturnFalseIfRecordMissing() throws Exception {
        Comment comment = new Comment();
        comment.setTargetPI("foobar");
        Assertions.assertFalse(comment.isTargetPiRecordIndexed());
    }

    /**
    * @see Comment#isTargetPiRecordIndexed()
    * @verifies return false if targetPI not set
    */
    @Test
    void isTargetPiRecordIndexed_shouldReturnFalseIfTargetPINotSet() throws Exception {
        Comment comment = new Comment();
        Assertions.assertFalse(comment.isTargetPiRecordIndexed());
    }
}
