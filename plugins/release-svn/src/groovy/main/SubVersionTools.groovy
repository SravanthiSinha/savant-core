import org.savantbuild.BuildException
import org.savantbuild.net.LocalSubVersion
import org.savantbuild.net.SubVersion
import org.savantbuild.net.LocalSubVersion.StatusHandler

/**
 * <p>
 * This class is a helper for SubVersion.
 * </p>
 *
 * @author  Brian Pontarelli
 */
class SubVersionTools {
  /**
   * Makes a SubVersion object by loading the plugin configuration, figuring out the username and password, and then
   * constructing the object.
   *
   * @param   url The SVN URL.
   * @return  The SubVersion object.
   * @throws  BuildException If the configuration could not be loaded.
   */
  static SubVersion make(String url) {
    Properties releaseProps = loadProperties()
    String username = findProperty(releaseProps, url, "username")
    String password = findProperty(releaseProps, url, "password")
    if (username == null || password == null) {
      throw new BuildException("You must create a file named ~/.savant/plugins/release-svn.properties and it must contain " +
        "properties that specify the username and password for the SVN repository that this project " +
        "is stored in.\n\nIf the project's SVN repository is [https://svn.example.com/svn/trunk], you must " +
        "define one of these sets of properties in order for the release-svn plugin to have the SVN " +
        "credentials for the repository:\n\n[svn.example.com.username] and [svn.example.com.password]\n" +
        "[example.com.username] and [example.com.password]\n" +
        "[com.username] and [com.password]")
    }

    return new SubVersion(url, username, password)
  }

  /**
   * Copies the from one directory to another in the current project's SVN URL. The src and dest are therefore just the
   * directory names and not full URLs.
   *
   * @param   src The source of the copy.
   * @param   dest The destination of the copy.
   */
  static void copy(String src, String dest) {
    SubVersion svn = make(LocalSubVersion.getProjectBaseURL(new File('.')))
    svn.doCopy(src, dest, "Copy from ${src} to ${dest}")
  }

  /**
   * Determines if the given tag is available for the current directories SubVersion repository.
   *
   * @param   tag The tag.
   * @return  True if the tag is available (doesn't exist), false otherwise.
   */
  static boolean isTagAvailable(String tag) {
    SubVersion svn = make(LocalSubVersion.getProjectBaseURL(new File('.')) + "/tags/${tag}")
    return !svn.isExists()
  }

  /**
   * Determines if the local directory has modifications that haven't been committed to SVN.
   *
   * @return  True or false depending.
   */
  static boolean hasModifications() {
    boolean mods = false
    LocalSubVersion.doStatus(new File("."), { file -> mods = true } as StatusHandler)
    return mods
  }

  private static Properties loadProperties() {
    def props = new Properties()
    def f = new File(System.getProperty("user.home") + "/.savant/plugins/release-svn.properties")
    if (f.isFile()) {
      f.withReader { reader ->
        props.load(reader);
      }
    }
    return props
  }

  private static String findProperty(Properties props, String url, String suffix) {
    // Clean up the svnURL to get just the host
    def uri = new URI(url)
    def host = uri.getHost()
    def prop = props["${host}.${suffix}"]
    if (prop == null) {
      def index = host.indexOf(".")
      while (index >= 0 && prop == null) {
        host = host.substring(index + 1)
        index = host.indexOf(".")
        prop = props["${host}.${suffix}"]
      }
    }

    return prop
  }
}
