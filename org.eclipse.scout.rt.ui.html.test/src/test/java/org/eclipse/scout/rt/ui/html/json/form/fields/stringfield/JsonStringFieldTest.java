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
package org.eclipse.scout.rt.ui.html.json.form.fields.stringfield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.AbstractStringField;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.html.json.form.fields.BaseFormFieldTest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing: {@link AbstractStringField}
 */
public class JsonStringFieldTest extends BaseFormFieldTest {

  private AbstractStringField m_model = new AbstractStringField() {

    @Override
    protected boolean getConfiguredMultilineText() {
      return true;
    }

  };

  private JsonStringField m_stringField = new JsonStringField<IStringField>(m_model, m_session, m_session.createUniqueId(), null);

  @Before
  public void setUp() {
    m_stringField.init();
  }

  @Test
  public void testToJson() throws JSONException {
    JSONObject json = m_stringField.toJson();
    assertEquals(true, json.get(IStringField.PROP_MULTILINE_TEXT));
  }
}
