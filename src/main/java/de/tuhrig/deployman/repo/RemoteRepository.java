/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.repo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

import de.tuhrig.deployman.aws.Aws;
import de.tuhrig.deployman.aws.S3;
import de.tuhrig.deployman.aws.SharpProgressListener;
import de.tuhrig.deployman.model.Size;
import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class RemoteRepository implements IRepository {
  private AmazonS3 s3 = new S3().getClient();

  public boolean objectExistsRemote(String objectName) {
    try {
      String bucketName = getUserProperty(REPO_BUCKET);
      this.s3.getObject(bucketName, objectName);
    } catch (AmazonServiceException e) {
      String errorCode = e.getErrorCode();
      if (!errorCode.equals("NoSuchKey")) //$NON-NLS-1$
      {
        throw e;
      }

      return false;
    }

    return true;
  }

  public boolean folderExistsRemote(String objectName) {
    try {
      String bucketName = getUserProperty(REPO_BUCKET);
      return this.s3.listObjects(bucketName, objectName).getObjectSummaries().size() > 0;
    } catch (AmazonServiceException e) {
      String errorCode = e.getErrorCode();
      if (!errorCode.equals("NoSuchKey")) //$NON-NLS-1$
      {
        throw e;
      }

      return false;
    }
  }

  public boolean folderExistsInRootOfBucket(String key) {
    if (!key.endsWith(SLASH))
      key += SLASH;

    ObjectListing objects = listObjectsInRootFolder();
    for (String commonPrefix : objects.getCommonPrefixes()) {
      if (commonPrefix.equals(key))
        return true;
    }
    return false;
  }

  public long getSizeOfRemoteFolder(String folderPath) {
    ObjectListing objectListing = listObjectsWithPrefix(folderPath);

    return objectListing.getObjectSummaries().stream().mapToLong(os -> os.getSize()).sum();
  }

  public long getNumberOfFilesOfRemoteFolder(String folderPath) {
    ObjectListing objectListing = listObjectsWithPrefix(folderPath);

    return objectListing.getObjectSummaries().stream().count();
  }

  public List<String> getRemoteFolders(String prefix) {

    String bucket = getUserProperty(REPO_BUCKET);
    ObjectListing listing =
        this.s3.listObjects(new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix)
            .withDelimiter("/")); //$NON-NLS-1$

    return listing.getCommonPrefixes().stream().map(o -> o.substring(0, o.length() - 1))
        .collect(Collectors.toList());
  }

  public void createRemoteFolder(String bucket, String folder) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);
    InputStream emptyContent = new ByteArrayInputStream(new byte[0]);

    PutObjectRequest createImagesFolder =
        new PutObjectRequest(bucket, folder, emptyContent, metadata);
    this.s3.putObject(createImagesFolder);
  }

  private void waitForUpload(Transfer upload, TransferManager tm) throws AmazonServiceException,
      AmazonClientException, InterruptedException {
    long bytes = upload.getProgress().getTotalBytesToTransfer();

    console.write(new Size(bytes) + " to upload"); //$NON-NLS-1$

    long fraction = bytes / 50;

    upload.addProgressListener(new SharpProgressListener(fraction));
    upload.waitForCompletion();

    tm.shutdownNow();
    console.write("\nDone"); //$NON-NLS-1$
  }

  public List<S3ObjectSummary> getRemoteObjects(String prefix) {
    ObjectListing objectListing = listObjectsWithPrefix(prefix);

    return objectListing.getObjectSummaries().stream().filter(obj -> !obj.getKey().endsWith(SLASH))
        .collect(Collectors.toList());
  }

  private ObjectListing listObjectsWithPrefix(String prefix) {
    String bucket = getUserProperty(REPO_BUCKET);
    return this.s3.listObjects(new ListObjectsRequest().withBucketName(bucket).withPrefix(prefix));
  }

  private ObjectListing listObjectsInRootFolder() {
    String bucket = getUserProperty(REPO_BUCKET);
    return this.s3
        .listObjects(new ListObjectsRequest().withBucketName(bucket).withDelimiter(SLASH));
  }

  public void uploadFile(File file, String key) throws AmazonServiceException,
      AmazonClientException, InterruptedException {
    TransferManager tm = new TransferManager(new Aws().getAwsCredentials());
    Upload upload = tm.upload(getUserProperty(REPO_BUCKET), key, file);
    waitForUpload(upload, tm);
  }

  public void uploadFileSilently(File file, String key) throws AmazonServiceException,
      AmazonClientException {
    TransferManager tm = new TransferManager(new Aws().getAwsCredentials());
    tm.upload(getUserProperty(REPO_BUCKET), key, file);
  }

  public void uploadFolder(File folder, String key) throws AmazonServiceException,
      AmazonClientException, InterruptedException {
    TransferManager tm = new TransferManager(new Aws().getAwsCredentials());
    MultipleFileUpload upload = tm.uploadDirectory(getUserProperty(REPO_BUCKET), key, folder, true);
    waitForUpload(upload, tm);
  }

  @Override
  public void init() {
    new ImageRepository().initRemote();
    new ConfigRepository().initRemote();
  }

  @Override
  public boolean exists() {
    return new ImageRepository().existsRemote() && new ConfigRepository().existsRemote();
  }

  @Override
  public void printInfo() {
    String bucket = getUserProperty(REPO_BUCKET);

    if (exists()) {
      console.write("Remote repository: " + bucket + " (exists)"); //$NON-NLS-1$ //$NON-NLS-2$
      console.newLine();

      new ImageRepository().printRemoteObjects();
      new ConfigRepository().printLocaleObjects();
    } else {
      if (!new ImageRepository().existsRemote())
        console.write("Remote image repository in '" + bucket + "' is missing"); //$NON-NLS-1$ //$NON-NLS-2$

      if (!new ConfigRepository().existsRemote())
        console.write("Remote config repository in '" + bucket + "' is missing"); //$NON-NLS-1$ //$NON-NLS-2$

      console.newLine();
    }
  }
}
