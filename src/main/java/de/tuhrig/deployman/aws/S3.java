/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * @author tuhrig
 */
public class S3 implements IAws<AmazonS3> {
  @Override
  public AmazonS3 getClient() {
    AWSCredentials credentials = new Aws().getAwsCredentials();
    return new AmazonS3Client(credentials);
  }

}
