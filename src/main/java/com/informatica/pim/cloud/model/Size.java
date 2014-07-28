/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.model;

import java.io.File;

import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * @author tuhrig
 */
public class Size {
  private int totalFiles = 0;
  private long fileSizeInBytes = 0;

  public Size(File file) {
    if (file.isFile())
      this.fileSizeInBytes = file.length();
    else
      this.fileSizeInBytes = getFileSize(file);
  }

  public Size(long size) {
    this.fileSizeInBytes = size;
  }

  public Size(S3ObjectSummary file) {
    this(file.getSize());
  }

  private long getAsKilobytes(long bytes) {
    return bytes / 1024;
  }

  private long getAsMegabytes(long bytes) {
    return getAsKilobytes(bytes) / 1024;
  }

  private long getAsGigabytes(long bytes) {
    return getAsMegabytes(bytes) / 1024;
  }

  @Override
  public String toString() {
    if (this.fileSizeInBytes > 1000000000)
      return getAsGigabytes(this.fileSizeInBytes) + " GB"; //$NON-NLS-1$

    if (this.fileSizeInBytes > 1000000)
      return getAsMegabytes(this.fileSizeInBytes) + " MB"; //$NON-NLS-1$

    if (this.fileSizeInBytes > 1000)
      return getAsKilobytes(this.fileSizeInBytes) + " KB"; //$NON-NLS-1$

    return this.fileSizeInBytes + " B"; //$NON-NLS-1$
  }

  public long getFileSize(File folder) {
    this.totalFiles = 0;
    long foldersize = 0;

    for (File file : folder.listFiles()) {
      if (file.isDirectory())
        foldersize += getFileSize(file);

      else {
        this.totalFiles++;
        foldersize += file.length();
      }
    }
    return foldersize;
  }

  public int getTotalFile() {
    return this.totalFiles;
  }
}
