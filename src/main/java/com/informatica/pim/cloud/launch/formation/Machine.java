/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch.formation;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * @author tuhrig
 */
public class Machine {
  @SerializedName("name")
  private String name;

  @SerializedName("elastic_ip")
  private String elasticIp;

  @SerializedName("instance_type")
  private String instanceType;

  @SerializedName("image_id")
  private String imageId;

  @SerializedName("security_group")
  private String securityGroup;

  @SerializedName("install_docker")
  private Boolean installDocker = true;

  @SerializedName("install_awscli")
  private Boolean installAwsCli = true;

  @SerializedName("open_docker")
  private Boolean openDocker = true;

  @SerializedName("auto_sync")
  private Boolean autoSync = false;

  @SerializedName("containers")
  private List<Container> containers;

  @SerializedName("scaling")
  private Scaling scaling;

  public Boolean hasAutoSync() {
    return this.autoSync;
  }

  public void setAutoSync(Boolean autoSync) {
    this.autoSync = autoSync;
  }

  public Boolean openDocker() {
    return this.openDocker;
  }

  public void openDocker(Boolean openDocker) {
    this.openDocker = openDocker;
  }

  public Boolean installDocker() {
    return this.installDocker;
  }

  public void installDocker(Boolean installDocker) {
    this.installDocker = installDocker;
  }

  public Boolean installAwsCli() {
    return this.installAwsCli;
  }

  public void installAwsCli(Boolean installAwsCli) {
    this.installAwsCli = installAwsCli;
  }

  public Scaling getScaling() {
    return this.scaling;
  }

  public void setScaling(Scaling scaling) {
    this.scaling = scaling;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSecurityGroup() {
    return this.securityGroup;
  }

  public void setSecurityGroup(String securityGroup) {
    this.securityGroup = securityGroup;
  }

  public String getInstanceType() {
    return this.instanceType;
  }

  public void setInstanceType(String instanceType) {
    this.instanceType = instanceType;
  }

  public String getImageId() {
    return this.imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public String getElasticIp() {
    return this.elasticIp;
  }

  public void setElasticIp(String elasticIp) {
    this.elasticIp = elasticIp;
  }

  public List<Container> getContainers() {
    return this.containers;
  }

  public void setContainers(List<Container> containers) {
    this.containers = containers;
  }

  @Override
  public String toString() {
    return this.elasticIp;
  }
}
