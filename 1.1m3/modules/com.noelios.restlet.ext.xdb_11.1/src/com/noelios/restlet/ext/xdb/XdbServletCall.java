/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.xdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Series;

import com.noelios.restlet.http.HttpServerCall;

/**
 * Call that is used by the XDB Servlet HTTP connector. This is a downgrade
 * version to Servlet 2.2 of ServletCall class.
 * 
 * @see com.noelios.restlet.ext.servlet.ServletCall
 * @author Marcelo F. Ochoa (mochoa@ieee.org)
 */
public class XdbServletCall extends HttpServerCall {
    /** The HTTP Servlet request to wrap. */
    private volatile HttpServletRequest request;

    /** The HTTP Servlet response to wrap. */
    private volatile HttpServletResponse response;

    /** The request headers. */
    private volatile Series<Parameter> requestHeaders;

    /**
     * Constructor.
     * 
     * @param logger
     *                The logger.
     * @param serverAddress
     *                The server IP address.
     * @param serverPort
     *                The server port.
     * @param request
     *                The Servlet request.
     * @param response
     *                The Servlet response.
     */
    public XdbServletCall(Logger logger, String serverAddress, int serverPort,
            HttpServletRequest request, HttpServletResponse response) {
        super(logger, serverAddress, serverPort);
        this.request = request;
        this.response = response;
    }

    /**
     * Constructor.
     * 
     * @param server
     *                The parent server.
     * @param request
     *                The HTTP Servlet request to wrap.
     * @param response
     *                The HTTP Servlet response to wrap.
     */
    public XdbServletCall(Server server, HttpServletRequest request,
            HttpServletResponse response) {
        super(server);
        this.request = request;
        this.response = response;
    }

    @Override
    public String getMethod() {
        return getRequest().getMethod();
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.valueOf(getRequest().getScheme());
    }

    /**
     * Returns the HTTP Servlet request.
     * 
     * @return The HTTP Servlet request.
     */
    public HttpServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ReadableByteChannel getRequestEntityChannel(long size) {
        // Can't do anything
        return null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        try {
            return getRequest().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public ReadableByteChannel getRequestHeadChannel() {
        // Not available
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Series<Parameter> getRequestHeaders() {
        if (this.requestHeaders == null) {
            this.requestHeaders = new Form();

            // Copy the headers from the request object
            String headerName;
            String headerValue;
            for (Enumeration<String> names = getRequest().getHeaderNames(); names
                    .hasMoreElements();) {
                headerName = names.nextElement();
                for (Enumeration<String> values = getRequest().getHeaders(
                        headerName); values.hasMoreElements();) {
                    headerValue = values.nextElement();
                    this.requestHeaders.add(new Parameter(headerName,
                            headerValue));
                }
            }
        }

        return this.requestHeaders;
    }

    @Override
    public InputStream getRequestHeadStream() {
        // Not available
        return null;
    }

    /**
     * Returns the full request URI.
     * 
     * @return The full request URI.
     */
    @Override
    public String getRequestUri() {
        String queryString = getRequest().getQueryString();

        if ((queryString == null) || (queryString.equals(""))) {
            return getRequest().getRequestURI();
        } else {
            return getRequest().getRequestURI() + '?' + queryString;
        }
    }

    /**
     * Returns the HTTP Servlet response.
     * 
     * @return The HTTP Servlet response.
     */
    public HttpServletResponse getResponse() {
        return this.response;
    }

    @Override
    public WritableByteChannel getResponseEntityChannel() {
        // Can't do anything
        return null;
    }

    /**
     * Returns the response stream if it exists.
     * 
     * @return The response stream if it exists.
     */
    @Override
    public OutputStream getResponseEntityStream() {
        try {
            return getResponse().getOutputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getVersion() {
        String result = null;
        int index = getRequest().getProtocol().indexOf('/');

        if (index != -1) {
            result = getRequest().getProtocol().substring(index + 1);
        }

        return result;
    }

    @Override
    public boolean isConfidential() {
        return getRequest().isSecure();
    }

    @Override
    public List<Certificate> getSslClientCertificates() {
        Certificate[] certificateArray = (Certificate[]) getRequest()
                .getAttribute("javax.servlet.request.X509Certificate");
        if (certificateArray != null) {
            return Arrays.asList(certificateArray);
        } else {
            return null;
        }
    }

    @Override
    public String getSslCipherSuite() {
        return (String) getRequest().getAttribute(
                "javax.servlet.request.cipher_suite");
    }

    @Override
    public Integer getSslKeySize() {
        Integer keySize = (Integer) getRequest().getAttribute(
                "javax.servlet.request.key_size");
        if (keySize == null) {
            keySize = super.getSslKeySize();
        }
        return keySize;
    }

    /**
     * Sends the response back to the client. Commits the status, headers and
     * optional entity and send them on the network.
     * 
     * @param response
     *                The high-level response.
     * @throws IOException
     */
    @Override
    public void sendResponse(Response response) throws IOException {
        // Add the response headers
        Parameter header;
        for (Iterator<Parameter> iter = getResponseHeaders().iterator(); iter
                .hasNext();) {
            header = iter.next();
            getResponse().addHeader(header.getName(), header.getValue());
        }

        // Set the status code in the response. We do this after adding the
        // headers because when we have to rely on the 'sendError' method,
        // the Servlet containers are expected to commit their response.
        if (Status.isError(getStatusCode()) && (response == null)) {
            try {
                getResponse().sendError(getStatusCode(), getReasonPhrase());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unable to set the response error status", ioe);
            }
        } else {
            // Send the response entity
            getResponse().setStatus(getStatusCode());
            super.sendResponse(response);
        }
    }

}