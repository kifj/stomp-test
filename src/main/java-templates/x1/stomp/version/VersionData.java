package x1.stomp.version;

public interface VersionData {
  String APP_NAME = "${project.artifactId}";
  String APP_VERSION_MAJOR = "${app.majorVersion}";
  String APP_VERSION_MINOR = "${app.minorVersion}";
  String APP_VERSION_MAJOR_MINOR = APP_VERSION_MAJOR + "." + APP_VERSION_MINOR;
  String APP_NAME_MAJOR_MINOR = APP_NAME + "-v" + APP_VERSION_MAJOR_MINOR;
}
