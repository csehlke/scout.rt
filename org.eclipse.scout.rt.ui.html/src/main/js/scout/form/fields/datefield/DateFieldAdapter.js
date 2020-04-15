/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DateFieldAdapter = function() {
  scout.DateFieldAdapter.parent.call(this);
  this._errorStatus = null;
  this._errorStatusDisplayText = null;
};
scout.inherits(scout.DateFieldAdapter, scout.ValueFieldAdapter);

scout.DateFieldAdapter.PROPERTIES_ORDER = ['hasTime', 'hasDate'];

/**
 * @override
 */
scout.DateFieldAdapter.prototype._initProperties = function(model) {
  scout.DateFieldAdapter.parent.prototype._initProperties.call(this, model);
  this._updateErrorStatus(model.errorStatus, model.displayText);
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._attachWidget = function() {
  scout.DateFieldAdapter.parent.prototype._attachWidget.call(this);
  this.widget.setValidator(function(value, defaultValidator) {
    // If the server reported an error for current display text,
    // make sure it will be shown in the UI if the user enters that display text again or selects the same date using the picker
    var displayText = this.formatValue(value);
    if (this.modelAdapter._errorStatus && displayText === this.modelAdapter._errorStatusDisplayText) {
      throw this.modelAdapter._errorStatus;
    }
    return defaultValidator(value);
  }.bind(this.widget), false);
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._onWidgetAcceptInput = function(event) {
  var data = {
      displayText: this.widget.displayText,
      errorStatus: this.widget.errorStatus
    };
  // In case of an error, the value is still the old, valid value -> don't send it
  if (!this.widget.errorStatus) {
    data.value = scout.dates.toJsonDate(this.widget.value);
  }
  this._send('acceptInput', data, {
    showBusyIndicator: !event.whileTyping,
    coalesce: function(previous) {
      return this.target === previous.target && this.type === previous.type;
    }
  });
  this._errorStatus = null;
  this._errorStatusDisplayText = null;
};

/**
 * @override
 */
scout.DateFieldAdapter.prototype._syncDisplayText = function(displayText) {
  // No need to call parseAndSetValue, the value will come separately
  this.widget.setDisplayText(displayText);
  this._updateErrorStatus(this._errorStatus, displayText);
};

/**
 * Make sure hasDate and hasTime are always set before displayText, otherwise toggling hasDate and hasTime dynamically
 * won't work because renderDisplayText would try to write the time into the date field
 *
 * @override
 */
scout.DateFieldAdapter.prototype._orderPropertyNamesOnSync = function(newProperties) {
  return Object.keys(newProperties).sort(this._createPropertySortFunc(scout.DateFieldAdapter.PROPERTIES_ORDER));
};

scout.DateFieldAdapter.prototype._syncErrorStatus = function(errorStatus) {
  if (errorStatus) {
    if (this.widget.errorStatus) {
      // Don't loose information which part was invalid
      errorStatus.invalidDate = this.widget.errorStatus.invalidDate;
      errorStatus.invalidTime = this.widget.errorStatus.invalidTime;
    } else {
      // If the error happened only on server side, we do not know which part was invalid.
      // Set both to true so that DateField._isDateValid / isTimeValid does not return true
      errorStatus.invalidDate = true;
      errorStatus.invalidTime = true;
    }
  }

  this._updateErrorStatus(errorStatus, this.widget.displayText);
  this.widget.setErrorStatus(errorStatus);
};

scout.DateFieldAdapter.prototype._updateErrorStatus = function(errorStatus, displayText) {
  // Find the first model error status. If server sends a UI error status (=PARSE_ERROR) then don't remember it
  errorStatus = scout.Status.ensure(errorStatus);
  var modelErrorStatus = null;
  if (errorStatus) {
    modelErrorStatus = scout.arrays.find(errorStatus.asFlatList(), function(status) {
      return status.code !== scout.DateField.ErrorCode.PARSE_ERROR;
    });
  }
  // Remember errorStatus from model
  if (modelErrorStatus) {
    this._errorStatus = modelErrorStatus;
    this._errorStatusDisplayText = displayText;
  } else {
    this._errorStatus = null;
    this._errorStatusDisplayText = null;
  }
};