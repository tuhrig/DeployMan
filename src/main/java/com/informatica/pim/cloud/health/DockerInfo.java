/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.health;

import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.client.DockerClient;
import com.github.dockerjava.client.DockerException;
import com.github.dockerjava.client.model.Container;
import com.github.dockerjava.client.model.Image;
import com.github.dockerjava.client.model.Version;
import com.informatica.pim.cloud.docker.DockerRemoteClient;

import static com.informatica.pim.cloud.DeployMan.*;

/**
 * @author tuhrig
 */
public class DockerInfo {
  private List<Image> images;
  private List<DockerContainerInfo> containers = new ArrayList<>();
  private Version version;

  public DockerInfo(String instanceId) {
    try {
      DockerRemoteClient client = new DockerRemoteClient();
      DockerClient docker = client.getDockerClientForInstance(instanceId);

      this.images = docker.listImagesCmd().exec();
      this.version = docker.versionCmd().exec();

      for (Container container : docker.listContainersCmd().withShowAll(true).exec())
        this.containers.add(new DockerContainerInfo(docker, container));
    } catch (DockerException e) {
      console.exception(e, "Cannot get Docker information"); //$NON-NLS-1$
    }
  }

  public List<Image> getImages() {
    return this.images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
  }

  public List<DockerContainerInfo> getContainers() {
    return this.containers;
  }

  public void setContainers(List<DockerContainerInfo> containers) {
    this.containers = containers;
  }

  public Version getVersion() {
    return this.version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }
}
