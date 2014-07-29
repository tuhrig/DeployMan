/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.launch.enums;

/**
 * @author tuhrig
 */
public class Variable {
  public final static String CONFIG_KEY = "config.key"; //$NON-NLS-1$

  public final static String IMAGE_NAME = "image.name"; //$NON-NLS-1$

  public final static String TARBALL_NAME = "tarball.name"; //$NON-NLS-1$
  public final static String TARBALL_KEY = "tarball.key"; //$NON-NLS-1$

  public final static String CONFIG_FOLDER = "config.folder"; //$NON-NLS-1$
  public final static String HOME_DIRECTORY = "home.directory"; //$NON-NLS-1$
  public final static String TIMESTAMP = "info.timestamp"; //$NON-NLS-1$
  public final static String HOST = "info.host"; //$NON-NLS-1$
  public final static String FORMATION = "info.formation"; //$NON-NLS-1$
  public final static String CONTAINERS = "info.containers"; //$NON-NLS-1$

  public final static String LOG_DEPLOYMENT = "log.deployment"; //$NON-NLS-1$
  public final static String LOG_DOCKER = "log.docker"; //$NON-NLS-1$

  public final static String REGISTRY_EMAIL = "registry.email"; //$NON-NLS-1$
  public final static String REGISTRY_NAME = "registry.name"; //$NON-NLS-1$
  public final static String REGISTRY_PASSWORD = "registry.password"; //$NON-NLS-1$
  public final static String REGISTRY_SERVER = "registry.server"; //$NON-NLS-1$

  // Don't change the keys of the following variables!
  // They are referenced in the ANT scripts of the
  // database setup and need to have the same names
  // as there!

  public final static String ANT_FILE = "ant.file"; //$NON-NLS-1$
  public final static String DEST_ROOT_LOCAL = "dest.root.local"; //$NON-NLS-1$
  public final static String DB_SERVER = "db.default.server"; //$NON-NLS-1$
  public final static String DB_PORT = "db.default.port"; //$NON-NLS-1$
  public final static String DB_USER = "db.default.user"; //$NON-NLS-1$
  public final static String DB_PASSWORD = "db.default.password"; //$NON-NLS-1$
  public final static String ENV_NLS_LANG = "env.NLS_LANG"; //$NON-NLS-1$
  public final static String HEADLESS = "headless"; //$NON-NLS-1$
}
