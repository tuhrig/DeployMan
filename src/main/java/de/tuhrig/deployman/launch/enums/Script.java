/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.launch.enums;

/**
 * @author tuhrig
 */
public class Script {
  public final static String COPY_AND_LOAD_TARBALL_PARALLEL = "copy_and_load_tarball_&.sh"; //$NON-NLS-1$

  public final static String COPY_CONFIG = "copy_config.sh"; //$NON-NLS-1$
  public final static String COPY_CONFIG_PARALLEL = "copy_config_&.sh"; //$NON-NLS-1$
  public final static String COPY_IMAGE = "copy_image.sh"; //$NON-NLS-1$
  public final static String COPY_IMAGE_PAR = "copy_image_&.sh"; //$NON-NLS-1$

  public final static String DOCKER_IMPORT = "docker_import.sh"; //$NON-NLS-1$
  public final static String DOCKER_LOAD = "docker_load.sh"; //$NON-NLS-1$
  public final static String DOCKER_LOAD_PRALLEL = "docker_load_&.sh"; //$NON-NLS-1$
  public final static String DOCKER_LOGIN = "docker_login.sh"; //$NON-NLS-1$

  public final static String DOWNLOAD_IMAGE = "download_image.sh"; //$NON-NLS-1$

  public final static String FINAL_MESSAGE = "final_message.sh"; //$NON-NLS-1$

  public final static String INSTALL_AWSCLI = "install_awscli.sh"; //$NON-NLS-1$
  public final static String INSTALL_AWSCLI_PARALLEL = "install_awscli_&.sh"; //$NON-NLS-1$
  public final static String INSTALL_DOCKER = "install_docker.sh"; //$NON-NLS-1$
  public final static String INSTALL_DOCKER_PARALLEL = "install_docker_&.sh"; //$NON-NLS-1$

  public final static String LOG_HEADER = "log_header.sh"; //$NON-NLS-1$
  public final static String LOG_INFO = "log_info.sh"; //$NON-NLS-1$

  public final static String OPEN_DOCKER = "open_docker.sh"; //$NON-NLS-1$
  public final static String RUN_IMAGE = "run_image.sh"; //$NON-NLS-1$
  public final static String SET_TIMEZONE = "set_timezone.sh"; //$NON-NLS-1$
  public final static String SYNC_CONFIG = "sync_config.sh"; //$NON-NLS-1$
  public final static String WAIT = "wait.sh"; //$NON-NLS-1$
}
