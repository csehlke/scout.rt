/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.extension.ui.tile;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.tile.TilesChains.TilesSelectedChain;
import org.eclipse.scout.rt.client.ui.tile.AbstractTiles;
import org.eclipse.scout.rt.client.ui.tile.ITile;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractTilesExtension<T extends ITile, TILES extends AbstractTiles<T>> extends AbstractExtension<TILES> implements ITilesExtension<T, TILES> {

  public AbstractTilesExtension(TILES owner) {
    super(owner);
  }

  @Override
  public void execTilesSelected(TilesSelectedChain<T> chain, List<T> tiles) {
    chain.execTilesSelected(tiles);
  }

}
