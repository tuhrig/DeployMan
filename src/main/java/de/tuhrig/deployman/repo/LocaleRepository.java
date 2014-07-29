/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.repo;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class LocaleRepository implements IRepository {
  public void createLocalFolder(String repo, String folder) {
    new File(repo + SLASH + folder).mkdir();
  }

  public String getLocation() {
    return getUserProperty(REPO_LOCALE);
  }

  public List<File> getFilesOfFolder(File folder) {
    return Arrays.asList(folder.listFiles()).stream().filter(file -> file.isFile())
        .collect(Collectors.toList());
  }

  public List<File> getFoldersOfFolder(File folder) {
    return Arrays.asList(folder.listFiles()).stream().filter(file -> file.isDirectory())
        .collect(Collectors.toList());
  }

  @Override
  public void init() {
    ILocaleRepository setupRepo = new SetupRepository();
    ILocaleRepository formationRepo = new FormationRepository();
    ILocaleRepository imageRepo = new ImageRepository();
    ILocaleRepository configRepo = new ConfigRepository();

    setupRepo.initLocale();
    formationRepo.initLocale();
    imageRepo.initLocale();
    configRepo.initLocale();
  }

  @Override
  public boolean exists() {
    ILocaleRepository setupRepo = new SetupRepository();
    ILocaleRepository formationRepo = new FormationRepository();
    ILocaleRepository imageRepo = new ImageRepository();
    ILocaleRepository configRepo = new ConfigRepository();

    return setupRepo.existsLocale() && formationRepo.existsLocale() && imageRepo.existsLocale()
        && configRepo.existsLocale();
  }

  @Override
  public void printInfo() {
    String repo = getUserProperty(REPO_LOCALE);

    if (exists()) {
      console.write("Locale repository: " + repo + " (exists)"); //$NON-NLS-1$ //$NON-NLS-2$
      console.newLine();
      new ImageRepository().printLocaleObjects();
      new ConfigRepository().printLocaleObjects();
      new SetupRepository().printLocaleObjects();
      new FormationRepository().printLocaleObjects();
    } else
      console.write("Locale repository: " + repo + " (is missing)"); //$NON-NLS-1$ //$NON-NLS-2$
  }
}
