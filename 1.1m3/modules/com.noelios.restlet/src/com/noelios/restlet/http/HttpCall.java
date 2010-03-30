/*
 * Copyright 2005-2008 Noelios Consulting.
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

package com.noelios.restlet.http;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.DateUtils;
import org.restlet.util.Series;

/**
 * Low-level call for the HTTP connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class HttpCall {

    /**
     * Formats a date as a header string.
     * 
     * @param date
     *                The date to format.
     * @param cookie
     *                Indicates if the date should be in the cookie format.
     * @return The formatted date.
     */
    public static String formatDate(Date date, boolean cookie) {
        if (cookie) {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1036.get(0));
        } else {
            return DateUtils.format(date, DateUtils.FORMAT_RFC_1123.get(0));
        }
    }

    /**
     * Parses a date string.
     * 
     * @param date
     *                The date string to parse.
     * @param cookie
     *                Indicates if the date is in the cookie format.
     * @return The parsed date.
     */
    public static Date parseDate(String date, boolean cookie) {
        if (cookie) {
            return DateUtils.parse(date, DateUtils.FORMAT_RFC_1036);
        } else {
            return DateUtils.parse(date, DateUtils.FORMAT_RFC_1123);
        }
    }

    /** The client IP address. */
    private volatile String clientAddress;

    /** The client port. */
    private volatile int clientPort;

    /** Indicates if the call is confidential. */
    private volatile boolean confidential;

    /** The hostRef domain. */
    private volatile String hostDomain;

    /** The hostRef port. */
    private volatile int hostPort;

    /** The logger to use. */
    private volatile Logger logger;

    /** The method. */
    private volatile String method;

    /** The exact protocol. */
    private volatile Protocol protocol;

    /** The reason phrase. */
    private volatile String reasonPhrase;

    /** The request headers. */
    private final Series<Parameter> requestHeaders;

    /** The request URI. */
    private volatile String requestUri;

    /** The response headers. */
    private final Series<Parameter> responseHeaders;

    /** The server IP address. */
    private volatile String serverAddress;

    /** The server port. */
    private volatile int serverPort;

    /** The status code. */
    private volatile int statusCode;

    /** The protocol version. */
    private volatile String version;

    /**
     * Constructor.
     */
    public HttpCall() {
        this.hostDomain = null;
        this.hostPort = -1;
        this.clientAddress = null;
        this.clientPort = -1;
        this.confidential = false;
        this.method = null;
        this.protocol = null;
        this.reasonPhrase = "";
        this.requestHeaders = new Form();
        this.requestUri = null;
        this.responseHeaders = new Form();
        this.serverAddress = null;
        this.serverPort = -1;
        this.statusCode = 200;
        this.version = null;
    }

    /**
     * Returns the client address.<br>
     * Corresponds to the IP address of the requesting client.
     * 
     * @return The client address.
     */
    public String getClientAddress() {
        return this.clientAddress;
    }

    /**
     * Returns the client port.<br>
     * Corresponds to the TCP/IP port of the requesting client.
     * 
     * @return The client port.
     */
    public int getClientPort() {
        return this.clientPort;
    }

    /**
     * Returns the connector service associated to a request.
     * 
     * @param request
     *                The request to lookup.
     * @return The connector service associated to a request.
     */
    public ConnectorService getConnectorService(Request request) {
        ConnectorService result = null;
        Application application = (Application) request.getAttributes().get(
                Application.KEY);

        if (application != null) {
            result = application.getConnectorService();
        } else {
            result = new ConnectorService();
        }

        return result;
    }

    /**
     * Returns the content length of the request entity if know,
     * {@link Representation#UNKNOWN_SIZE} otherwise.
     * 
     * @return The request content length.
     */
    protected long getContentLength(Series<Parameter> headers) {
        long contentLength = Representation.UNKNOWN_SIZE;

        // Extract the content length header
        for (Parameter header : headers) {
            if (header.getName().equalsIgnoreCase(
                    HttpConstants.HEADER_CONTENT_LENGTH)) {
                try {
                    contentLength = Long.parseLong(header.getValue());
                } catch (NumberFormatException e) {
                    contentLength = Representation.UNKNOWN_SIZE;
                }
            }
        }

        return contentLength;
    }

    /**
     * Returns the host domain.
     * 
     * @return The host domain.
     */
    public String getHostDomain() {
        return this.hostDomain;
    }

    /**
     * Returns the host port.
     * 
     * @return The host port.
     */
    public int getHostPort() {
        return this.hostPort;
    }

    /**
     * Returns the logger to use.
     * 
     * @return The logger to use.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Returns the request method.
     * 
     * @return The request method.
     */
    public String getMethod() {
        return this.method;
    }

    /**
     * Returns the exact protocol (HTTP or HTTPS).
     * 
     * @return The exact protocol (HTTP or HTTPS).
     */
    public Protocol getProtocol() {
        if (this.protocol == null)
            this.protocol = isConfidential() ? Protocol.HTTPS : Protocol.HTTP;
        return this.protocol;
    }

    /**
     * Returns the reason phrase.
     * 
     * @return The reason phrase.
     */
    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    /**
     * Returns the modifiable list of request headers.
     * 
     * @return The modifiable list of request headers.
     */
    public Series<Parameter> getRequestHeaders() {
        return this.requestHeaders;
    }

    /**
     * Returns the URI on the request line (most like a relative reference, but
     * not necessarily).
     * 
     * @return The URI on the request line.
     */
    public String getRequestUri() {
        return this.requestUri;
    }

    /**
     * Returns the modifiable list of server headers.
     * 
     * @return The modifiable list of server headers.
     */
    public Series<Parameter> getResponseHeaders() {
        return this.responseHeaders;
    }

    /**
     * Returns the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @return The response address.
     */
    public String getServerAddress() {
        return this.serverAddress;
    }

    /**
     * Returns the server port.
     * 
     * @return The server port.
     */
    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * Returns the status code.
     * 
     * @return The status code.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public int getStatusCode() throws IOException {
        return this.statusCode;
    }

    /**
     * Returns the protocol version used.
     * 
     * @return The protocol version used.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Indicates if the client wants a persistent connection.
     * 
     * @return True if the client wants a persistent connection.
     */
    protected abstract boolean isClientKeepAlive();

    /**
     * Indicates if the confidentiality of the call is ensured (ex: via SSL).
     * 
     * @return True if the confidentiality of the call is ensured (ex: via SSL).
     */
    public boolean isConfidential() {
        return this.confidential;
    }

    /**
     * Indicates if both the client and the server want a persistent connection.
     * 
     * @return True if the connection should be kept alive after the call
     *         processing.
     */
    protected boolean isKeepAlive() {
        return isClientKeepAlive() && isServerKeepAlive();
    }

    /**
     * Indicates if the request entity is chunked.
     * 
     * @return True if the request entity is chunked.
     */
    protected boolean isRequestChunked() {
        String header = getRequestHeaders().getFirstValue(
                HttpConstants.HEADER_TRANSFER_ENCODING, true);
        return (header != null) && header.equalsIgnoreCase("chunked");
    }

    /**
     * Indicates if the response entity is chunked.
     * 
     * @return True if the response entity is chunked.
     */
    protected boolean isResponseChunked() {
        String header = getResponseHeaders().getFirstValue(
                HttpConstants.HEADER_TRANSFER_ENCODING, true);
        return (header != null) && header.equalsIgnoreCase("chunked");
    }

    /**
     * Indicates if the server wants a persistent connection.
     * 
     * @return True if the server wants a persistent connection.
     */
    protected abstract boolean isServerKeepAlive();

    /**
     * Sets the client address.
     * 
     * @param clientAddress
     *                The client address.
     */
    protected void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    /**
     * Sets the client port.
     * 
     * @param clientPort
     *                The client port.
     */
    protected void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    /**
     * Indicates if the confidentiality of the call is ensured (ex: via SSL).
     * 
     * @param confidential
     *                True if the confidentiality of the call is ensured (ex:
     *                via SSL).
     */
    protected void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    /**
     * Sets the host domain name.
     * 
     * @param hostDomain
     *                The baseRef domain name.
     */
    public void setHostDomain(String hostDomain) {
        this.hostDomain = hostDomain;
    }

    /**
     * Sets the host port.
     * 
     * @param hostPort
     *                The host port.
     */
    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    /**
     * Sets the logger to use.
     * 
     * @param logger
     *                The logger to use.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the request method.
     * 
     * @param method
     *                The request method.
     */
    protected void setMethod(String method) {
        this.method = method;
    }

    /**
     * Sets the exact protocol used (HTTP or HTTPS).
     * 
     * @param protocol
     *                The protocol.
     */
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the reason phrase.
     * 
     * @param reasonPhrase
     *                The reason phrase.
     */
    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Sets the full request URI.
     * 
     * @param requestUri
     *                The full request URI.
     */
    protected void setRequestUri(String requestUri) {
        if ((requestUri == null) || (requestUri.equals(""))) {
            requestUri = "/";
        }

        this.requestUri = requestUri;
    }

    /**
     * Sets the response address.<br>
     * Corresponds to the IP address of the responding server.
     * 
     * @param responseAddress
     *                The response address.
     */
    public void setServerAddress(String responseAddress) {
        this.serverAddress = responseAddress;
    }

    /**
     * Sets the server port.
     * 
     * @param serverPort
     *                The server port.
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Sets the status code.
     * 
     * @param code
     *                The status code.
     */
    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    /**
     * Sets the protocol version used.
     * 
     * @param version
     *                The protocol version used.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}