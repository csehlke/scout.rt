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
package org.eclipse.scout.rt.client.ui.wizard;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.annotations.OrderedCollection;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.splitbox.AbstractSplitBox;
import org.eclipse.scout.rt.client.ui.form.fields.wizard.AbstractWizardProgressField;
import org.eclipse.scout.rt.client.ui.form.fields.wrappedform.AbstractWrappedFormField;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.SplitBox;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.SplitBox.ContentBox;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.SplitBox.ContentBox.WrappedWizardForm;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.SplitBox.StatusBox;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.SplitBox.StatusBox.StatusField;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardCancelButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardFinishButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardNextStepButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardPreviousStepButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardProgressField;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardResetButton;
import org.eclipse.scout.rt.client.ui.wizard.DefaultWizardContainerForm.MainBox.WizardSuspendButton;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * <h3>DefaultWizardContainerForm</h3> A container form containing a wizard form
 * area (current step) a status area. see {@link AbstractWizard#execCreateContainerForm()}
 *
 * @since 24.11.2009
 */
public class DefaultWizardContainerForm extends AbstractWizardContainerForm implements IWizardContainerForm {

  public DefaultWizardContainerForm(IWizard wizard) throws ProcessingException {
    this(wizard, true);
  }

  public DefaultWizardContainerForm(IWizard wizard, boolean callInitializer) throws ProcessingException {
    super(wizard, false);
    if (callInitializer) {
      callInitializer();
    }
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public WizardProgressField getWizardProgressField() {
    return getFieldByClass(WizardProgressField.class);
  }

  public SplitBox getSplitBox() {
    return getFieldByClass(SplitBox.class);
  }

  public ContentBox getContentBox() {
    return getFieldByClass(ContentBox.class);
  }

  public StatusBox getStatusBox() {
    return getFieldByClass(StatusBox.class);
  }

  public WrappedWizardForm getWrappedWizardForm() {
    return getFieldByClass(WrappedWizardForm.class);
  }

  public StatusField getStatusField() {
    return getFieldByClass(StatusField.class);
  }

  @Override
  public IButton getWizardCancelButton() {
    return getFieldByClass(WizardCancelButton.class);
  }

  @Override
  public IButton getWizardSuspendButton() {
    return getFieldByClass(WizardSuspendButton.class);
  }

  @Override
  public IButton getWizardResetButton() {
    return getFieldByClass(WizardResetButton.class);
  }

  @Override
  public IButton getWizardPreviousStepButton() {
    return getFieldByClass(WizardPreviousStepButton.class);
  }

  @Override
  public IButton getWizardNextStepButton() {
    return getFieldByClass(WizardNextStepButton.class);
  }

  @Override
  public IButton getWizardFinishButton() {
    return getFieldByClass(WizardFinishButton.class);
  }

  @Override
  protected IForm getInnerWizardForm() {
    return getWrappedWizardForm().getInnerForm();
  }

  @Override
  protected void setInnerWizardForm(IForm form) {
    getWrappedWizardForm().setInnerForm(form);
  }

  @Override
  public void startWizard() throws ProcessingException {
    startInternal(new WizardHandler());
  }

  @Order(10.0f)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridW() {
      return 3;
    }

    @Override
    protected int getConfiguredGridH() {
      return 32;
    }

    @Override
    protected boolean getConfiguredGridUseUiHeight() {
      return false;
    }

    @Override
    protected int getConfiguredGridColumnCount() {
      return 2;
    }

    @Order(10.0)
    public class WizardProgressField extends AbstractWizardProgressField {
    }

    @Order(20.0)
    public class SplitBox extends AbstractSplitBox {

      @Override
      protected double getConfiguredSplitterPosition() {
        return 0.75;
      }

      @Override
      protected boolean getConfiguredLabelVisible() {
        return false;
      }

      @Order(10.0)
      public class ContentBox extends AbstractGroupBox {

        @Override
        protected boolean getConfiguredBorderVisible() {
          return false;
        }

        @Order(10.0)
        public class WrappedWizardForm extends AbstractWrappedFormField<IForm> {

          @Override
          protected int getConfiguredGridW() {
            return 2;
          }
        }
      }

      @Order(20.0)
      public class StatusBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected boolean getConfiguredBorderVisible() {
          return false;
        }

        @Override
        protected void injectFieldsInternal(OrderedCollection<IFormField> fields) {
          super.injectFieldsInternal(fields);
          // TODO BSH Inject info boxes and groups here
        }

        @Order(20.0)
        public class StatusField extends AbstractWizardStatusField {

          @Override
          protected int getConfiguredGridW() {
            return 2;
          }

          @Override
          protected int getConfiguredGridH() {
            return 2;
          }

          @Override
          protected void execAppLinkAction(String ref) throws ProcessingException {
            if (DefaultWizardContainerForm.this.getWizard() != null) {
              DefaultWizardContainerForm.this.getWizard().doAppLinkAction(ref);
            }
            else {
              super.execAppLinkAction(ref);
            }
          }
        }
      }
    }

    @Order(30.0)
    public class WizardPreviousStepButton extends AbstractButton {

      @Override
      protected String getConfiguredIconId() {
        return AbstractIcons.WizardBackButton;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardBackButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("WizardBackButtonTooltip");
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 1;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getWizard().doPreviousStep();
      }
    }

    @Order(40.0)
    public class WizardNextStepButton extends AbstractButton {

      @Override
      protected String getConfiguredIconId() {
        return AbstractIcons.WizardNextButton;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardNextButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("WizardNextButtonTooltip");
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 1;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getWizard().doNextStep();
      }
    }

    @Order(50.0)
    public class WizardFinishButton extends AbstractButton {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardFinishButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("WizardFinishButtonTooltip");
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return 1;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getWizard().doFinish();
      }
    }

    @Order(60.0)
    public class WizardCancelButton extends AbstractButton implements IButton {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardCancelButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("WizardCancelButtonTooltip");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return -1;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getWizard().doCancel();
      }
    }

    @Order(70.0)
    public class WizardSuspendButton extends AbstractButton {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("WizardSuspendButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("WizardSuspendButtonTooltip");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return -1;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getWizard().doSuspend();
      }
    }

    @Order(80.0)
    public class WizardResetButton extends AbstractButton implements IButton {

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("ResetButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("ResetButtonTooltip");
      }

      @Override
      protected boolean getConfiguredVisible() {
        return false;
      }

      @Override
      protected int getConfiguredHorizontalAlignment() {
        return -1;
      }

      @Override
      protected void execClickAction() throws ProcessingException {
        getWizard().doReset();
      }
    }

    public class EnterKeyStroke extends AbstractKeyStroke {

      @Override
      protected String getConfiguredKeyStroke() {
        return "enter";
      }

      @Override
      protected void execAction() throws ProcessingException {
        handleEnterKey();
      }
    }

    public class EscapeKeyStroke extends AbstractKeyStroke {

      @Override
      protected String getConfiguredKeyStroke() {
        return "escape";
      }

      @Override
      protected void execAction() throws ProcessingException {
        handleEscapeKey(false);
      }
    }
  }

  public class WizardHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      setInnerWizardForm(getWizard() == null ? null : getWizard().getWizardForm());
    }
  }
}
