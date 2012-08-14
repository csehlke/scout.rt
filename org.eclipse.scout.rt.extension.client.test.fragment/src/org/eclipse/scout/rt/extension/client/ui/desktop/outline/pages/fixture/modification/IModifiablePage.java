/*******************************************************************************
 * Copyright (c) 2012 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.extension.client.ui.desktop.outline.pages.fixture.modification;

import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;

/**
 * @since 3.9.0
 */
public interface IModifiablePage extends IPage {

  void markModified();

  boolean isModified();
}
