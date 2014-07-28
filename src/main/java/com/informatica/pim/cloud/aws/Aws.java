/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.aws;

import java.io.IOException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class Aws {
  public AWSCredentials getAwsCredentials() {
    try {
      return new PropertiesCredentials(getUserProperitesFile());
    } catch (IOException e) {
      console
          .write("Cannot find or read configuration file at'" + getUserPropertiesFilePath() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
      throw new RuntimeException(e);
    }
  }
}
