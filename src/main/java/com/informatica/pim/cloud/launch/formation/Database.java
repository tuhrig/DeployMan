/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch.formation;

import com.google.gson.annotations.SerializedName;

/**
 * @author tuhrig
 */
public class Database {
  @SerializedName("setup")
  private String setup;

  @SerializedName("security_group")
  private String securityGroup;

  @SerializedName("license")
  private String license;

  @SerializedName("engine")
  private String engine;

  @SerializedName("engine_version")
  private String engineVersion;

  @SerializedName("instance_class")
  private String instanceClass;

  @SerializedName("multi_az")
  private Boolean multiAz;

  @SerializedName("auto_minor_version_upgrade")
  private Boolean autoMinorVersionUpgrade;

  @SerializedName("allocated_storage")
  private Integer allocatedStorage;

  @SerializedName("instance_identifier")
  private String instanceIdentifier;

  @SerializedName("username")
  private String username;

  @SerializedName("password")
  private String password;

  @SerializedName("name")
  private String name;

  @SerializedName("port")
  private Integer port;

  public String getSetup() {
    return this.setup;
  }

  public void setSetup(String setup) {
    this.setup = setup;
  }

  public String getLicense() {
    return this.license;
  }

  public void setLicense(String license) {
    this.license = license;
  }

  public String getSecurityGroup() {
    return this.securityGroup;
  }

  public void setSecurityGroup(String securityGroup) {
    this.securityGroup = securityGroup;
  }

  public String getEngine() {
    return this.engine;
  }

  public void setEngine(String engine) {
    this.engine = engine;
  }

  public String getEngineVersion() {
    return this.engineVersion;
  }

  public void setEngineVersion(String engineVersion) {
    this.engineVersion = engineVersion;
  }

  public String getInstanceClass() {
    return this.instanceClass;
  }

  public void setInstanceClass(String instanceClass) {
    this.instanceClass = instanceClass;
  }

  public Boolean getMultiAz() {
    return this.multiAz;
  }

  public void setMultiAz(Boolean multiAz) {
    this.multiAz = multiAz;
  }

  public Boolean getAutoMinorVersionUpgrade() {
    return this.autoMinorVersionUpgrade;
  }

  public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
    this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
  }

  public Integer getAllocatedStorage() {
    return this.allocatedStorage;
  }

  public void setAllocatedStorage(Integer allocatedStorage) {
    this.allocatedStorage = allocatedStorage;
  }

  public String getInstanceIdentifier() {
    return this.instanceIdentifier;
  }

  public void setInstanceIdentifier(String instanceIdentifier) {
    this.instanceIdentifier = instanceIdentifier;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getPort() {
    return this.port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

}
