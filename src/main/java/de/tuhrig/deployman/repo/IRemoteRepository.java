/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.repo;

import java.io.File;

/**
 * @author tuhrig
 */
public interface IRemoteRepository {
  public void printRemoteObjects();

  public String getRemoteFolder();

  public void uploadLocaleFile(String fileName);

  public File getLocaleFileByName(String name);

  public boolean existsRemote();

  public void initRemote();
}
