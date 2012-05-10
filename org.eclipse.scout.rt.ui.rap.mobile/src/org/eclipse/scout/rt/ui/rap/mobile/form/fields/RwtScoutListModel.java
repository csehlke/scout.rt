/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.scout.rt.ui.rap.mobile.form.fields;

import java.util.HashMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.ui.rap.basic.table.RwtScoutTableEvent;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.swt.graphics.Image;

public class RwtScoutListModel implements IRwtScoutListModel {
  private static final long serialVersionUID = 1L;

  private transient ListenerList listenerList = null;
  private final ITable m_scoutTable;
  private HashMap<ITableRow, HashMap<IColumn<?>, ICell>> m_cachedCells;
  private final RwtScoutList m_uiList;
  private boolean m_multiline;

  public RwtScoutListModel(ITable scoutTable, RwtScoutList uiTable) {
    m_scoutTable = scoutTable;
    m_uiList = uiTable;
    rebuildCache();
  }

  @Override
  public void setMultiline(boolean multiline) {
    m_multiline = multiline;
  }

  @Override
  public boolean isMultiline() {
    return m_multiline;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (m_scoutTable != null) {
      return m_scoutTable.getFilteredRows();
    }
    else {
      return new Object[0];
    }
  }

  @Override
  public boolean isLabelProperty(Object element, String property) {
    return false;
  }

  @Override
  public void addListener(ILabelProviderListener listener) {
    if (listenerList == null) {
      listenerList = new ListenerList(ListenerList.IDENTITY);
    }
    listenerList.add(listener);
  }

  @Override
  public void removeListener(ILabelProviderListener listener) {
    if (listenerList != null) {
      listenerList.remove(listener);
      if (listenerList.isEmpty()) {
        listenerList = null;
      }
    }
  }

  private Object[] getListeners() {
    final ListenerList list = listenerList;
    if (list == null) {
      return new Object[0];
    }

    return list.getListeners();
  }

  @Override
  public void dispose() {
    if (listenerList != null) {
      listenerList.clear();
    }
  }

  protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
    Object[] listeners = getListeners();
    for (int i = 0; i < listeners.length; ++i) {
      final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
      SafeRunnable.run(new SafeRunnable() {
        private static final long serialVersionUID = 1L;

        @Override
        public void run() {
          l.labelProviderChanged(event);
        }
      });
    }
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public void consumeTableModelEvent(RwtScoutTableEvent uiTableEvent) {
    rebuildCache();
  }

  protected ICell getCell(Object row) {
    IColumn<?> column = m_scoutTable.getColumnSet().getVisibleColumns()[0];
    if (column != null) {
      if (m_cachedCells.get(row) == null) {
        rebuildCache();
      }
      return m_cachedCells.get(row).get(column);
    }
    else {
      return null;
    }
  }

  private void rebuildCache() {
    m_cachedCells = new HashMap<ITableRow, HashMap<IColumn<?>, ICell>>();
    if (m_scoutTable != null) {
      for (ITableRow scoutRow : m_scoutTable.getRows()) {
        HashMap<IColumn<?>, ICell> cells = new HashMap<IColumn<?>, ICell>();
        for (IColumn<?> col : m_scoutTable.getColumnSet().getVisibleColumns()) {
          cells.put(col, m_scoutTable.getCell(scoutRow, col));
        }
        m_cachedCells.put(scoutRow, cells);
      }
    }
  }

  @Override
  public RwtScoutList getRwtScoutList() {
    return m_uiList;
  }

  @Override
  public Image getImage(Object element) {
    //Has no effect on ListViewer
    return null;
  }

  @Override
  public String getText(Object element) {
    ICell cell = getCell(element);
    if (cell == null) {
      return "";
    }

    String text = cell.getText();
    if (text == null) {
      text = "";
    }
    if (HtmlTextUtility.isTextWithHtmlMarkup(text)) {
      text = m_uiList.getUiEnvironment().adaptHtmlCell(m_uiList, text);
      text = m_uiList.getUiEnvironment().convertLinksWithLocalUrlsInHtmlCell(m_uiList, text);
    }
    else if (text.indexOf("\n") >= 0) {
      if (isMultiline()) {
        //transform to html
        text = "<html>" + HtmlTextUtility.transformPlainTextToHtml(text) + "</html>";
        text = m_uiList.getUiEnvironment().adaptHtmlCell(m_uiList, text);
      }
      else {
        text = StringUtility.replace(text, "\n", " ");
      }
    }
    return text;
  }
}
