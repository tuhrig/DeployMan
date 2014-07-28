/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch.enums;

/**
 * @author tuhrig
 */
public class Comment {
  public final static String COPY_IMAGES_AND_CONFIGS = "# copy images and configs"; //$NON-NLS-1$
  public final static String EXEC_IMAGES = "# exec images"; //$NON-NLS-1$

  public final static String DOWNLOAD_IMAGE = "# download image"; //$NON-NLS-1$
  public final static String DOWNLOAD_TARBALL = "# download tarball"; //$NON-NLS-1$

  public final static String RUN_IMAGE = "# exec image"; //$NON-NLS-1$
  public final static String DONE = "# done"; //$NON-NLS-1$
  public final static String WAIT_FOR_SUBSHELL = "# wait for subshells"; //$NON-NLS-1$
  public final static String BASH = "#!/bin/bash"; //$NON-NLS-1$
}
