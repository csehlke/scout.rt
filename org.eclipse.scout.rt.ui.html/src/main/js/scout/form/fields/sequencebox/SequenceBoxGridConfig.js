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
scout.SequenceBoxGridConfig = function() {
  scout.SequenceBoxGridConfig.parent.call(this);
};
scout.inherits(scout.SequenceBoxGridConfig, scout.LogicalGridConfig);

scout.SequenceBoxGridConfig.prototype.getGridColumnCount = function() {
  return this.widget.fields.length;
};

scout.SequenceBoxGridConfig.prototype.getGridWidgets = function() {
  return this.widget.fields;
};
