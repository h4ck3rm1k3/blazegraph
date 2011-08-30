/**

Copyright (C) SYSTAP, LLC 2006-2011.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Aug 29, 2011
 */

package com.bigdata.rdf.sparql.ast;

/**
 * Unit tests for subquery evaluation based on an AST input model.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class TestASTExistsSubqueryEvaluation extends AbstractASTEvaluationTestCase {

    public TestASTExistsSubqueryEvaluation() {
    }

    public TestASTExistsSubqueryEvaluation(final String name) {
        super(name);
    }

    /**
     * Unit test for an AST modeling an EXISTS filter. The EXISTS filter is
     * modeled as an ASK sub-query which projects an anonymous variable and a
     * simple test of the truth state of that anonymous variable.
     */
    public void test_existsSubquery() {
        fail("write test");
    }

}