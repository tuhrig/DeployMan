/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.aws;

import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;

import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class Rds implements IAws<AmazonRDSClient> {
  @Override
  public AmazonRDSClient getClient() {
    AWSCredentials credentials = new Aws().getAwsCredentials();
    AmazonRDSClient rds = new AmazonRDSClient(credentials);

    String region = getUserProperty(AWS_REGION);
    String endpoint = "https://rds." + region + ".amazonaws.com"; //$NON-NLS-1$ //$NON-NLS-2$

    rds.setEndpoint(endpoint);

    return rds;
  }

  public List<DBInstance> getDatabases() {
    AmazonRDSClient rds = getClient();
    return rds.describeDBInstances().getDBInstances();
  }

  public void printDatabases() {
    console.printDatabases(getDatabases());
  }

  public DBInstance getDatabase(String name) {
    for (DBInstance instance : getDatabases()) {
      if (instance.getDBInstanceIdentifier().equals(name))
        return instance;
    }

    return null;
  }

  public boolean databaseExists(String name) {
    for (DBInstance instance : getDatabases()) {
      if (instance.getDBInstanceIdentifier().equals(name))
        return true;
    }
    return false;
  }
}
