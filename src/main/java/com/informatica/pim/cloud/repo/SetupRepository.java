/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.repo;

import java.io.File;
import java.util.List;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class SetupRepository implements IRepository, ILocaleRepository {
  private LocaleRepository locale = new LocaleRepository();

  @Override
  public void printLocaleObjects() {
    File folder = getLocaleFolder();
    console.printFoldersOfFolder(folder);
  }

  @Override
  public File getLocaleFolder() {
    return new File(this.locale.getLocation() + SLASH + SETUP_FOLDER);
  }

  @Override
  public List<File> getLocaleFiles() {
    File localFolder = getLocaleFolder();
    return this.locale.getFoldersOfFolder(localFolder);
  }

  @Override
  public boolean exists() {
    return existsLocale();
  }

  @Override
  public boolean existsLocale() {
    return getLocaleFolder().exists();
  }

  @Override
  public void init() {
    init();
  }

  @Override
  public void initLocale() {
    if (exists())
      console.write("Locale setup repository already exists (skip)"); //$NON-NLS-1$

    else {
      String repoName = getUserProperty(REPO_LOCALE);
      this.locale.createLocalFolder(repoName, SETUP_FOLDER);
      console.write("Create locale folder '" + IMAGE_FOLDER + "' in " + repoName); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void printInfo() {
    printLocaleObjects();
  }
}
