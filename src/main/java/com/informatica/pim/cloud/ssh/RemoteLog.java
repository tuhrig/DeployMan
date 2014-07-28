/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.ssh;

import java.io.IOException;

import com.jcraft.jsch.JSchException;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class RemoteLog {
  public void printLast10LinesOfDeployManLog(String instanceId) {
    printLastLinesOfDeployManLog(instanceId, 10);
  }

  public void printLastLinesOfDeployManLog(String instanceId, int lines) {
    String tailCommand = "tail -" + lines + " /home/ubuntu/deployman.log"; //$NON-NLS-1$ //$NON-NLS-2$

    try {
      String result = new SshClient().runCommand(instanceId, tailCommand);
      console.write(tailCommand);
      for (String line : result.split(NL))
        console.write(TAB + line);
      console.newLine();
    } catch (JSchException | IOException e) {
      console.write("Cannot get log"); //$NON-NLS-1$
      e.printStackTrace();
    }
  }
}
