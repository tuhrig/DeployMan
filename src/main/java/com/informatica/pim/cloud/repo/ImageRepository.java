/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.repo;

import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.informatica.pim.cloud.aws.S3;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class ImageRepository implements IRepository, ILocaleRepository, IRemoteRepository {
  private LocaleRepository locale = new LocaleRepository();
  private RemoteRepository remote = new RemoteRepository();

  @Override
  public void printRemoteObjects() {
    console.write(getRemoteFolder());
    console.printS3ObjectSummaries(getRemoteImages());
  }

  @Override
  public void printLocaleObjects() {
    File folder = getLocaleFolder();
    console.printFilesOfFolder(folder);
  }

  @Override
  public File getLocaleFolder() {
    return new File(this.locale.getLocation() + SLASH + IMAGE_FOLDER);
  }

  @Override
  public List<File> getLocaleFiles() {
    File localFolder = getLocaleFolder();
    return this.locale.getFilesOfFolder(localFolder);
  }

  @Override
  public void uploadLocaleFile(String fileName) {
    File file = getLocaleFileByName(fileName);
    console.write("Upload file " + file.getAbsolutePath()); //$NON-NLS-1$ 
    try {
      this.remote.uploadFile(file, IMAGE_FOLDER + file.getName());
    } catch (AmazonClientException | InterruptedException e) {
      console.write("Cannot upload"); //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  @Override
  public File getLocaleFileByName(String name) {
    for (File file : getLocaleFiles()) {
      if (file.getName().equals(name))
        return file;
    }
    return null;
  }

  public Map<URL, S3ObjectSummary> getRemoteImagesWithUrl() {
    Map<URL, S3ObjectSummary> objects = new HashMap<>();

    String bucketName = getUserProperty(REPO_BUCKET);

    for (S3ObjectSummary object : this.remote.getRemoteObjects(IMAGE_FOLDER)) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.HOUR_OF_DAY, 1);

      URL url =
          new S3().getClient().generatePresignedUrl(bucketName, object.getKey(), cal.getTime());

      objects.put(url, object);
    }

    return objects;
  }

  public List<S3ObjectSummary> getRemoteImages() {
    return this.remote.getRemoteObjects(IMAGE_FOLDER);
  }

  @Override
  public String getRemoteFolder() {
    return getUserProperty(REPO_BUCKET) + "/" + IMAGE_FOLDER; //$NON-NLS-1$
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
    return this.remote.folderExistsInRootOfBucket(IMAGE_FOLDER);
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
      this.remote.createRemoteFolder(bucket, IMAGE_FOLDER);
      console.write("Create remote folder '" + IMAGE_FOLDER + "' in " + bucket); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void initLocale() {
    if (existsLocale())
      console.write("Locale image repository already exists (skip)"); //$NON-NLS-1$

    else {
      String repoName = getUserProperty(REPO_LOCALE);
      this.locale.createLocalFolder(repoName, IMAGE_FOLDER);
      console.write("Create locale folder '" + IMAGE_FOLDER + "' in " + repoName); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void printInfo() {
    printLocaleObjects();
    printRemoteObjects();
  }
}
