// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG
//

//@include("javascript/Scout.js");
//@include("javascript/Session.js");
//@include("javascript/desktop/Desktop.js");
//@include("javascript/desktop/DesktopBench.js");
//@include("javascript/desktop/DesktopMatrix.js");
//@include("javascript/desktop/DesktopTable.js");
//@include("javascript/desktop/DesktopTableChart.js");
//@include("javascript/desktop/DesktopTableGraph.js");
//@include("javascript/desktop/DesktopTableHeader.js");
//@include("javascript/desktop/DesktopTableMap.js");
//@include("javascript/desktop/DesktopTableOrganize.js");
//@include("javascript/desktop/DesktopToolButton.js");
//@include("javascript/desktop/DesktopTree.js");
//@include("javascript/desktop/DesktopViewButton.js");
//@include("javascript/desktop/DesktopViewButtonBar.js");
//@include("javascript/desktop/DesktopViewButtonOwn.js");
//@include("javascript/menu/Menu.js");
//@include("javascript/menu/MenuHeader.js");
//@include("javascript/scrollbar/Scrollbar.js");

$(document).ready(function () {
    var tabId = '' + new Date().getTime();
    $('.scout').each(function () {
      var portletPartId = $(this).data('partid') || '0',
        sessionPartId = [portletPartId, tabId].join('.');
      var session = new Scout.Session($(this), sessionPartId);
      session.init();
    });
  });
