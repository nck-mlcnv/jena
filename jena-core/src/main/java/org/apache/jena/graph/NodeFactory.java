/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.graph;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sys.JenaSystem;

public class NodeFactory {

    static { JenaSystem.init(); }
    private NodeFactory() {}

    public static RDFDatatype getType(String s) {
        if ( s == null )
            return null;
        return TypeMapper.getInstance().getSafeTypeByName(s);
    }

    /** Make a fresh blank node */
    public static Node createBlankNode() {
        return createBlankNode(BlankNodeId.createFreshId());
    }

    /** make a blank node with the specified label */
    public static Node createBlankNode(String string) {
        return new Node_Blank(string);
    }

    /** make a URI node with the specified URIref string */
    public static Node createURI(String uri) {
        Objects.requireNonNull(uri, "Argument to NodeFactory.createURI is null");
        return new Node_URI(uri);
    }

    /** make a variable node with a given name */
    public static Node createVariable(String name) {
        Objects.requireNonNull(name, "Argument to NodeFactory.createVariable is null");
        return new Node_Variable(name);
    }

    /** make an extension node based on a string. */
    public static Node createExt(String name) {
        Objects.requireNonNull(name, "Argument to NodeFactory.createExt is null");
        return new Node_Marker(name);
    }

    /**
     * Make a literal node with the specified literal value
     * @deprecated Making nodes directly from {@link LiteralLabel} may be removed.
     */
    @Deprecated
    public static Node createLiteral(LiteralLabel lit) {
        Objects.requireNonNull(lit, "Argument to NodeFactory.createLiteral is null");
        return new Node_Literal( lit );
    }

    public static Node createLiteral(String string) {
        Objects.requireNonNull(string, "Argument to NodeFactory.createLiteral is null");
        return new Node_Literal(string);
    }

    /**
     * Make a literal with specified language. lexical form must not be null.
     *
     * @param string  the lexical form of the literal
     * @param lang    the optional language tag
     */
    public static Node createLiteral(String string, String lang) {
        Objects.requireNonNull(string, "null lexical form for literal");
        if ( StringUtils.isEmpty(lang) )
            return new Node_Literal(string);
        else
            return new Node_Literal(string, lang);
    }

    /**
     * Build a literal node from its lexical form.
     * <p>
     * This is a convenience operation for passing in language and datatype without
     * needing the caller to differentiate the xsd:string, rdf:langString and other
     * datatype cases.
     * It calls {@link #createLiteral(String)},
     * {@link #createLiteral(String, String)} or
     * {@link #createLiteral(String, RDFDatatype)}
     * as appropriate.
     *
     * @param lex the lexical form of the literal
     * @param lang the optional language tag or null or ""
     * @param dtype the type of the literal or null.
     */
    public static Node createLiteral(String lex, String lang, RDFDatatype dtype) {
        Objects.requireNonNull(lex, "null lexical form for literal");
        boolean hasLang = ! StringUtils.isEmpty(lang);
        if ( hasLang ) {
            if ( dtype != null && ! dtype.equals(RDFLangString.rdfLangString) )
                throw new JenaException("Datatype is not rdf:langString but a language was given");
            return createLiteral(lex, lang);
        }

        if ( dtype == null )
            // No datatype, no lang (it is null or "") => xsd:string.
            return createLiteral(lex);

        // No lang, with datatype and it's not rdf:langString
        if ( dtype.equals(RDFLangString.rdfLangString) )
            throw new JenaException("Datatype is rdf:langString but no language given");
        Node n = createLiteral(lex, dtype);
        return n;
    }

    /**
     * Build a typed literal node from its lexical form.
     *
     * @param lex
     *            the lexical form of the literal
     * @param dtype
     *            the type of the literal
     */
    public static Node createLiteral(String lex, RDFDatatype dtype) {
        Objects.requireNonNull(lex, "null lexical form for literal");
        if ( dtype == null )
            dtype = XSDDatatype.XSDstring;
        return new Node_Literal(lex, dtype);
    }

    /** Create a Node based on the value
     * If the value is a string we
     * assume this is intended to be a lexical form after all.
     * @param value
     *          The value, mapped according to registered types.
     * @return Node
     */
    public static Node createLiteralByValue(Object value) {
        Objects.requireNonNull(value, "Argument 'value' to NodeFactory.createLiteralByValue is null");
        return new Node_Literal(LiteralLabelFactory.createByValue(value));
    }

    /** Create a Node based on the value
     * If the value is a string we
     * assume this is intended to be a lexical form after all.
     * @param value
     *          The value, mapped according to registered types.
     * @param dtype
     *          RDF Datatype.
     * @return Node
     */
    public static Node createLiteralByValue(Object value, RDFDatatype dtype) {
        Objects.requireNonNull(value, "Argument 'value' to NodeFactory.createLiteralByValue is null");
        return new Node_Literal(LiteralLabelFactory.createByValue(value, dtype));
    }

    /** Create a triple node (RDF-star) */
    public static Node createTripleNode(Node s, Node p, Node o) {
        Triple triple = Triple.create(s, p, o);
        return createTripleNode(triple);
    }

    /** Create a triple node (RDF-star) */
    public static Node createTripleNode(Triple triple) {
        return new Node_Triple(triple);
    }

    /** Create a graph node. This is an N3-formula; it is not a named graph (see "quad") */
    public static Node createGraphNode(Graph graph) {
        return new Node_Graph(graph);
    }
}
