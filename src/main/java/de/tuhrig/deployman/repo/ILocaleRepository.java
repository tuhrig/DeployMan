/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.repo;

import java.io.File;
import java.util.List;

/**
 * @author tuhrig
 */
public interface ILocaleRepository {
  public void printLocaleObjects();

  public File getLocaleFolder();

  public List<File> getLocaleFiles();

  public boolean existsLocale();

  public void initLocale();
}
