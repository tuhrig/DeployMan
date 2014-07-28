/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.health;

/**
 * @author tuhrig
 */
public class Status {
  private String name;
  private DeploymentStatus status;

  public Status(String name, DeploymentStatus status) {
    this.setName(name);
    this.setStatus(status);
  }

  public DeploymentStatus getStatus() {
    return this.status;
  }

  public void setStatus(DeploymentStatus status) {
    this.status = status;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
