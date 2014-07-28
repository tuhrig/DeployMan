/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch.formation;

import com.google.gson.annotations.SerializedName;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class Container {
  @SerializedName("name")
  private String name = ""; //$NON-NLS-1$

  @SerializedName("tarball")
  private String tarball = ""; //$NON-NLS-1$

  @SerializedName("image")
  private String image = ""; //$NON-NLS-1$

  @SerializedName("credential")
  private Credential credential;

  @SerializedName("config")
  private String config = ""; //$NON-NLS-1$

  @SerializedName("command")
  private String command = ""; //$NON-NLS-1$

  public String getImage() {
    return this.image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Credential getCredential() {
    return this.credential;
  }

  public void setCredential(Credential credential) {
    this.credential = credential;
  }

  public String getCommand() {
    return this.command;
  }

  public String getTarball() {
    return this.tarball;
  }

  public String getTarballName() {
    if (!this.tarball.equals("")) //$NON-NLS-1$
    {
      String fileName = getTarballFileName();
      return fileName.substring(0, fileName.length() - 4);
    }
    return ""; //$NON-NLS-1$
  }

  public String getTarballFileName() {
    if (this.tarball.contains(SLASH))
      return this.tarball.substring(this.tarball.lastIndexOf(SLASH) + 1, this.tarball.length());
    return this.tarball;
  }

  public String getConfig() {
    return this.config;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public boolean hasImage() {
    return this.image != null && !this.image.equals(""); //$NON-NLS-1$
  }

  public boolean hasCredential() {
    return getCredential() != null;
  }
}
