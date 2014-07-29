/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.aws;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;

import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class CloudWatch implements IAws<AmazonCloudWatchClient> {

  @Override
  public AmazonCloudWatchClient getClient() {
    AmazonCloudWatchClient client = new AmazonCloudWatchClient(new Aws().getAwsCredentials());

    String region = getUserProperty(AWS_REGION);
    String endpoint = "https://monitoring." + region + ".amazonaws.com"; //$NON-NLS-1$ //$NON-NLS-2$

    client.setEndpoint(endpoint);

    return client;
  }
}
