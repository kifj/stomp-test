window.onload = function() {
  //<editor-fold desc="Changeable Configuration Block">
  var url = window.location.search.match(/url=([^&]+)/);
  if (url && url.length > 1) {
    url = decodeURIComponent(url[1]);
  } else {
    var path = '';
    if (window.location.pathname.length > 1) {
      path = '/' + window.location.pathname.split( '/' )[1];
    }
    url = window.location.protocol + "//" + location.host + path + "/openapi?format=JSON";
  }
  
  // the following lines will be replaced by docker/configurator, when it runs in a docker-container
  window.ui = SwaggerUIBundle({
    url: url,
    dom_id: '#swagger-ui',
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl
    ],
    layout: "StandaloneLayout"
  });

  //</editor-fold>
};
