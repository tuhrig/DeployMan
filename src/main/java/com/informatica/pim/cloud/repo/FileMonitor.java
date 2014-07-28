/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.repo;

import static com.informatica.pim.cloud.DeployMan.REPO_BUCKET;
import static com.informatica.pim.cloud.DeployMan.REPO_LOCALE;
import static com.informatica.pim.cloud.DeployMan.SLASH;
import static com.informatica.pim.cloud.DeployMan.getUserProperty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileMonitor;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.informatica.pim.cloud.aws.S3;
import com.informatica.pim.cloud.console.Console;
import com.informatica.pim.cloud.console.TerminalConsole;

/**
 * @author tuhrig
 */
public class FileMonitor {
  private final AmazonS3 client;
  private final String locale;
  private final String bucket;
  private Console console;
  private boolean logging;

  public FileMonitor() {
    this(true);
  }

  public FileMonitor(boolean logging) {
    this(getUserProperty(REPO_BUCKET), getUserProperty(REPO_LOCALE), logging);
  }

  public FileMonitor(String bucket, String locale, boolean logging) {
    this.client = new S3().getClient();
    this.bucket = bucket;
    this.locale = locale;
    this.logging = logging;
    this.console = new Console(new TerminalConsole(logging));
  }

  public void monitor(String prefix) {
    prefix = addSlash(prefix);

    File root = new File(this.locale + SLASH + prefix);

    this.console.write("Monitor " + root); //$NON-NLS-1$
    this.console.newLine();

    final String finalPrefix = prefix;

    try {
      FileSystemManager manager = VFS.getManager();
      FileObject file = manager.toFileObject(root);

      DefaultFileMonitor monitor = new DefaultFileMonitor(new FileListener() {
        @Override
        public void fileDeleted(FileChangeEvent arg0) throws Exception {
          sync(finalPrefix);
        }

        @Override
        public void fileCreated(FileChangeEvent arg0) throws Exception {
          sync(finalPrefix);
        }

        @Override
        public void fileChanged(FileChangeEvent arg0) throws Exception {
          sync(finalPrefix);
        }
      });

      monitor.setDelay(3000);
      monitor.setRecursive(true);
      monitor.addFile(file);
      monitor.start();
    } catch (IOException e) {
      this.console.write("Cannot monitor " + prefix); //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  public void sync(String prefix) {
    prefix = addSlash(prefix);

    File root = new File(this.locale + SLASH + prefix);

    this.console.write("Sync " + root + " to " + this.bucket); //$NON-NLS-1$ //$NON-NLS-2$

    try {
      Map<String, Md5FilePair> localFiles = getLocalFiles(root, prefix);
      Map<String, String> remoteFiles = getRemoteFiles(this.client, this.bucket);

      for (Entry<String, Md5FilePair> entry : localFiles.entrySet()) {
        String key = entry.getKey();
        Md5FilePair value = entry.getValue();

        if (!remoteFiles.containsKey(key)) {
          if (this.logging) {
            this.console.write("Add " + key); //$NON-NLS-1$
            new RemoteRepository().uploadFile(value.file, key);
          } else
            new RemoteRepository().uploadFileSilently(value.file, key);
        }

        else if (!remoteFiles.get(key).equals(value.md5)) {
          if (this.logging) {
            this.console.write("Update " + key); //$NON-NLS-1$
            new RemoteRepository().uploadFile(value.file, key);
          } else
            new RemoteRepository().uploadFileSilently(value.file, key);
        }
      }

      /*
       * Delete missing files
       */
      for (Map.Entry<String, String> entry : remoteFiles.entrySet()) {
        String key = entry.getKey();

        if (key.startsWith(prefix) && !localFiles.containsKey(key)) {

          this.console.write("Delete " + key); //$NON-NLS-1$
          this.client.deleteObject(this.bucket, key);
        }
      }

      this.console.write("Sync done"); //$NON-NLS-1$
      this.console.newLine();

    } catch (IOException | AmazonClientException | InterruptedException e) {
      this.console.write("Cannot sync"); //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  private Map<String, String> getRemoteFiles(AmazonS3 client, String bucketName) {

    this.console.write("Get remote files..."); //$NON-NLS-1$

    ObjectListing listing;
    Map<String, String> files = new TreeMap<>();
    ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName);

    do {
      listing = client.listObjects(listObjectsRequest);
      for (S3ObjectSummary object : listing.getObjectSummaries())
        files.put(object.getKey(), object.getETag());
    } while (listing.isTruncated());

    this.console.write("Found " + files.size() + " remote files"); //$NON-NLS-1$ //$NON-NLS-2$
    return files;
  }

  private Map<String, Md5FilePair> getLocalFiles(File root, String prefix) throws IOException {
    this.console.write("Get locale files..."); //$NON-NLS-1$
    Map<String, Md5FilePair> files = new TreeMap<>();
    getLocalFilesRecursive(root, files, prefix);
    this.console.write("Found " + files.size() + " locale files"); //$NON-NLS-1$ //$NON-NLS-2$
    return files;
  }

  private void getLocalFilesRecursive(File folder, Map<String, Md5FilePair> list, String prefix)
      throws IOException {
    for (File file : folder.listFiles()) {
      if (file.isFile()) {
        String key = prefix + file.getName();
        list.put(key, new Md5FilePair(file));
      } else if (file.isDirectory()) {
        getLocalFilesRecursive(file, list, prefix + file.getName() + SLASH);
      }
    }
  }

  private static String getMD5Checksum(File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    return DigestUtils.md5Hex(fis);
  }

  private String addSlash(String prefix) {
    if (!prefix.endsWith(SLASH))
      return prefix += SLASH;
    return prefix;
  }

  /**
   * A locale helper class to put a file, togehter with its MD5 hash into a key-value map.
   */
  private static class Md5FilePair {
    final File file;
    final String md5;

    Md5FilePair(File file) throws IOException {
      this.file = file;
      this.md5 = getMD5Checksum(file);
    }
  }
}
