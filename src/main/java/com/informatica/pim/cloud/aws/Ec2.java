/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.aws;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class Ec2 implements IAws<AmazonEC2> {

  @Override
  public AmazonEC2 getClient() {
    AWSCredentials credentials = new Aws().getAwsCredentials();
    AmazonEC2 ec2 = new AmazonEC2Client(credentials);

    String region = getUserProperty(AWS_REGION);
    String endpoint = "https://ec2." + region + ".amazonaws.com"; //$NON-NLS-1$ //$NON-NLS-2$

    ec2.setEndpoint(endpoint);
    return ec2;
  }

  public void associate(String instanceId, String elasticIp) {
    Instance instance = getEC2InstanceById(instanceId);

    console.write("Associate " + instance.getInstanceId() + " with IP " + elasticIp); //$NON-NLS-1$ //$NON-NLS-2$
    console.newLine();

    getClient().associateAddress(
        new AssociateAddressRequest().withInstanceId(instance.getInstanceId()).withPublicIp(
            elasticIp));

  }

  public void tag(String instanceId, String tag) {
    tag(instanceId, new Tag("Name", tag)); //$NON-NLS-1$
    console.newLine();
  }

  public void openPort(String securityGroup, int port) {
    IpPermission permission = new IpPermission().withIpProtocol("tcp") //$NON-NLS-1$
        .withFromPort(port).withToPort(port).withIpRanges("0.0.0.0/0"); //$NON-NLS-1$

    AuthorizeSecurityGroupIngressRequest request =
        new AuthorizeSecurityGroupIngressRequest().withGroupName(securityGroup).withIpPermissions(
            permission);

    getClient().authorizeSecurityGroupIngress(request);
  }

  public void tag(String instanceId, Tag tag) {
    CreateTagsRequest request = new CreateTagsRequest().withResources(instanceId).withTags(tag);

    AmazonEC2 ec2 = getClient();
    ec2.createTags(request);

    console.write("Tag " + instanceId + " as " + tag); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void printEC2Instances() {
    List<Instance> instances = getEC2Instances();

    if (instances.size() == 0)
      console.write("no instances\n"); //$NON-NLS-1$
    else
      console.printEc2Instances(instances);
  }

  public void terminateEC2InstanceById(String id) {
    AmazonEC2 ec2 = getClient();
    ec2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(id));
    console.write("EC2 instance " + id + " will be terminated"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public IamInstanceProfileSpecification getIamInstanceProfileSpecification(String name) {
    return new IamInstanceProfileSpecification().withName(name);
  }

  public BlockDeviceMapping getBlockDeviceMapping(String deviceName, int sizeInGb) {
    return new BlockDeviceMapping().withDeviceName(deviceName).withEbs(
        new EbsBlockDevice().withVolumeSize(sizeInGb));
  }

  public Instance getEC2InstanceById(String instanceId) {
    for (Instance instance : getEC2Instances()) {
      if (instance.getInstanceId().equals(instanceId))
        return instance;
    }
    return null;
  }

  public List<Instance> getEC2Instances() {
    DescribeInstancesResult request = getClient().describeInstances();
    List<Instance> instances = new ArrayList<>();
    for (Reservation reservation : request.getReservations())
      instances.addAll(reservation.getInstances());
    return instances;
  }

  public String getHostNameOfInstance(String instanceId) {
    Instance instance = getEC2InstanceById(instanceId);
    return instance.getPublicDnsName();
  }
}
