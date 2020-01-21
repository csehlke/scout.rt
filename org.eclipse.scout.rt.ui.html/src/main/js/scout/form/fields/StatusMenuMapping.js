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
scout.StatusMenuMapping = function() {
  scout.StatusMenuMapping.parent.call(this);
  this.codes = [];
  this.severities = [];
  this.menu = null;
  this._addWidgetProperties(['menu']);
};
scout.inherits(scout.StatusMenuMapping, scout.Widget);

scout.StatusMenuMapping.prototype._createChild = function(model) {
  if (typeof model === 'string') {
    // If the model is a string it is probably the id of the menu.
    // Menus are defined by the parent (form field) -> search the parent's children for the menu
    var existingWidget = this.parent.widget(model);
    if (!existingWidget) {
      throw new Error('Referenced widget not found: ' + model);
    }
    return existingWidget;
  }
  return scout.StatusMenuMapping.parent.prototype._createChild(model);
};