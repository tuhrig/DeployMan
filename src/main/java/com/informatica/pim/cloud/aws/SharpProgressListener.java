/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.aws;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;

/**
 * @author tuhrig
 */
public class SharpProgressListener implements ProgressListener {
  private long progress = 0;
  private long total = 0;
  private long fraction;

  public SharpProgressListener(long fraction) {
    this.fraction = fraction;
  }

  @Override
  public void progressChanged(ProgressEvent progressEvent) {
    this.total += progressEvent.getBytesTransferred();

    if (this.total >= this.progress) {
      System.out.print("#"); //$NON-NLS-1$
      this.progress += this.fraction;
    }
  }
}
