/**

Copyright (C) SYSTAP, LLC DBA Blazegraph 2006-2016.  All rights reserved.

Contact:
     SYSTAP, LLC DBA Blazegraph
     2501 Calvert ST NW #106
     Washington, DC 20008
     licenses@blazegraph.com

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
 * Created on Nov 6, 2015
 */

package com.bigdata.rdf.sparql.ast.hints;

import com.bigdata.bop.PipelineOp;
import com.bigdata.bop.join.PipelineJoin.Annotations;
import com.bigdata.rdf.sparql.ast.ASTBase;
import com.bigdata.rdf.sparql.ast.IQueryNode;
import com.bigdata.rdf.sparql.ast.QueryRoot;
import com.bigdata.rdf.sparql.ast.eval.AST2BOpContext;

/**
 * Sets the {@link PipelineOp.Annotations#NUM_TASKS_PER_THREAD} annotation of an operator.
 * 
 * 
 * @author <a href="mailto:ms@metaphacts.com">Michael Schmidt</a>
 * @version $Id$
 */
final class NumTasksPerThreadHint extends AbstractIntQueryHint {

   protected NumTasksPerThreadHint() {
       super(Annotations.NUM_TASKS_PER_THREAD,
               Annotations.DEFAULT_NUM_TASKS_PER_THREAD);       
   }

   @Override
   public void handle(final AST2BOpContext context, final QueryRoot queryRoot,
           final QueryHintScope scope, final ASTBase op, final Integer value) {

       if (op instanceof IQueryNode) {

           /*
            * Note: This is set on the queryHint Properties object and then
            * transferred to the pipeline operator when it is generated.
            */
           _setQueryHint(context, scope, op, getName(), value);

       }

   }

}
