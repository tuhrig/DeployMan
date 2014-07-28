/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.health;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.services.ec2.model.Instance;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.informatica.pim.cloud.aws.Ec2;
import com.informatica.pim.cloud.launch.Launcher;
import com.informatica.pim.cloud.launch.formation.Container;
import com.informatica.pim.cloud.ssh.SshClient;
import com.jcraft.jsch.JSchException;
import com.sun.jersey.api.client.ClientHandlerException;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * A MachineStatus contains the "status" of a EC2 machine. Therefore it can be created with the ID
 * of an EC2 machine. The status of a machine is determined by its log.
 * 
 * @author tuhrig
 */
public class MachineStatus {
  /**
   * The current log file from the machine.
   */
  private String deploymentLog;

  /**
   * The deployment steps (called states) of the machine. Each deployed container has various steps
   * (download, load, start).
   */
  private List<Status> states = new ArrayList<>();

  /**
   * Some general meta information about the machine.
   */
  private MetaInfo metaInfo;

  /**
   * EC2 instance.
   */
  private Instance machine;

  /**
   * Information about the running Docker instance. E.g. which containers are running and which
   * images are present.
   */
  private DockerInfo dockerInfo;

  /**
   * The cloud init script which created the instance.
   */
  private String cloudInitScript;

  private static final Integer SESSION_CACHE_SIZE = 100;

  private static final Integer SESSION_CACHE_TIME = 15;

  private static final Cache<String, MachineStatus> machines = CacheBuilder.newBuilder()
      .maximumSize(SESSION_CACHE_SIZE).expireAfterWrite(SESSION_CACHE_TIME, TimeUnit.MINUTES)
      .build();

  public static MachineStatus getMachineStatus(String instanceId) {
    MachineStatus machineStatus = machines.getIfPresent(instanceId);

    if (machineStatus != null) {
      String currentLog = getCompleteDeploymentLog(instanceId);

      machineStatus.setDeploymentLog(currentLog);
      machineStatus.updateLogDependingInformation();
      machineStatus.updateDockerInfo(instanceId);

      return machineStatus;
    }

    return new MachineStatus(instanceId);
  }

  private MachineStatus(String instanceId) {
    updateDeploymentLog(instanceId);
    updateCloudInitScript(instanceId);
    updateMachineInformation(instanceId);
    updateDockerInfo(instanceId);

    // the log is null if the machine didn't booted yet because
    // we cannot get the log until the machine is up (right?)
    if (this.deploymentLog != null)
      updateLogDependingInformation();

    // We add this object to the machine cache. The creation
    // of a new machine object is very expensive (e.g. because
    // of the SSH connection), so we reuse it.
    machines.put(this.machine.getInstanceId(), this);
  }

  private void updateDeploymentLog(String instanceId) {
    String currentLog = getCompleteDeploymentLog(instanceId);
    setDeploymentLog(currentLog);
  }

  private void updateCloudInitScript(String instanceId) {
    String cloudInit = new SshClient().getCloutInitScript(instanceId);
    this.setCloudInitScript(cloudInit);
  }

  private void updateMachineInformation(String instanceId) {
    this.machine = new Ec2().getEC2InstanceById(instanceId);
  }

  private void updateDockerInfo(String instanceId) {
    try {
      this.dockerInfo = new DockerInfo(instanceId);
    } catch (ClientHandlerException e) {
      console.exception(e,
          "Cannot get Docker info. Is the machine already up and Docker installed?"); //$NON-NLS-1$
    }
  }

  private void updateLogDependingInformation() {

    setMetaInfo(this.deploymentLog);

    this.states = new ArrayList<>();
    addStatus(new Status("Booting", getStatusOfBooting()));
    addStatus(new Status("Initialization", getStatusOfInitialization()));

    int pulls = 1;
    int tars = 1;
    int total = 1;

    for (Container setup : this.metaInfo.getFormation().getMachine().getContainers()) {

      if (setup.hasImage()) {

        addStatus(new Status("Pull " + setup.getImage(), getStatusOfImageDownload(pulls)));
        addStatus(new Status("Download " + setup.getConfig(), getStatusOfConfigDownload(pulls)));
        addStatus(new Status("Start " + setup.getImage(), getStatusOfImageStart(total)));
        pulls++;

      } else {

        addStatus(new Status("Download " + setup.getTarball(), getStatusOfTarballDownload(tars)));
        addStatus(new Status("Download " + setup.getConfig(), getStatusOfConfigDownload(total)));
        addStatus(new Status("Load " + setup.getTarball(), getStatusOfImageLoad(tars)));
        addStatus(new Status("Start " + setup.getTarball(), getStatusOfImageStart(total)));
        tars++;
      }

      total++;
    }
  }

  private void addStatus(Status status) {

    this.states.add(status);
  }

  private void setDeploymentLog(String log) {
    this.deploymentLog = log;
  }

  private void setMetaInfo(String log) {
    this.metaInfo = new MetaInfo(log);
  }

  /**
   * Returns the complete deployment log of an instance. The log is requested via SSH.
   */
  public static String getCompleteDeploymentLog(String instanceId) {
    try {
      SshClient ssh = new SshClient();
      return ssh.runCommand(instanceId, "cat " + Launcher.DEPLOYMENT_LOG_FILE); //$NON-NLS-1$
    } catch (JSchException | IOException e) {
      console.write("Cannot get deployment log"); //$NON-NLS-1$
      // we don't print the stack trace, since during the
      // bootprocess this exception is expected (because
      // the machine is not reachable yet)
      e.printStackTrace();
    }
    return null;
  }

  public List<Status> getSetupStates() {
    return this.states;
  }

  private DeploymentStatus getStatusOfBooting() {
    if (this.deploymentLog != null)
      return DeploymentStatus.DONE;
    return DeploymentStatus.PENDING;
  }

  private DeploymentStatus getStatusOfInitialization() {
    return getStatusOfInitialization(this.deploymentLog);
  }

  private DeploymentStatus getStatusOfImageDownload(int number) {
    return getStatusOfImageDownload(this.deploymentLog, number);
  }

  private DeploymentStatus getStatusOfTarballDownload(int number) {
    return getStatusOfTarballDownload(this.deploymentLog, number);
  }

  private DeploymentStatus getStatusOfConfigDownload(int number) {
    return getStatusOfConfigDownload(this.deploymentLog, number);
  }

  private DeploymentStatus getStatusOfImageLoad(int number) {
    return getStatusOfImageLoad(this.deploymentLog, number);
  }

  private DeploymentStatus getStatusOfImageStart(int number) {
    return getStatusOfImageStart(this.deploymentLog, number);
  }

  private DeploymentStatus getStatus(String log, String doneMessage, String progressMessage) {
    if (log.contains(doneMessage))
      return DeploymentStatus.DONE;
    if (log.contains(progressMessage))
      return DeploymentStatus.INPROGRESS;
    return DeploymentStatus.PENDING;
  }

  private DeploymentStatus getStatus(String log, String doneMessage, String progressMessage,
      int number) {
    int occuranceDone = StringUtils.countMatches(log, doneMessage);
    int occuranceInProgress = StringUtils.countMatches(log, progressMessage);

    if (occuranceDone >= number)
      return DeploymentStatus.DONE;
    if (occuranceInProgress >= number)
      return DeploymentStatus.INPROGRESS;
    return DeploymentStatus.PENDING;
  }

  private DeploymentStatus getStatusOfImageDownload(String log, int number) {
    String doneString = "downloading image"; //$NON-NLS-1$
    String progressString = "downloaded image"; //$NON-NLS-1$
    return getStatus(log, doneString, progressString, number);
  }

  private DeploymentStatus getStatusOfTarballDownload(String log, int number) {
    String doneString = "copied image"; //$NON-NLS-1$
    String progressString = "copying image"; //$NON-NLS-1$
    return getStatus(log, doneString, progressString, number);
  }

  private DeploymentStatus getStatusOfImageLoad(String log, int number) {
    String doneString = "loaded Docker image"; //$NON-NLS-1$
    String progressString = "loading Docker image"; //$NON-NLS-1$
    return getStatus(log, doneString, progressString, number);
  }

  private DeploymentStatus getStatusOfInitialization(String log) {
    String doneString = "installed AWS CLI"; //$NON-NLS-1$
    String progressString = "installing Docker..."; //$NON-NLS-1$
    return getStatus(log, doneString, progressString);
  }

  private DeploymentStatus getStatusOfConfigDownload(String log, int number) {
    String doneMessage = "copied config"; //$NON-NLS-1$
    String progressMessage = "copying config"; //$NON-NLS-1$
    return getStatus(log, doneMessage, progressMessage, number);
  }

  private DeploymentStatus getStatusOfImageStart(String log, int number) {
    String doneMessage = "started command"; //$NON-NLS-1$
    String progressMessage = "starting command"; //$NON-NLS-1$
    return getStatus(log, doneMessage, progressMessage, number);
  }

  public Instance getMachine() {
    return this.machine;
  }

  public void setMachine(Instance machine) {
    this.machine = machine;
  }

  public DockerInfo getDockerInfo() {
    return this.dockerInfo;
  }

  public void setDockerInfo(DockerInfo dockerInfo) {
    this.dockerInfo = dockerInfo;
  }

  public String getCloudInitScript() {
    return cloudInitScript;
  }

  public void setCloudInitScript(String cloudInitScript) {
    this.cloudInitScript = cloudInitScript;
  }
}
