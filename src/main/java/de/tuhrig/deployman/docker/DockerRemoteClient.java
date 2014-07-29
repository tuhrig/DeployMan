/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package de.tuhrig.deployman.docker;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import com.amazonaws.services.ec2.model.Instance;
import com.github.dockerjava.client.DockerClient;
import com.github.dockerjava.client.DockerException;
import com.github.dockerjava.client.model.Container;
import com.github.dockerjava.client.model.Image;
import com.github.dockerjava.client.model.Info;
import com.sun.jersey.api.client.ClientResponse;

import de.tuhrig.deployman.aws.Ec2;
import de.tuhrig.deployman.interfaces.WebInterface;
import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class DockerRemoteClient {
  private static final Logger log = Logger.getLogger(WebInterface.class.getName());

  private static final String DOCKER_PORT = "4243"; //$NON-NLS-1$
  private static final String COLON = ":"; //$NON-NLS-1$
  private static final String HTTP = "http://"; //$NON-NLS-1$

  // public void printImages( String instanceId )
  // {
  // try
  // {
  //      String result = new SshClient().runCommand( instanceId, "sudo docker images" ); //$NON-NLS-1$
  //
  // console.write( result );
  // console.newLine();
  // }
  // catch ( JSchException | IOException e )
  // {
  //      console.write( "Cannot list Docker images" ); //$NON-NLS-1$
  // e.printStackTrace();
  // }
  // }

  public DockerClient getDockerClientForInstance(String instanceId) throws DockerException {
    Instance instance = new Ec2().getEC2InstanceById(instanceId);
    String dockerUrl = HTTP + instance.getPublicIpAddress() + COLON + DOCKER_PORT;
    log.info("Get Docker client for " + dockerUrl); //$NON-NLS-1$
    return new DockerClient(dockerUrl);
  }

  public void stopContainer(String instanceId, String containerId) {
    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      dockerClient.stopContainerCmd(containerId);
    } catch (DockerException e) {
      console.exception(e, "Cannot stop Docker container"); //$NON-NLS-1$
    }
  }

  public void removeContainer(String instanceId, String containerId) {
    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      dockerClient.removeContainerCmd(containerId);
    } catch (DockerException e) {
      console.exception(e, "Cannot remove Docker container"); //$NON-NLS-1$
    }
  }

  public void startContainer(String instanceId, String containerId) {
    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      dockerClient.startContainerCmd(containerId);
    } catch (DockerException e) {
      console.exception(e, "Cannot start Docker container"); //$NON-NLS-1$
    }
  }

  public DockerContainer getContainer(String instanceId, String containerId) {
    DockerContainer container = new DockerContainer();
    container.setLog(getLogOfContainer(instanceId, containerId));
    container.setId(containerId);
    return container;
  }

  public String getLogOfContainer(String instanceId, String containerId) {
    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      ClientResponse response =
          dockerClient.logContainerCmd(containerId).withStdOut().withStdErr().exec();

      StringBuilder builder = new StringBuilder();

      try {
        LineIterator itr = IOUtils.lineIterator(response.getEntityInputStream(), "UTF-8"); //$NON-NLS-1$
        while (itr.hasNext()) {
          String line = itr.next();
          builder.append(line);
          builder.append(NL);
        }
      } finally {
        IOUtils.closeQuietly(response.getEntityInputStream());
      }

      return builder.toString();
    } catch (DockerException | IOException e) {
      console.exception(e, "Cannot get Docker log"); //$NON-NLS-1$
    }

    return "no log available"; //$NON-NLS-1$
  }

  public void printImages(String instanceId) {
    console.write("List Docker images of " + instanceId); //$NON-NLS-1$

    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      List<Image> images = dockerClient.listImagesCmd().exec();
      console.printImages(images);
    } catch (DockerException e) {
      console.exception(e, "Cannot print Docker information"); //$NON-NLS-1$
    }
  }

  public void printInfo(String instanceId) {
    console.write("Docker info of " + instanceId); //$NON-NLS-1$

    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      Info info = dockerClient.infoCmd().exec();
      console.printDockerInfo(info);
    } catch (DockerException e) {
      console.exception(e, "Cannot print Docker information"); //$NON-NLS-1$
    }
  }

  public void printContainers(String instanceId) {
    console.write("Docker containers of " + instanceId); //$NON-NLS-1$
    try {
      DockerClient dockerClient = getDockerClientForInstance(instanceId);
      List<Container> containers = dockerClient.listContainersCmd().exec();
      console.printContainers(containers);
    } catch (DockerException e) {
      console.exception(e, "Cannot print Docker containers"); //$NON-NLS-1$
    }
  }
}
