/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.model;

import java.net.URL;

/**
 * @author tuhrig
 */
public class Config {
  private URL url;
  private String name;
  private long size;
  private long numberOfFiles;

  public long getNumberOfFiles() {
    return this.numberOfFiles;
  }

  public URL getUrl() {
    return this.url;
  }

  public String getName() {
    return this.name;
  }

  public long getSize() {
    return this.size;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public void setNumberOfFiles(long numberOfFiles) {
    this.numberOfFiles = numberOfFiles;
  }

}
