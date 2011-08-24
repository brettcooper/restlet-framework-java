/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.oauth.util;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Utility for setting up a static html page resource. For more complex and
 * dynamic pages Freemarker is a better choice.
 * 
 * @author Kristoffer Gronowski
 */
public class StaticHtmlPage extends Finder {

    private String page;

    private MediaType type;

    public StaticHtmlPage(String uri) {
        Reference ref = new Reference(uri);
        // TODO could check that it is CLAP and ends .html
        ClientResource local = new ClientResource(ref);
        Representation tmpPage = local.get();
        try {
            page = tmpPage.getText();
        } catch (IOException e) {
            page = e.getLocalizedMessage();
        }
        type = tmpPage.getMediaType();
        tmpPage.release();
        local.release();
    }

    @Override
    public ServerResource find(Request request, Response response) {
        Representation result = new StringRepresentation(page, type);
        // page.setLocationRef(request.getResourceRef());
        return new StaticServerResource(result);
    }
}