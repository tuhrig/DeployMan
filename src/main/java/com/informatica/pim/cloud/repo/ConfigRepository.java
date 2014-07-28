/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.repo;

import static com.informatica.pim.cloud.DeployMan.CONFIG_FOLDER;
import static com.informatica.pim.cloud.DeployMan.IMAGE_FOLDER;
import static com.informatica.pim.cloud.DeployMan.REPO_BUCKET;
import static com.informatica.pim.cloud.DeployMan.REPO_LOCALE;
import static com.informatica.pim.cloud.DeployMan.SLASH;
import static com.informatica.pim.cloud.DeployMan.console;
import static com.informatica.pim.cloud.DeployMan.getUserProperty;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.informatica.pim.cloud.aws.S3;
import com.informatica.pim.cloud.model.Config;

/**
 * @author tuhrig
 */
public class ConfigRepository implements IRepository, ILocaleRepository, IRemoteRepository {
  private LocaleRepository locale = new LocaleRepository();
  private RemoteRepository remote = new RemoteRepository();

  @Override
  public void printRemoteObjects() {
    console.write(getRemoteFolder());
    console.printRemoteConfig(getRemoteConfigs());
  }

  @Override
  public void printLocaleObjects() {
    File folder = getLocaleFolder();
    console.printFoldersOfFolder(folder);
  }

  @Override
  public File getLocaleFolder() {
    return new File(this.locale.getLocation() + SLASH + CONFIG_FOLDER);
  }

  @Override
  public List<File> getLocaleFiles() {
    File localFolder = getLocaleFolder();
    return this.locale.getFoldersOfFolder(localFolder);
  }

  @Override
  public void uploadLocaleFile(String fileName) {
    File file = getLocaleFileByName(fileName);
    console.write("Upload file " + fileName); //$NON-NLS-1$ 
    try {
      this.remote.uploadFolder(file, CONFIG_FOLDER + file.getName());
    } catch (AmazonClientException | InterruptedException e) {
      console.write("Cannot upload"); //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  @Override
  public File getLocaleFileByName(String name) {
    List<File> files = this.locale.getFoldersOfFolder(getLocaleFolder());

    for (File file : files) {
      if (file.getName().equals(name))
        return file;
    }
    return null;
  }

  public List<Config> getRemoteConfigsWithUrl() {
    List<Config> configs = new ArrayList<>();

    String bucketName = getUserProperty(REPO_BUCKET);

    for (String object : this.remote.getRemoteFolders(CONFIG_FOLDER)) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.HOUR_OF_DAY, 1);

      URL url = new S3().getClient().generatePresignedUrl(bucketName, object, cal.getTime());

      Config config = new Config();
      config.setUrl(url);
      config.setName(object);
      config.setSize(this.remote.getSizeOfRemoteFolder(object));
      config.setNumberOfFiles(this.remote.getNumberOfFilesOfRemoteFolder(object));

      configs.add(config);
    }

    return configs;
  }

  public List<String> getRemoteConfigs() {
    return this.remote.getRemoteFolders(CONFIG_FOLDER);
  }

  @Override
  public boolean exists() {
    return existsLocale() && existsRemote();
  }

  @Override
  public boolean existsLocale() {
    return getLocaleFolder().exists();
  }

  @Override
  public boolean existsRemote() {
    return this.remote.folderExistsInRootOfBucket(CONFIG_FOLDER);
  }

  @Override
  public void init() {
    initLocale();
    initRemote();
  }

  @Override
  public void initRemote() {
    if (existsRemote())
      console.write("Remote config repository already exists (skip)"); //$NON-NLS-1$

    else {
      String bucket = getUserProperty(REPO_BUCKET);
      this.remote.createRemoteFolder(bucket, CONFIG_FOLDER);
      console.write("Create remote folder '" + IMAGE_FOLDER + "' in " + bucket); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void initLocale() {
    if (existsLocale())
      console.write("Locale config repository already exists (skip)"); //$NON-NLS-1$

    else {
      String repoName = getUserProperty(REPO_LOCALE);
      this.locale.createLocalFolder(repoName, CONFIG_FOLDER);
      console.write("Create locale folder '" + IMAGE_FOLDER + "' in " + repoName); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void printInfo() {
    printLocaleObjects();
    printRemoteObjects();
  }

  @Override
  public String getRemoteFolder() {
    return getUserProperty(REPO_BUCKET) + "/" + CONFIG_FOLDER; //$NON-NLS-1$
  }
}
