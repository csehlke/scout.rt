/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.testing;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonRequest;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.mockito.Mockito;

public final class JsonTestUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonTestUtility.class);

  private JsonTestUtility() {
  }

  public static IJsonSession createAndInitializeJsonSession() {
    String jsonSessionId = "1.1";
    String clientSessionId = "testClientSession123";
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession httpSession = Mockito.mock(HttpSession.class);
    Mockito.when(request.getLocale()).thenReturn(new Locale("de_CH"));
    Mockito.when(request.getHeader("User-Agent")).thenReturn("dummy");
    Mockito.when(request.getSession()).thenReturn(httpSession);
    Mockito.when(request.getSession(false)).thenReturn(httpSession);
    Mockito.when(httpSession.getAttribute("scout.htmlui.session.client." + clientSessionId)).thenReturn(null);
    JSONObject jsonReqObj = new JSONObject();
    try {
      jsonReqObj.put(JsonRequest.PROP_JSON_SESSION_ID, jsonSessionId);
      jsonReqObj.put(JsonStartupRequest.PROP_CLIENT_SESSION_ID, clientSessionId);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
    JsonRequest jsonRequest = new JsonRequest(jsonReqObj);
    IJsonSession jsonSession = new TestEnvironmentJsonSession();
    jsonSession.init(request, new JsonStartupRequest(jsonRequest));
    return jsonSession;
  }

  /**
   * Empties the response object and flushes the session
   */
  public static void endRequest(IJsonSession jsonSession) throws Exception {
    Field field = AbstractJsonSession.class.getDeclaredField("m_currentJsonResponse");
    field.setAccessible(true);
    field.set(jsonSession, new JsonResponse());

    field = AbstractJsonSession.class.getDeclaredField("m_currentHttpRequest");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    AtomicReference<HttpServletRequest> ref = (AtomicReference<HttpServletRequest>) field.get(jsonSession);
    ref.set(null);

    jsonSession.flush();
  }

  /**
   * @param eventType
   *          Optional. If set only events with the given type will be returned.
   * @param adapterId
   *          Optional. If set only events for the given id will be returned.
   */
  public static List<JsonEvent> extractEventsFromResponse(JsonResponse response, String eventType, String adapterId) throws JSONException {
    List<JsonEvent> list = new ArrayList<>();
    for (JsonEvent event : response.getEventList()) {
      if ((eventType == null || event.getType().equals(eventType))
          && (adapterId == null || adapterId.equals(event.getTarget()))) {
        list.add(event);
      }
    }
    return list;
  }

  public static List<JsonEvent> extractEventsFromResponse(JsonResponse response, String eventType) throws JSONException {
    return extractEventsFromResponse(response, eventType, null);
  }

  public static List<JsonEvent> extractPropertyChangeEvents(JsonResponse response, String adapterId) throws JSONException {
    return extractEventsFromResponse(response, JsonEventType.PROPERTY.getEventType(), adapterId);
  }

  public static <T> T extractProperty(JsonResponse response, String adapterId, String propertyName) throws JSONException {
    List<JsonEvent> properties = JsonTestUtility.extractPropertyChangeEvents(response, adapterId);
    if (properties.size() > 0) {
      return extractProperty(properties.get(0).getData(), propertyName);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T extractProperty(JSONObject data, String propertyName) throws JSONException {
    return (T) data.getJSONObject("properties").get(propertyName);
  }

  public static void assertGC(WeakReference<?> ref) {
    int maxRuns = 50;
    for (int i = 0; i < maxRuns; i++) {
      if (ref.get() == null) {
        return;
      }
      System.gc();
      try {
        Thread.sleep(50);
      }
      catch (InterruptedException e) {
        // NOP
      }
    }
    Assert.fail("Potential memory leak, object " + ref.get() + "still exists after gc");
  }

  public static JSONObject getAdapterData(JSONObject json, String id) throws JSONException {
    return json.getJSONObject(JsonResponse.PROP_ADAPTER_DATA).getJSONObject(id);
  }

  public static JSONObject getEvent(JSONObject json, int index) throws JSONException {
    return (JSONObject) json.getJSONArray(JsonResponse.PROP_EVENTS).get(index);
  }

  public static JSONObject getPropertyChange(JSONObject json, int index) throws JSONException {
    return getEvent(json, index).getJSONObject("properties");
  }

  public static void initField(ICompositeField compositeField) {
    InitFieldVisitor visitor = new InitFieldVisitor();
    compositeField.visitFields(visitor, 0);
  }

  // copy from FormUtility
  private static class InitFieldVisitor implements IFormFieldVisitor {
    private ProcessingException m_firstEx;

    @Override
    public boolean visitField(IFormField field, int level, int fieldIndex) {
      try {
        field.initField();
      }
      catch (ProcessingException e) {
        if (m_firstEx == null) {
          m_firstEx = e;
        }
      }
      catch (Throwable t) {
        if (m_firstEx == null) {
          m_firstEx = new ProcessingException("Unexpected", t);
        }
      }
      return true;
    }

    public void handleResult() throws ProcessingException {
      if (m_firstEx != null) {
        throw m_firstEx;
      }
    }
  }

}
