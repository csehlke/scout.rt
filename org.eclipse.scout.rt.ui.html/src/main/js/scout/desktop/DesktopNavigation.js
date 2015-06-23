// FIXME NBU/AWE: inherit from Widget.js? refactor un-/installKeyStroke
scout.DesktopNavigation = function(desktop) {
  this.desktop = desktop;
  this.session = desktop.session;

  this.$navigation;
  this.$header;
  this.$container;

  this.activeTab;
  this.outlineTab;
  this.searchTab;
  this.$queryField;
  this.$outlineTitle;
  this.previousOutline;
  this.breadcrumbSwitchWidth = 190;
  this.searchFieldKeyStrokeAdapter;
  this.keyStrokeAdapter = this._createKeyStrokeAdapter();
};

scout.DesktopNavigation.prototype.render = function($parent) {
  this.$navigation = $parent.appendDiv('desktop-navigation');
  this.$header = this.$navigation.appendDiv('navigation-header');

  this.outlineTab = new scout.DesktopNavigation.TabAndContent(this._createOutlinesTab());
  this.outlineTab.$tab.on('click', function() {
    // Switch tab if search outline is selected. Otherwise outline menu gets opened
    if (this.desktop.outline === this.desktop.searchOutline) {
      if (!this.previousOutline) {
        // Open menu if previous outline is not set.
        // Happens after reloading the page. Reason: The model does know nothing about the previous outline
        this._openMenu();
      } else {
        this._selectTab(this.outlineTab, this.previousOutline);
      }
    }
  }.bind(this));

  if (this.desktop.searchOutline) {
    this.searchTab = new scout.DesktopNavigation.TabAndContent(this._createSearchTab());
    this.searchTab.$tab.on('click', function() {
      this._selectTab(this.searchTab, this.desktop.searchOutline);
    }.bind(this));
    this.$header.addClass('search-available');
  }

  this.$container = this.$navigation.appendDiv('navigation-container');
  this._installKeyStrokeAdapter();
};

scout.DesktopNavigation.prototype._selectTab = function(tab, outline) {
  this.desktop.changeOutline(outline);
  this.session.send(this.desktop.id, 'outlineChanged', {
    outlineId: outline.id
  });
  this._setActiveTab(tab);
};

// outline tab creation

scout.DesktopNavigation.prototype._createOutlinesTab = function() {
  var that = this;

  // create tab
  var $tab = this.$header.appendDiv('navigation-tab-outline');

  // create button
  this.$menuButton = $tab.appendDiv('navigation-tab-outline-button')
    .on('mousedown', this._onMenuButtonClicked.bind(this));

  // create title of active outline
  var $outlineTitle = $tab.appendDiv('navigation-tab-outline-title');

  $outlineTitle.click(function() {
    that.outline.clearSelection();
    that.outline.collapseAll();
  });

  // save and return
  this.$outlineTitle = $outlineTitle;
  return $tab;
};

scout.DesktopNavigation.prototype._onMenuButtonClicked = function(event) {
  if (this.activeTab === this.outlineTab) {
    this._openMenu();
    event.stopPropagation();
    event.stopImmediatePropagation();
  }
};

scout.DesktopNavigation.prototype._openMenu = function(event) {
  this.popup = new scout.DesktopNavigationPopup(this, this.session);
  this.popup.render();
};

scout.DesktopNavigation.prototype._createSearchTab = function() {
  // create tab
  var $tab = this.$header.appendDiv('navigation-tab-search');

  // create field
  this.$queryField = $('<input>')
    .addClass('navigation-tab-search-field')
    .placeholder(this.session.text('SearchFor_'))
    .on('input', this._onQueryFieldInput.bind(this))
    .on('keypress', this._onQueryFieldKeyPress.bind(this))
    .appendTo($tab);

  // create button
  $tab.appendDiv('navigation-tab-search-button')
    .on('click', this._onSearchButtonClick.bind(this));

  // add keystroke adapter for search field.
  if (!this.searchFieldKeyStrokeAdapter) {
    this.searchFieldKeyStrokeAdapter = new scout.SearchFieldKeyStrokeAdapter(this);
  }
  // reinstall
  scout.keyStrokeManager.uninstallAdapter(this.searchFieldKeyStrokeAdapter);
  scout.keyStrokeManager.installAdapter(this.$queryField, this.searchFieldKeyStrokeAdapter);

  return $tab;
};

scout.DesktopNavigation.prototype.renderSearchQuery = function(searchQuery) {
  this.$queryField.val(searchQuery);
};

scout.DesktopNavigation.prototype._onSearchButtonClick = function(event) {
  if (this.activeTab === this.searchTab) {
    this.desktop.searchOutline.performSearch();
  }
};

scout.DesktopNavigation.prototype._onQueryFieldInput = function(event) {
  //Store locally so that the value persists when changing the outline without performing the search
  this.desktop.searchOutline.searchQuery = this.$queryField.val();
};

scout.DesktopNavigation.prototype._onQueryFieldKeyPress = function(event) {
  if (event.which === scout.keys.ENTER) {
    this.desktop.searchOutline.performSearch();
  }
};

// tab state and container handling

scout.DesktopNavigation.prototype._setActiveTab = function(tab) {
  var oldTab = this.activeTab;
  if (oldTab === tab) {
    return;
  }

  if (oldTab) {
    oldTab.$tab.removeClass('tab-active');
  }

  tab.$tab.addClass('tab-active');
  this.activeTab = tab;
};

// event handling

scout.DesktopNavigation.prototype.onOutlineChanged = function(outline) {
  if (this.outline === outline) {
    return;
  }

  if (this.outline) {
    this.outline.remove();
  }

  if (outline === this.desktop.searchOutline) {
    // Remember previous outline when switching to search outline
    this.previousOutline = this.outline;

    this._setActiveTab(this.searchTab);
  } else {
    this._setActiveTab(this.outlineTab);
  }

  this.outline = outline;
  this.outline.render(this.$container);
  this.outline.htmlComp.validateLayout();
  this.outline.pixelBasedSizing = true;
  this.$outlineTitle.html(this.outline.title);

  if (outline === this.desktop.searchOutline) {
    // Focus and select content AFTER the search outline was rendered (and therefore the query field filled)
    this.$queryField.focus();
    this.$queryField[0].select();
  } else{
    scout.focusManager.validateFocus(this.session.uiSessionId);
  }
};

// vertical splitter
scout.DesktopNavigation.prototype.onResize = function(event) {
  var w = event.data; // data = newSize

  this.$navigation.width(w);
  this.desktop.$taskBar.css('left', w);
  this.desktop.$bench.css('left', w);

  if (w <= this.breadcrumbSwitchWidth) {
    if (!this.$navigation.hasClass('navigation-breadcrumb')) {
      this.$navigation.addClass('navigation-breadcrumb');
      this.outline.setBreadcrumbEnabled(true);
    }
  } else {
    this.$navigation.removeClass('navigation-breadcrumb');
    this.outline.setBreadcrumbEnabled(false);
  }
};

/**
 * Called by OutlineViewButton.js
 */
scout.DesktopNavigation.prototype.onOutlinePropertyChange = function(event) {
  for (var propertyName in event.properties) {
    if (propertyName === 'text') {
      this.$outlineTitle.text(event.properties[propertyName]);
    }
  }
};

scout.DesktopNavigation.prototype._createKeyStrokeAdapter = function() {
  return new scout.DesktopNavigationKeyStrokeAdapter(this);
};

scout.DesktopNavigation.prototype._installKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter && !scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.installAdapter(this.desktop.$container, this.keyStrokeAdapter);
  }
};

scout.DesktopNavigation.prototype._uninstallKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter && scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }
};

/* --- INNER TYPES ---------------------------------------------------------------- */

scout.DesktopNavigation.TabAndContent = function($tab) {
  this.$tab = $tab;
  this.$storage = null;
};
