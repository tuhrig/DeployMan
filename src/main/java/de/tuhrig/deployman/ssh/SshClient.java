/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.tuhrig.deployman.aws.Ec2;
import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class SshClient {

  private static final String CLOUD_INIT_LOCATION = "/var/lib/cloud/instance/user-data.txt"; //$NON-NLS-1$

  private static final String SSH_USER = "ubuntu"; //$NON-NLS-1$

  private static final Integer SSH_PORT = 22;

  private static final Integer SESSION_CACHE_SIZE = 10;

  private static final Integer SESSION_CACHE_TIME = 10;

  /**
   * A Guave Cache which holds _open_ SSH session. This makes all SSH operations much faster, since
   * the connection can be reused. The cache has a limited size and all sessions have a limited life
   * time. Sessions will be closed on removal.
   */
  private static final Cache<String, Session> sessions = CacheBuilder.newBuilder()
      .maximumSize(SESSION_CACHE_SIZE).expireAfterWrite(SESSION_CACHE_TIME, TimeUnit.MINUTES)
      .removalListener(new RemovalListener<String, Session>() {
        @Override
        public void onRemoval(RemovalNotification<String, Session> event) {
          event.getValue().disconnect();
        }
      }).build();

  public String getCloutInitScript(String instanceId) {

    try {
      return runCommand(instanceId, "sudo cat " + CLOUD_INIT_LOCATION); //$NON-NLS-1$;
    } catch (JSchException | IOException e) {
      return "error on getting cloud init script: " + e.getMessage(); //$NON-NLS-1$
    }
  }

  public String runCommand(String instanceId, String command) throws JSchException, IOException {

    JSch jsch = getSshClient();

    String hostName = new Ec2().getHostNameOfInstance(instanceId);

    Session session = getSession(jsch, hostName);

    try {
      String result = runCommand(session, command);
      return result.trim();
    } catch (JSchException e) {
      removeSession(hostName);
      throw e;
    }
  }

  private void removeSession(String hostName) {
    sessions.invalidate(hostName);
  }

  public void openSshShell(String instanceIndex) {
    console.write("Connecting..."); //$NON-NLS-1$

    try {
      JSch jsch = getSshClient();
      String hostName = new Ec2().getHostNameOfInstance(instanceIndex);

      console.write("Host " + hostName); //$NON-NLS-1$
      console.write("Type 'exit' to quite"); //$NON-NLS-1$

      Session session = getSession(jsch, hostName);

      while (true) {
        String command = readUserInput();

        if (command.equals("exit")) //$NON-NLS-1$
          break;

        String result = runCommand(session, command);

        console.write(result);
      }
    } catch (Exception e) {
      console.write("Cannot open SSH session"); //$NON-NLS-1$
      e.printStackTrace();
    }
  }

  public String readUserInput() throws IOException {
    System.out.print("$: "); //$NON-NLS-1$
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    return input.readLine();
  }

  private String runCommand(Session session, String command) throws IOException, JSchException {

    Channel channel = session.openChannel("exec"); //$NON-NLS-1$
    ((ChannelExec) channel).setCommand(command);
    channel.connect();

    InputStream inputStream = channel.getInputStream();
    String result = IOUtils.toString(inputStream);
    IOUtils.closeQuietly(inputStream);
    channel.disconnect();
    return result;
  }

  /**
   * Returns an SSH session for the given host name. If a session for the host already exists, the
   * cached session is returned. Otherwise, a new session will be created (and stored in the cache).
   */
  private Session getSession(JSch jsch, String hostName) throws JSchException {

    // return the (open) session if it is already present
    Session session = sessions.getIfPresent(hostName);
    if (session != null)
      return session;

    // otherwise, make a new session and add it to the cache
    return createAndCacheSession(jsch, hostName);
  }

  /**
   * Creates a new SSH session and stores it in the session cache.
   */
  private Session createAndCacheSession(JSch jsch, String hostName) throws JSchException {

    Session session = jsch.getSession(SSH_USER, hostName, SSH_PORT);
    session.connect();
    sessions.put(hostName, session);
    return session;
  }

  /**
   * Returns a new SSH client with the configured SSH key. This client can be used to create a new
   * SSH session.
   */
  private JSch getSshClient() throws JSchException {

    JSch jsch = new JSch();
    jsch.addIdentity(getUserProperty(SSH_KEY));
    JSch.setConfig("StrictHostKeyChecking", "no"); //$NON-NLS-1$ //$NON-NLS-2$
    return jsch;
  }
}
