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
package org.eclipse.scout.rt.ui.swing.form.fields.timefield;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.form.fields.timefield.ITimeField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.swing.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swing.SwingUtility;
import org.eclipse.scout.rt.ui.swing.basic.IconGroup;
import org.eclipse.scout.rt.ui.swing.basic.document.BasicDocumentFilter;
import org.eclipse.scout.rt.ui.swing.ext.IDropDownButtonListener;
import org.eclipse.scout.rt.ui.swing.ext.JPanelEx;
import org.eclipse.scout.rt.ui.swing.ext.JStatusLabelEx;
import org.eclipse.scout.rt.ui.swing.ext.JTextFieldWithDropDownButton;
import org.eclipse.scout.rt.ui.swing.ext.calendar.TimeChooser;
import org.eclipse.scout.rt.ui.swing.form.fields.SwingScoutValueFieldComposite;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewEvent;
import org.eclipse.scout.rt.ui.swing.window.SwingScoutViewListener;
import org.eclipse.scout.rt.ui.swing.window.popup.SwingScoutDropDownPopup;

public class SwingScoutTimeField extends SwingScoutValueFieldComposite<ITimeField> implements ISwingScoutTimeField {
  private static final long serialVersionUID = 1L;

  // cache
  private SwingScoutDropDownPopup m_proposalPopup;

  @Override
  protected void initializeSwing() {
    JPanelEx container = new JPanelEx();
    container.setOpaque(false);
    JStatusLabelEx label = getSwingEnvironment().createStatusLabel();
    container.add(label);
    //
    JTextComponent textField = createTextField(container);
    Document doc = textField.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new BasicDocumentFilter(60));
    }
    doc.addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        setInputDirty(true);
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        setInputDirty(true);
      }
    });
    // key mappings
    InputMap inputMap = textField.getInputMap(JTextField.WHEN_FOCUSED);
    inputMap.put(SwingUtility.createKeystroke("F2"), "timeChooser");
    ActionMap actionMap = textField.getActionMap();
    actionMap.put("timeChooser", new P_SwingTimeChooserAction());
    //
    setSwingContainer(container);
    setSwingLabel(label);
    setSwingField(textField);
    // layout
    getSwingContainer().setLayout(new LogicalGridLayout(getSwingEnvironment(), 1, 0));
  }

  /**
   * Create and add the text field to the container.
   * <p>
   * May add additional components to the container.
   */
  protected JTextComponent createTextField(JComponent container) {
    JTextFieldWithDropDownButton textField = new JTextFieldWithDropDownButton(getSwingEnvironment());
    textField.setIconGroup(new IconGroup(getSwingEnvironment(), AbstractIcons.DateFieldTime));
    container.add(textField);
    //
    textField.addDropDownButtonListener(new IDropDownButtonListener() {
      @Override
      public void iconClicked(Object source) {
        getSwingTextField().requestFocus();
        handleSwingTimeChooserAction();
      }

      @Override
      public void menuClicked(Object source) {
      }
    });
    return textField;
  }

  public JTextComponent getSwingTextField() {
    return (JTextComponent) getSwingField();
  }

  @Override
  protected void setForegroundFromScout(String scoutColor) {
    JComponent fld = getSwingField();
    if (fld != null && scoutColor != null && fld instanceof JTextComponent) {
      setDisabledTextColor(SwingUtility.createColor(scoutColor), (JTextComponent) fld);
    }
    super.setForegroundFromScout(scoutColor);
  }

  @Override
  protected void setDisplayTextFromScout(String s) {
    JTextComponent swingField = getSwingTextField();
    swingField.setText(s);
  }

  @Override
  protected void setEnabledFromScout(boolean b) {
    super.setEnabledFromScout(b);
    if (getSwingTextField() instanceof JTextFieldWithDropDownButton) {
      ((JTextFieldWithDropDownButton) getSwingTextField()).setDropDownButtonEnabled(b);
    }
  }

  @Override
  protected boolean handleSwingInputVerifier() {
    final String text = getSwingTextField().getText();
    // only handle if text has changed
    if (CompareUtility.equals(text, getScoutObject().getDisplayText()) && getScoutObject().getErrorStatus() == null) {
      return true;
    }
    final Holder<Boolean> result = new Holder<Boolean>(Boolean.class, false);
    // notify Scout
    Runnable t = new Runnable() {
      @Override
      public void run() {
        boolean b = getScoutObject().getUIFacade().setTextFromUI(text);
        result.setValue(b);
      }
    };
    JobEx job = getSwingEnvironment().invokeScoutLater(t, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    // end notify
    getSwingEnvironment().dispatchImmediateSwingJobs();
    return true; // continue always
  }

  @Override
  protected void handleSwingFocusGained() {
    super.handleSwingFocusGained();
    JTextComponent swingField = getSwingTextField();
    if (swingField.getDocument().getLength() > 0) {
      swingField.setCaretPosition(swingField.getDocument().getLength());
      swingField.moveCaretPosition(0);
    }
  }

  protected boolean isTimeChooserEnabled() {
    if (getSwingTextField() instanceof JTextFieldWithDropDownButton) {
      return (((JTextFieldWithDropDownButton) getSwingTextField()).isDropDownButtonEnabled());
    }
    return false;
  }

  protected void handleSwingTimeChooserAction() {
    // close old
    closePopup();
    if (isTimeChooserEnabled()) {
      final TimeChooser cal = new TimeChooser();
      Double d = getScoutObject().getValue();
      cal.setTimeAsAsDouble(d);
      //
      Action acceptAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
          acceptProposalFromSwing(cal.getTimeAsDouble());
        }
      };
      Action escAction = new AbstractAction() {
        private static final long serialVersionUID = 1L;

        public void actionPerformed(ActionEvent e) {
          closePopup();
        }
      };
      //add enter and escape keys to text field
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ENTER"), "enter");
      getSwingTextField().getActionMap().put("enter", acceptAction);
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      getSwingTextField().getActionMap().put("escape", escAction);
      //add enter and escape keys to popup
      JComponent popupContent = cal.getContainer();
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ENTER"), "enter");
      popupContent.getActionMap().put("enter", acceptAction);
      popupContent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtility.createKeystroke("ESCAPE"), "escape");
      popupContent.getActionMap().put("escape", escAction);
      cal.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          acceptProposalFromSwing(cal.getTimeAsDouble());
        }
      });
      //show popup (focusComponent must be null! to allow focus in popup window)
      m_proposalPopup = new SwingScoutDropDownPopup(getSwingEnvironment(), getSwingTextField(), getSwingTextField(), getSwingTextField().getWidth());
      m_proposalPopup.makeNonFocusable();
      m_proposalPopup.addSwingScoutViewListener(new SwingScoutViewListener() {
        public void viewChanged(SwingScoutViewEvent e) {
          if (e.getType() == SwingScoutViewEvent.TYPE_CLOSED) {
            closePopup();
          }
        }
      });
      m_proposalPopup.getSwingContentPane().add(popupContent);
      m_proposalPopup.openView();
    }
  }

  private void acceptProposalFromSwing(final Double newTime) {
    // close old
    closePopup();
    if (newTime != null) {
      // notify Scout
      Runnable t = new Runnable() {
        @Override
        public void run() {
          getScoutObject().getUIFacade().setTimeFromUI(newTime);
        }
      };
      getSwingEnvironment().invokeScoutLater(t, 0);
      // end notify
    }
  }

  private void closePopup() {
    if (m_proposalPopup != null) {
      m_proposalPopup.closeView();
      m_proposalPopup = null;
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ENTER"));
      getSwingTextField().getInputMap(JComponent.WHEN_FOCUSED).remove(SwingUtility.createKeystroke("ESCAPE"));
    }
  }

  /*
   * Swing actions
   */

  private class P_SwingTimeChooserAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public void actionPerformed(ActionEvent e) {
      handleSwingTimeChooserAction();
    }
  }// end private class

}
