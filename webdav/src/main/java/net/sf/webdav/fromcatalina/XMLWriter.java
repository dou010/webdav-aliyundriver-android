/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.webdav.fromcatalina;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * XMLWriter helper class.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 */
public class XMLWriter {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(XMLWriter.class);

    // -------------------------------------------------------------- Constants

    /**
     * Opening tag.
     */
    public static final int OPENING = 0;

    /**
     * Closing tag.
     */
    public static final int CLOSING = 1;

    /**
     * Element with no content.
     */
    public static final int NO_CONTENT = 2;

    // ----------------------------------------------------- Instance Variables

    /**
     * Buffer.
     */
    protected StringBuffer _buffer = new StringBuffer();

    /**
     * Writer.
     */
    protected Writer _writer = null;

    /**
     * Namespaces to be declared in the root element
     */
    protected Map<String, String> _namespaces;

    /**
     * Is true until the root element is written
     */
    protected boolean _isRootElement = true;

    // ----------------------------------------------------------- Constructors

    /**
     * Constructor.
     */
    public XMLWriter(Map<String, String> namespaces) {
        _namespaces = namespaces;
    }

    /**
     * Constructor.
     */
    public XMLWriter(Writer writer, Map<String, String> namespaces) {
        _writer = writer;
        _namespaces = namespaces;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Retrieve generated XML.
     * 
     * @return String containing the generated XML
     */
    public String toString() {
        return _buffer.toString();
    }

    /**
     * Write property to the XML.
     * 
     * @param name
     *      Property name
     * @param value
     *      Property value
     */
    public void writeProperty(String name, String value) {
        writeElement(name, OPENING);
        _buffer.append(value);
        writeElement(name, CLOSING);
    }

    /**
     * Write property to the XML.
     * 
     * @param name
     *      Property name
     */
    public void writeProperty(String name) {
        writeElement(name, NO_CONTENT);
    }

    /**
     * Write an element.
     * 
     * @param name
     *      Element name
     * @param type
     *      Element type
     */
    public void writeElement(String name, int type) {
        StringBuffer nsdecl = new StringBuffer();

        if (_isRootElement) {
            for (Iterator<String> iter = _namespaces.keySet().iterator(); iter
                    .hasNext();) {
                String fullName = (String) iter.next();
                String abbrev = (String) _namespaces.get(fullName);
                nsdecl.append(" xmlns:").append(abbrev).append("=\"").append(
                        fullName).append("\"");
            }
            _isRootElement = false;
        }

        int pos = name.lastIndexOf(':');
        if (pos >= 0) {
            // lookup prefix for namespace
            String fullns = name.substring(0, pos);
            String prefix = (String) _namespaces.get(fullns);
            if (prefix == null) {
                // there is no prefix for this namespace
                name = name.substring(pos + 1);
                nsdecl.append(" xmlns=\"").append(fullns).append("\"");
            } else {
                // there is a prefix
                name = prefix + ":" + name.substring(pos + 1);
            }
        } else {
            throw new IllegalArgumentException(
                    "All XML elements must have a namespace");
        }

        switch (type) {
        case OPENING:
            _buffer.append("<" + name + nsdecl + ">");
            break;
        case CLOSING:
            _buffer.append("</" + name + ">\n");
            break;
        case NO_CONTENT:
        default:
            _buffer.append("<" + name + nsdecl + "/>");
            break;
        }
    }

    /**
     * Write text.
     * 
     * @param text
     *      Text to append
     */
    public void writeText(String text) {
        _buffer.append(text);
    }

    /**
     * Write data.
     * 
     * @param data
     *      Data to append
     */
    public void writeData(String data) {
        _buffer.append("<![CDATA[" + data + "]]>");
    }

    /**
     * Write XML Header.
     */
    public void writeXMLHeader() {
        _buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    }

    /**
     * Send data and reinitializes buffer.
     */
    public void sendData() throws IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("sendData _writer: {} _buffer: {}", _writer, _buffer.toString());
        }
        if (_writer != null) {
            _writer.write(_buffer.toString());
            _writer.flush();
            _buffer = new StringBuffer();
        }
    }

}
