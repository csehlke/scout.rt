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
package org.eclipse.scout.rt.client.mobile.transformation;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPageWithTable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * @since 3.9.0
 */
public interface IDeviceTransformer {
  void desktopInit(IDesktop desktop);

  void desktopGuiAttached() throws ProcessingException;

  void desktopGuiDetached() throws ProcessingException;

  void tablePageLoaded(IPageWithTable<?> tablePage) throws ProcessingException;

  void transformForm(IForm form) throws ProcessingException;

  void transformOutline(IOutline outline) throws ProcessingException;

  void transformPageDetailTable(ITable table) throws ProcessingException;

  boolean acceptForm(IForm form);

  void adaptFormHeaderLeftActions(IForm form, List<IMenu> menuList);

  void adaptFormHeaderRightActions(IForm form, List<IMenu> menuList);
}
