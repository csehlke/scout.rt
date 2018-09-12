/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Key = function(keyStroke, which) {
  this.keyStroke = keyStroke;
  this.which = which;

  this.ctrl = keyStroke.ctrl;
  this.alt = keyStroke.alt;
  this.shift = keyStroke.shift;

  this.keyStrokeMode = keyStroke.keyStrokeMode;
};

scout.Key.prototype.render = function($drawingArea, event) {
  this.$drawingArea = this.keyStroke.renderKeyBox($drawingArea, event);
  return !!this.$drawingArea;
};

scout.Key.prototype.remove = function() {
  this.keyStroke.removeKeyBox(this.$drawingArea);
  this.$drawingArea = null;
};

scout.Key.prototype.toKeyStrokeString = function() {
  var keyStroke = '';
  if (this.ctrl) {
    keyStroke += 'Ctrl-';
  }
  if (this.alt) {
    keyStroke += 'Alt-';
  }
  if (this.shift) {
    keyStroke += 'Shift-';
  }
  var key = scout.codesToKeys[this.which];
  if (key === undefined) {
    key = this.which;
  }
  keyStroke += key;
  return keyStroke;
};
