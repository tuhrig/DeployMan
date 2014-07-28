/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.aws;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class AutoScaling implements IAws<AmazonAutoScalingClient> {

  @Override
  public AmazonAutoScalingClient getClient() {
    AmazonAutoScalingClient client = new AmazonAutoScalingClient(new Aws().getAwsCredentials());

    String region = getUserProperty(AWS_REGION);
    String endpoint = "https://autoscaling." + region + ".amazonaws.com"; //$NON-NLS-1$ //$NON-NLS-2$

    client.setEndpoint(endpoint);

    return client;
  }
}
