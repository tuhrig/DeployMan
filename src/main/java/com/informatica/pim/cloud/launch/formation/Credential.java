/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch.formation;

import com.google.gson.annotations.SerializedName;

/**
 * @author tuhrig
 */
public class Credential {
  @SerializedName("name")
  private String name = ""; //$NON-NLS-1$

  @SerializedName("email")
  private String email = ""; //$NON-NLS-1$

  @SerializedName("password")
  private String password = ""; //$NON-NLS-1$

  @SerializedName("server")
  private String server = ""; //$NON-NLS-1$

  public String getServer() {
    return this.server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
