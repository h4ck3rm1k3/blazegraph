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
package com.bigdata.rdf.internal.constraints;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.bigdata.bop.BOp;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IValueExpression;
import com.bigdata.rdf.error.SparqlTypeErrorException;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.internal.XSD;
import com.bigdata.rdf.model.BigdataLiteral;
import com.bigdata.rdf.model.BigdataURI;
import com.bigdata.rdf.sparql.ast.DummyConstantNode;

public class IriBOp extends AbstractLiteralBOp {

    private static final long serialVersionUID = -8448763718374010166L;

    public IriBOp(IValueExpression<? extends IV> x, String lex) {
        super(x, lex);
    }

    public IriBOp(BOp[] args, Map<String, Object> anns) {
        super(args, anns);
        if (args.length != 1 || args[0] == null)
            throw new IllegalArgumentException();
    }

    public IriBOp(IriBOp op) {
        super(op);
    }

    public Requirement getRequirement() {
        return Requirement.SOMETIMES;
    }

    public IV _get(final IBindingSet bs) throws SparqlTypeErrorException {
        
    	IV iv = get(0).get(bs);
    	
        if (iv == null)
            throw new SparqlTypeErrorException.UnboundVarException();

        if (iv.isURI()) {
        	
        	return iv;
        	
        }

        if (!iv.isLiteral())
        	throw new SparqlTypeErrorException();
        
        final BigdataLiteral lit = literalValue(iv);
        
        final BigdataURI dt = lit.getDatatype();
        
        if (dt != null && !dt.stringValue().equals(XSD.STRING.stringValue()))
            throw new SparqlTypeErrorException();
        	
        final BigdataURI uri = getValueFactory().createURI(lit.getLabel());
            
        return DummyConstantNode.toDummyIV(uri);
            
    }
    
}