/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.provider.context;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPException;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates unexpected exceptions which are not declared in WSDL.
 *
 * @since 6.1
 */
public class JaxWsUndeclaredExceptionTranslator implements IExceptionTranslator<WebServiceException> {

  private static final Logger LOG = LoggerFactory.getLogger(JaxWsUndeclaredExceptionTranslator.class);

  @Override
  public WebServiceException translate(final Throwable throwable) {
    final Throwable t = BEANS.get(DefaultExceptionTranslator.class).unwrap(throwable);

    if (t instanceof WebServiceException) {
      throw unsetStackTrace((WebServiceException) t);
    }
    else {
      LOG.error("Undeclared exception while processing webservice request", t);
      throw unsetStackTrace(new HTTPException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)); // Security: do not include exception details
    }
  }

  private WebServiceException unsetStackTrace(final WebServiceException e) {
    e.setStackTrace(new StackTraceElement[]{});
    return e;
  }
}
