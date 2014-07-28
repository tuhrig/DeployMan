/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.docker;

/**
 * @author tuhrig
 */
public class DockerContainer {
  private String log;
  private String id;

  public String getLog() {
    return this.log;
  }

  public void setLog(String log) {
    this.log = log;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }
}
