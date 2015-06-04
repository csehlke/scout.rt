scout.DateField = function() {
  scout.DateField.parent.call(this);
  this.$timeField;
  this.$dateField;
  this.$timeFieldIcon;

};
scout.inherits(scout.DateField, scout.ValueField);

scout.DateField.prototype.init = function(model, session) {
  scout.DateField.parent.prototype.init.call(this, model, session);
  this.internalTimeParseDateFormat = new scout.DateFormat(session.locale, 'HHmmssSSS');
};

scout.DateField.prototype._render = function($parent) {
  this.addContainer($parent, 'date-field', new scout.DateFieldLayout(this));
  this.addField($.makeDiv());
  this.addLabel();
  this.addMandatoryIndicator();
  this._mouseDownListener = this._onMouseDownFocusContext.bind(this);
  this._dateFormat = new scout.DateFormat(this.session.locale, this.fullDatePattern);
};

scout.DateField.prototype._renderProperties = function() {
  this._renderHasTime();
  this._renderHasDate();
  this._renderTimestamp(this.timestamp);
  scout.DateField.parent.prototype._renderProperties.call(this);
};

scout.DateField.prototype._renderHasTime = function() {
  if (this.hasTime) {
    this.$timeField = scout.fields.new$TextField()
      .addClass('time')
      .blur(this._onFieldBlurTime.bind(this));
    this.$field.append(this.$timeField);
    this.$timeFieldIcon = $('<span>')
      .addClass('icon')
      .addClass('time')
      .click(this._onIconClickTime.bind(this))
      .appendTo(this.$field);

    this.isolatedTimeFormat = new scout.DateFormat(this.session.locale, this.timeFormatPattern);
    this.$timeField.val(this.isolatedTimeFormat.format(new Date(this.timestamp)));

  } else if (this.$timeField) {
    this.$timeField.remove();
    this.$timeFieldIcon.remove();
    this.$timeField=null;
  }
  this.htmlComp.valid=false;
  this.layout();
};

scout.DateField.prototype._onIconClickTime = function(event) {
  this.$timeField.focus();
};

scout.DateField.prototype._onIconClick = function(event) {
  this.openPicker();
  this.$dateField.focus();
};

scout.DateField.prototype.timestampChanged = function() {
  var date;
  if(this.hasTime && this.hasDate){
    date = this._dateFormat.fusionDateTime(this.isolatedDateFormat.parse(this.$dateField.val()),
        this.isolatedTimeFormat.parse(this.$timeField.val()));
  }
  else if(this.hasTime){
    date=this.isolatedTimeFormat.parse(this.$timeField.val());
  }
  else if(this.hasDate){
    date=this.isolatedDateFormat.parse(this.$dateField.val());
  }


  var timestamp = date?date.getTime():null,
    oldTimestamp = this.timestamp;
  if (oldTimestamp === timestamp) {
    return;
  }
  this.timestamp = timestamp;
  this._sendTimestampChanged(timestamp);
};

scout.DateField.prototype._sendTimestampChanged = function(timestamp) {
  this.session.send(this.id, 'timestampChanged', {
    timestamp: timestamp});
};

scout.DateField.prototype._renderHasDate = function() {
  if (this.hasDate) {
    this.$dateField = scout.fields.new$TextField()
      .focus(this._onFieldFocus.bind(this))
      .blur(this._onFieldBlurDate.bind(this))
      .keydown(this._onKeyDownDate.bind(this))
      .click(this._onClick.bind(this))
      .addClass('date');
    this.$field.prepend(this.$dateField);
    this.isolatedDateFormat = new scout.DateFormat(this.session.locale, this.dateFormatPattern);
    this._picker = new scout.DatePicker(this.isolatedDateFormat, this);
    this.$dateField.val(this.isolatedDateFormat.format(new Date(this.timestamp)));
    this.addIcon(this.$field);
  } else if (this.$dateField) {
    this.$dateField.remove();
    this.$icon.remove();
    this.$dateField=null;
  }
  this.addStatus(this.$field);
  this.htmlComp.valid=false;
  this.layout();
};
/**
 * @Override FormField.js
 */
scout.DateField.prototype._renderEnabled = function() {
  scout.DateField.parent.prototype._renderEnabled.call(this);
  this.$container.setEnabled(this.enabled);
  if (this.$dateField) {
    this.$dateField.setEnabled(this.enabled);
  }
  if (this.$timeField) {
    this.$timeField.setEnabled(this.enabled);
  }
};

scout.DateField.prototype._onFieldFocus = function() {
  if (!this._$predict || this._$predict.length === 0) {
//    setTimeout(function(){
      this._$predict = this._createPredictionField();
//    }.bind(this));
  }
};

scout.DateField.prototype._onFieldBlurDate = function() {
  this.closeOnClickOutsideOrFocusLost();
};

scout.DateField.prototype._onFieldBlurTime = function() {
  if (!this.hasTime && !this.$timeField) {
    return;
  }
  if(!this.$timeField.val()){
    return;
  }
  var date = this._parseTime(this.$timeField.val());
  if (!date) {
    return;
  }

  this.errorStatus=null;
  this.timeError = false;
  if(isNaN( date.getTime() )){
    this.timeError=true;
      this.errorStatus= {
        message: this.session.text('InvalidDateFormat')
      };
  }
  this._renderErrorStatus(this.errorStatus);

  if(!this.timeError){
    this.$timeField.val(this.isolatedTimeFormat.format(date));
  }
  this.timestampChanged();
};

scout.DateField.prototype.closeOnClickOutsideOrFocusLost = function() {
  if (!this._picker.isOpen() || !this._$predict) {
    return;
  }
  this._acceptPrediction();

  // Only update model if date is valid (according to ui)
  if (!this.errorStatusUi) {
    this.timestampChanged();
  }

  this._$predict.remove();
  this._$predict = null;
  this._picker.close();
  $(document).off('mousedown', this._mouseDownListener);

};

scout.DateField.prototype._onMouseDownFocusContext = function(event) {
  var $target = $(event.target),
    insidePopup = (this._picker && this._picker.$popup && this._picker.$popup.has($target).length > 0),
    insideContainer = (this.$container && this.$container.has($target).length > 0);
  // close the popup only if the click happened outside of the popup
  if (!insidePopup && !insideContainer) {
    this.closeOnClickOutsideOrFocusLost();
  }
};

scout.DateField.prototype._onClick = function() {
  this.openPicker();
};

scout.DateField.prototype._onIconClick = function(event) {
  scout.DateField.parent.prototype._onIconClick.call(this, event);
  this.openPicker();
};

/**
 * Opens picker and selects date
 */
scout.DateField.prototype.openPicker = function() {
  $(document).on('mousedown', this._mouseDownListener);
  this._updateSelection(this.$dateField.val());
};

/**
 * Called by datepicker when a date has been selected
 */
scout.DateField.prototype.onDateSelected = function(date) {
  var text = this.isolatedDateFormat.format(date);
  this._renderTimestamp(date);
};

/**
 * @Override
 */
scout.DateField.prototype._renderDisplayText = function(text) {
 //nop -> handled in _renderTimestamp
  this.displayText=text;
};

scout.DateField.prototype._renderTimestamp = function(timestamp){
  if((timestamp && timestamp==="")|| timestamp===undefined){
    if (this.hasDate && date) {
      this.$dateField.val('');
      // Make sure there is no invisible and wrong prediction
      if (this._$predict) {
        this._$predict.val('');
      }
    }
    if (this.hasTime && date) {
      this.$timeField.val('');
    }
    return;
  }
  var date = new Date(timestamp);
  if (this.hasDate && date) {
    this.$dateField.val(this.isolatedDateFormat.format(date));
    // Make sure there is no invisible and wrong prediction
    if (this._$predict) {
      this._$predict.val('');
    }
  }
  if (this.hasTime && date) {
    this.$timeField.val(this.isolatedTimeFormat.format(date));
  }
};

/**
 * @override
 */
scout.DateField.prototype._renderErrorStatus = function(errorStatus) {
  errorStatus = this.errorStatusUi || this.errorStatus;
  this.$container.toggleClass('has-error', !! errorStatus);

  if (this.$dateField ) {
    this.$dateField.toggleClass('has-error', !! this.dateError);

  }
  if (this.$timeField ) {
    this.$timeField.toggleClass('has-error', !! this.timeError);

  }

  if (errorStatus) {
    this._showStatusMessage({
      autoRemove: false
    });
  } else {
    this._hideStatusMessage();
  }

  if (this._$predict) {
    this._$predict.toggleClass('has-error', !! errorStatus);
  }
  if (this.hasDate && this._picker.$popup) {
    this._picker.$popup.toggleClass('has-error', !! errorStatus);
  }
};



scout.DateField.prototype._onKeyDownDate = function(event) {
  var years = 0,
    months = 0,
    days = 0,
    diff = 0,
    cursorPos = this.$dateField[0].selectionStart,
    displayText = this.$dateField.val(),
    prediction = this._$predict.val();

  if (event.which === scout.keys.TAB ||
    event.which === scout.keys.SHIFT) {
    return;
  }
  if (event.which === scout.keys.ENTER) {
    // Update model and close picker
    this.timestampChanged();
    if (this._picker.isOpen()) {
      this._picker.close();
      $(document).off('mousedown', this._mouseDownListener);
      event.stopPropagation();
    }
    return;
  }
  if (event.which === scout.keys.ESC) {
    if (this._picker.isOpen()) {
      this._picker.close();
      $(document).off('mousedown', this._mouseDownListener);
      event.stopPropagation();
    }
    return;
  }

  if (event.which === scout.keys.RIGHT && cursorPos === displayText.length) {
    //Move cursor one right and apply next char of the prediction
    if (prediction) {
      this.$dateField.val(prediction.substring(0, displayText.length + 1));
    }
    return;
  }

  if (event.which === scout.keys.PAGE_UP || event.which === scout.keys.PAGE_DOWN) {
    months = (event.which === scout.keys.PAGE_UP ? -1 : 1);
    this._picker.shiftSelectedDate(0, months, 0);
    return;
  }

  if (event.which === scout.keys.UP || event.which === scout.keys.DOWN) {
    diff = (event.which === scout.keys.UP ? -1 : 1);

    var modifierCount = (event.ctrlKey ? 1 : 0) + (event.shiftKey ? 1 : 0) + (event.altKey ? 1 : 0) + (event.metaKey ? 1 : 0);
    if (modifierCount === 1 && event.ctrlKey) {
      years = diff;
    } else if (modifierCount === 1 && event.shiftKey) {
      months = diff;
    } else if (modifierCount === 0) {
      days = diff;
    }

    this._picker.shiftSelectedDate(years, months, days);
    return false;
  }

  // Use set timeout because field value is not set when keyDown is fired (keyDown is used because keyUp feels laggy).
  setTimeout(function(e) {
    if (!this._$predict) {
      // Return if $predict was already removed (e.g. by focus lost)
      return;
    }
    displayText = this.$dateField.val();
    var predictedDateText = '';
    var valid = this.validateDisplayText(displayText);
    if (displayText && valid) {
      predictedDateText = this._predict(displayText);
    }
    this._$predict.val(predictedDateText.display);
    this._updateSelection(predictedDateText.full);
  }.bind(this), 1);
};

scout.DateField.prototype._updateSelection = function(displayText) {
  var date = this._dateFormat.parse(displayText);
  this._picker.selectDate(date);
};

/**
 * Analyzes the text and checks whether all relevant parts are filled.<p>
 * If year is provided, month and day need to be provided as well.<br>
 * If no year but a month is provided day needs to be provided as well.
 * @return errorStatus
 */
scout.DateField.prototype._validateDisplayText = function(text) {
  if (!text) {
    return;
  }

  //FIXME CGU what if text is 12. Juli 2014 -> wrong? actually depends on pattern... check with cru prototype
  //FIXME CGU optimize validation -> 1a.12.2003 currently is valid because parseInt strips 'a' maybe better use regexp. Also 10....02.2014 is currently valid
  var dateInfo = this._dateFormat.analyze(text, true);
  var day = dateInfo.day,
    month = dateInfo.month,
    year = dateInfo.year;
  var valid = false;

  if (year) {
    valid = day >= 0 && day < 32 && month >= 0 && month < 13 && year > 0 && year < 9999;
  } else if (month) {
    valid = day >= 0 && day < 32 && month >= 0 && month < 13;
  } else if (day) {
    valid = day >= 0 && day < 32;
  }
  this.dateError =!valid;
  if (!valid) {
    return {
      message: this.session.text('InvalidDateFormat')
    };
  }
};

/**
 * @return true if valid, false if not
 */
scout.DateField.prototype.validateDisplayText = function(text) {
  this.errorStatusUi = this._validateDisplayText(text);
  this._renderErrorStatus(this.errorStatusUi);
  return !this.errorStatusUi;
};

scout.DateField.prototype._acceptPrediction = function() {
  if (!this._$predict) {
    return;
  }
  var prediction = this._$predict.val();
  if (!prediction) {
    return;
  }
  prediction = this._predict(prediction, true);
  this.$dateField.val(prediction.full);
  this._updateSelection(prediction.full);
};

scout.DateField.prototype._predict = function(validatedText, format) {
  // TODO BSH Date | Check this code
  var now = new Date();
  var currentYear = String(now.getFullYear());
  var dateInfo = this._dateFormat.analyze(validatedText);
  var day = dateInfo.day,
    month = dateInfo.month,
    year = dateInfo.year;

  if (!day) {
    day = ('0' + (now.getDate())).slice(-2);
  }
  if (!month) {
    month = ('0' + (now.getMonth() + 1)).slice(-2);
  }

  if (year) {
    if (year.length === 1 && year.substr(0, 1) === '0') {
      year += '9';
    }
    if (year.length === 1 && year.substr(0, 1) === '1') {
      year += year.substr(3, 1);
    }
    if (year.substr(0, 1) === '2') {
      year += currentYear.substr(year.length, 4 - year.length);
    }
    if (year.substr(0, 2) === '19') {
      year += '1999'.substr(year.length, 4 - year.length);
    }
  } else {
    year = currentYear;
  }
  var prediction = {};
  var fullDay = scout.strings.padZeroLeft(day, 2);
  var fullMonth = scout.strings.padZeroLeft(month, 2);
  var fullYear;
  if (year < 100) {
    fullYear = 2000 + Number(year);
  } else {
    fullYear = year;
  }

  prediction.full = this.isolatedDateFormat.pattern.replace('dd', fullDay).replace('MM', fullMonth).replace('yyyy', fullYear);
  prediction.display = this.isolatedDateFormat.pattern.replace('dd', day).replace('MM', month).replace('yyyy', year);

  return prediction;
};

scout.DateField.prototype._createPredictionField = function() {
  var $predict = this.$dateField
    .clone()
    .addClass('predict')
    .attr('tabIndex', '-1');

  $predict.val('');
  this.$dateField.before($predict);
  return $predict;
};

scout.DateField.prototype._parseTime = function(inputValue) {
  var timeString = '';
  var isPM = false;
  inputValue = inputValue.replace(':', '');
  inputValue = inputValue.replace('.', '');
  inputValue = inputValue.replace(/am/i, '');
  inputValue = inputValue.replace(/a.m./i, '');
  if (inputValue.match(/pm/i) || inputValue.match(/p.m./i)) {
    inputValue = inputValue.replace(/pm/i, '');
    inputValue = inputValue.replace(/p.m./i, '');
    inputValue = inputValue.trim();
    isPM = true;
  }
  if (inputValue.length === 1 || inputValue.length === 3) {
    timeString += '0';
    timeString += inputValue;
  } else {
    timeString = inputValue;
  }
  timeString = timeString + scout.strings.repeat('0', 9 - timeString.length);
  if (isPM) {
    //add 12 hours before parsing
    timeString = Number(timeString) + 120000000;
  }
  return this.internalTimeParseDateFormat.parse(timeString);
};

scout.DateField.prototype._createKeyStrokeAdapter = function() {
  return new scout.DateFieldKeyStrokeAdapter(this);
};
