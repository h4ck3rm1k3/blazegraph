package com.bigdata.rdf.sparql.ast;

import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase2;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import com.bigdata.bop.Constant;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.PipelineOp;
import com.bigdata.bop.bindingSet.ListBindingSet;
import com.bigdata.bop.engine.AbstractRunningQuery;
import com.bigdata.bop.engine.QueryEngine;
import com.bigdata.bop.fed.QueryEngineFactory;
import com.bigdata.journal.BufferMode;
import com.bigdata.journal.ITx;
import com.bigdata.journal.Journal;
import com.bigdata.rdf.axioms.NoAxioms;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.internal.constraints.MathBOp;
import com.bigdata.rdf.internal.impl.literal.XSDNumericIV;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.store.LocalTripleStore;
import com.bigdata.rdf.vocab.NoVocabulary;
import com.bigdata.relation.accesspath.ThickAsynchronousIterator;

/**
 * AST evaluation test suite.
 *
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class TestASTTriplesModeEvaluation extends TestCase2 {

    /**
     *
     */
    public TestASTTriplesModeEvaluation() {
    }

    public TestASTTriplesModeEvaluation(String name) {
        super(name);
    }

    private StatementPatternNode sp() {

        return new StatementPatternNode(new VarNode("s"), new VarNode("p"),
                new VarNode("o"));
        
    }

    /**
     * Unit test developed to identify a problem where a query with 3 solutions
     * passes through those solutions but a query with one does not.
     * 
     * @throws Exception
     */
    public void testAST() throws Exception {

        final Properties properties = getProperties();

        final AbstractTripleStore store = getStore(properties);

        try {

            final URI x = new URIImpl("http://www.foo.org/x");
            final URI y = new URIImpl("http://www.foo.org/y");
            final URI z = new URIImpl("http://www.foo.org/z");
            final URI A = new URIImpl("http://www.foo.org/A");
            final URI B = new URIImpl("http://www.foo.org/B");
            final URI C = new URIImpl("http://www.foo.org/C");
            final URI rdfType = RDF.TYPE;

            // add statements using the URIs declared above.
            store.addStatement(x, rdfType, C);
            store.addStatement(y, rdfType, B);
            store.addStatement(z, rdfType, A);
 
            final AtomicInteger idFactory = new AtomicInteger(0);
            
            final QueryEngine queryEngine = QueryEngineFactory
                    .getQueryController(store.getIndexManager());
            
            /*
             * Run query expecting 3 statements.
             */
            {
                
                final IGroupNode root = new JoinGroupNode();

                root.addChild(sp());

                final QueryRoot query = new QueryRoot();

                query.setWhereClause(root);

                final ProjectionNode pn = new ProjectionNode();
                pn.addProjectionVar(new VarNode("s"));
                query.setProjection(pn);

                final PipelineOp pipeline = AST2BOpUtility
                        .convert(new AST2BOpContext(query, idFactory, store,
                                queryEngine, new Properties()));
                
                // Submit query for evaluation.
                final IBindingSet[][] existingBindings = new IBindingSet[][] { new IBindingSet[] { new ListBindingSet() } };

                final AbstractRunningQuery runningQuery = queryEngine.eval(UUID
                        .randomUUID(), pipeline,
                        new ThickAsynchronousIterator<IBindingSet[]>(
                                existingBindings));
                
                final Iterator<IBindingSet[]> iter = runningQuery.iterator();

                int i = 0;
                while (iter.hasNext()) {
                    final IBindingSet[] set = iter.next();
                    i += set.length;
                }

                runningQuery.get();// check the future.

                assertEquals("Baseline", 3, i);
                
            }
            
            /*
             * Run query expecting 1 statement using SLICE w/ LIMIT :=1.
             */
            if(true){
                
                final IGroupNode root = new JoinGroupNode();

                root.addChild(sp());

                final QueryRoot query = new QueryRoot();

                query.setWhereClause(root);

                final ProjectionNode pn = new ProjectionNode();
                
                pn.addProjectionVar(new VarNode("s"));
                
                query.setProjection(pn);

                final SliceNode sn = new SliceNode();
                sn.setLimit(1);
                query.setSlice(sn);

                final PipelineOp pipeline = AST2BOpUtility
                        .convert(new AST2BOpContext(query, idFactory, store,
                                queryEngine, new Properties()));
                
                // Submit query for evaluation.
                final IBindingSet[][] existingBindings = new IBindingSet[][] { new IBindingSet[] { new ListBindingSet() } };

                final AbstractRunningQuery runningQuery = queryEngine.eval(UUID
                        .randomUUID(), pipeline,
                        new ThickAsynchronousIterator<IBindingSet[]>(
                                existingBindings));
                
                final Iterator<IBindingSet[]> iter = runningQuery.iterator();

                int i = 0;
                while (iter.hasNext()) {
                    final IBindingSet[] set = iter.next();
                    i += set.length;
                }

                runningQuery.get();// check the future.

                assertEquals("SLICE(limit=1)", 1, i);
            
            }

            /*
             * Run query expecting 3 statements using DISTINCT
             */
            {
                
                final IGroupNode root = new JoinGroupNode();

                root.addChild(sp());

                final QueryRoot query = new QueryRoot();

                query.setWhereClause(root);

                final ProjectionNode pn = new ProjectionNode();
                pn.addProjectionVar(new VarNode("s"));
                pn.setDistinct(true);
                query.setProjection(pn);

                final PipelineOp pipeline = AST2BOpUtility
                        .convert(new AST2BOpContext(query, idFactory, store,
                                queryEngine, new Properties()));
                
                // Submit query for evaluation.
                final IBindingSet[][] existingBindings = new IBindingSet[][] { new IBindingSet[] { new ListBindingSet() } };

                final AbstractRunningQuery runningQuery = queryEngine.eval(UUID
                        .randomUUID(), pipeline,
                        new ThickAsynchronousIterator<IBindingSet[]>(
                                existingBindings));
                
                final Iterator<IBindingSet[]> iter = runningQuery.iterator();

                int i = 0;
                while (iter.hasNext()) {
                    final IBindingSet[] set = iter.next();
                    i += set.length;
                }
                
                runningQuery.get();// check the future.

                assertEquals("DISTINCT", 3, i);

            }
            
        } finally {

            store.__tearDownUnitTest();

        }

    }

    /**
     * Unit test with an optional join but with and without DISTINCT. The
     * version with "SELECT DISTINCT" will encounter an unbound variable. The
     * DISTINCT operator must handle that unbound variable in the solution.
     * 
     * @throws Exception
     */
    public void testOptionalDistinct() throws Exception {

        final Properties properties = getProperties();

        final AbstractTripleStore store = getStore(properties);

        try {

            final URI x = new URIImpl("http://www.foo.org/x");
            final URI y = new URIImpl("http://www.foo.org/y");
            final URI z = new URIImpl("http://www.foo.org/z");
            final URI A = new URIImpl("http://www.foo.org/A");
            final URI B = new URIImpl("http://www.foo.org/B");
            final URI C = new URIImpl("http://www.foo.org/C");
            final URI rdfType = RDF.TYPE;

            // add statements using the URIs declared above.
            store.addStatement(x, rdfType, C);
            store.addStatement(x, y, B);
            store.addStatement(z, rdfType, A);

            final AtomicInteger idFactory = new AtomicInteger(0);

            final QueryEngine queryEngine = QueryEngineFactory
                    .getQueryController(store.getIndexManager());


            final IV type = store.getIV(rdfType);
            final IV cIv = store.getIV(C);
            final IV bIv = store.getIV(B);

            /*
             * Run query expecting 1 statements w/o DISTINCT
             */
            {

                final IGroupNode root = new JoinGroupNode();
                
                root.addChild(new StatementPatternNode(new VarNode("s"),
                        new ConstantNode(new Constant<IV>(type)),
                        new ConstantNode(new Constant<IV>(cIv))));
                
                final JoinGroupNode optional = new JoinGroupNode(true/* optional */);

                optional.addChild(new StatementPatternNode(new VarNode("s"),
                        new ConstantNode(new Constant<IV>(bIv)), new VarNode(
                                "o")));
                
                root.addChild(optional);

                final QueryRoot query = new QueryRoot();

                query.setWhereClause(root);

                final ProjectionNode pn = new ProjectionNode();
                pn.addProjectionVar(new VarNode("s"));
                pn.addProjectionVar(new VarNode("o"));
                query.setProjection(pn);

                final PipelineOp pipeline = AST2BOpUtility
                        .convert(new AST2BOpContext(query, idFactory, store,
                                queryEngine, new Properties()));

                // Submit query for evaluation.
                final IBindingSet[][] existingBindings = new IBindingSet[][] { new IBindingSet[] { new ListBindingSet() } };

                final AbstractRunningQuery runningQuery = queryEngine.eval(UUID
                        .randomUUID(), pipeline,
                        new ThickAsynchronousIterator<IBindingSet[]>(
                                existingBindings));

                final Iterator<IBindingSet[]> iter = runningQuery.iterator();

                int i = 0;
                while (iter.hasNext()) {
                    final IBindingSet[] set = iter.next();
                    i += set.length;
                }

                runningQuery.get();// check the future.

                assertEquals("Baseline", 1, i);

            }

            /*
             * Run query expecting 1 statements, but set the distinct flag.
             */
            {

                final IGroupNode root = new JoinGroupNode();

                root.addChild(new StatementPatternNode(new VarNode("s"),
                        new ConstantNode(new Constant<IV>(type)),
                        new ConstantNode(new Constant<IV>(cIv))));

                final JoinGroupNode optional = new JoinGroupNode(true/* optional */);

                optional.addChild(new StatementPatternNode(new VarNode("s"),
                        new ConstantNode(new Constant<IV>(bIv)), new VarNode(
                                "o")));

                root.addChild(optional);

                final QueryRoot query = new QueryRoot();

                query.setWhereClause(root);

                final ProjectionNode pn = new ProjectionNode();
                pn.addProjectionVar(new VarNode("s"));
                pn.addProjectionVar(new VarNode("o"));
                pn.setDistinct(true); // DISTINCT
                query.setProjection(pn);

                final PipelineOp pipeline = AST2BOpUtility
                        .convert(new AST2BOpContext(query, idFactory, store,
                                queryEngine, new Properties()));

                // Submit query for evaluation.
                final IBindingSet[][] existingBindings = new IBindingSet[][] { new IBindingSet[] { new ListBindingSet() } };

                final AbstractRunningQuery runningQuery = queryEngine.eval(UUID
                        .randomUUID(), pipeline,
                        new ThickAsynchronousIterator<IBindingSet[]>(
                                existingBindings));

                final Iterator<IBindingSet[]> iter = runningQuery.iterator();

                int i = 0;
                while (iter.hasNext()) {
                    final IBindingSet[] set = iter.next();
                    i += set.length;
                }

                runningQuery.get();// check the future.

                assertEquals("Baseline", 1, i);

            }


        } finally {

            store.__tearDownUnitTest();

        }

    }

    /**
     * Unit test developed to identify a problem where a query with 3 solutions
     * passes through those solutions but a query with one does not.
     *
     * @throws Exception
     */
    public void testProjectedGroupByWithConstant() throws Exception {

        final Properties properties = getProperties();

        final AbstractTripleStore store = getStore(properties);

        try {

            final URI x = new URIImpl("http://www.foo.org/x");
            final URI z = new URIImpl("http://www.foo.org/z");
            final URI predicate = new URIImpl("http://www.foo.org/predicate1");
            final URI C = new URIImpl("http://www.foo.org/typeC");
            final URI rdfType = RDF.TYPE;
            final Literal one= new LiteralImpl("1",XMLSchema.INT);
            final Literal two= new LiteralImpl("1",XMLSchema.INT);
            final Literal three= new LiteralImpl("1",XMLSchema.INT);


            // add statements using the URIs declared above.
            store.addStatement(x, rdfType, C);
            store.addStatement(x, predicate, one);
            store.addStatement(x, predicate, two);
            store.addStatement(x, predicate, three);

            store.addStatement(z, rdfType, C);
            store.addStatement(z, predicate, two);
            store.addStatement(z, predicate, three);

            final AtomicInteger idFactory = new AtomicInteger(0);

            final QueryEngine queryEngine = QueryEngineFactory
                    .getQueryController(store.getIndexManager());

            final IV type = store.getIV(rdfType);
            final IV cIv = store.getIV(C);
            final IV predicateIv = store.getIV(predicate);

            /*
             * Run query expecting 1 statements.
             */
            {
                VarNode s=new VarNode("s");
                VarNode o = new VarNode("o");
                final IGroupNode root = new JoinGroupNode();
                root.addChild(new StatementPatternNode(s, new ConstantNode(
                        new Constant<IV>(type)), new ConstantNode(
                        new Constant<IV>(cIv))));
                root.addChild(new StatementPatternNode(s, new ConstantNode(
                        new Constant<IV>(predicateIv)), o));

                final QueryRoot query = new QueryRoot();

                query.setWhereClause(root);

                final ProjectionNode pn = new ProjectionNode();


                pn.addProjectionVar(new VarNode("index"));
                query.setProjection(pn);

                final GroupByNode gbn = new GroupByNode();
                
                final MathBOp op = new MathBOp(o.getValueExpression(),
                        new Constant<IV>(new XSDNumericIV(1)),
                        MathBOp.MathOp.PLUS);//, store.getNamespace());
                
                final ValueExpressionNode ven = new ValueExpressionNode(op);

                gbn.addExpr(new AssignmentNode(new VarNode("index"), ven));

                query.setGroupBy(gbn);

                final PipelineOp pipeline = AST2BOpUtility
                        .convert(new AST2BOpContext(query, idFactory, store,
                                queryEngine, new Properties()));

                // Submit query for evaluation.
                final IBindingSet[][] existingBindings = new IBindingSet[][] { new IBindingSet[] { new ListBindingSet() } };

                final AbstractRunningQuery runningQuery = queryEngine.eval(UUID
                        .randomUUID(), pipeline,
                        new ThickAsynchronousIterator<IBindingSet[]>(
                                existingBindings));

                final Iterator<IBindingSet[]> iter = runningQuery.iterator();

                int i = 0;
                while (iter.hasNext()) {
                    final IBindingSet[] set = iter.next();
                    i += set.length;
                }

                runningQuery.get(); // check the future.
                
                assertEquals("Baseline", 1, i);

            }


        } finally {

            store.__tearDownUnitTest();

        }

    }

    public Properties getProperties() {

        // Note: clone to avoid modifying!!!
        final Properties properties = (Properties) super.getProperties().clone();

//        // turn on quads.
//        properties.setProperty(AbstractTripleStore.Options.QUADS, "true");

        // override the default vocabulary.
        properties.setProperty(AbstractTripleStore.Options.VOCABULARY_CLASS,
                NoVocabulary.class.getName());

        // turn off axioms.
        properties.setProperty(AbstractTripleStore.Options.AXIOMS_CLASS,
                NoAxioms.class.getName());

        // no persistence.
        properties.setProperty(com.bigdata.journal.Options.BUFFER_MODE,
                BufferMode.Transient.toString());
        
        return properties;

    }

    protected AbstractTripleStore getStore(final Properties properties) {

        final String namespace = "kb";

        // create/re-open journal.
        final Journal journal = new Journal(properties);

        final LocalTripleStore lts = new LocalTripleStore(journal, namespace,
                ITx.UNISOLATED, properties);

        lts.create();

        return lts;

    }

}