/**
 * DeployMan # Thomas Uhrig (Stuttgart, 2014) # www.tuhrig.de
 */
package com.informatica.pim.cloud.launch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.services.ec2.model.InstanceType;
import com.informatica.pim.cloud.launch.formation.Container;
import com.informatica.pim.cloud.launch.formation.Formation;
import com.informatica.pim.cloud.launch.formation.Machine;
import com.informatica.pim.cloud.repo.RemoteRepository;

/**
 * JSON has no standardized way to validate its schema. This is great, because it makes it easy to
 * write JSON and most validation tasks are to complicated for schemas eitherway. This call performs
 * simple and highlevel validation taks for formation files.
 * 
 * @author tuhrig
 */
@SuppressWarnings("nls")
public class FormationValidator {
  private static final String ERROR = "[ERROR]   ";
  private static final String WARNING = "[WARNING] ";
  private static final String SUCCESS = "[SUCCESS] ";

  public List<String> validate(String path) {
    Formation formation = Formation.read(path);
    return validate(formation);
  }

  public List<String> validate(File file) {
    Formation formation = Formation.read(file);
    return validate(formation);
  }

  public List<String> validate(Formation formation) {
    List<String> results = new ArrayList<>();

    validateFormationHasName(results, formation);
    validateFormationHasDescription(results, formation);
    validateFormationHasDefinition(results, formation);

    /*
     * Validate instance definition
     */
    if (formation.hasInstanceDefinition()) {
      Machine machine = formation.getMachine();

      validateMachineHasName(results, machine);
      validateMachineHasInstanceType(results, machine);
      validateMachineHasSecurityGroup(results, machine);
      validateMachineHasImageId(results, machine);

      for (Container setup : machine.getContainers()) {
        validateContainerHasName(results, setup);
        validateContainerHasImage(results, setup);
        validateContainerHasConfig(results, setup);
        validateContainerHasCommand(results, setup);
      }
    }

    /*
     * Validate database definition
     */
    if (formation.hasDatabaseDefinition()) {

    }

    return results;
  }

  private void validateContainerHasCommand(List<String> results, Container setup) {
    if (StringUtils.isEmpty(setup.getCommand()))
      results.add(ERROR + "You must provide a command for the container");
    else
      results.add(SUCCESS + "Command for the container is provided");
  }

  private void validateContainerHasConfig(List<String> results, Container setup) {
    if (StringUtils.isEmpty(setup.getConfig()))
      results.add(ERROR + "You must provide a config for the container");
    else {
      if (!new RemoteRepository().folderExistsRemote(setup.getConfig()))
        results.add(ERROR + "Config " + setup.getConfig() + " does not exists in S3 bucket"); //$NON-NLS-2$
      else
        results.add(SUCCESS + "Config exists in S3 bucket");
    }
  }

  private void validateContainerHasImage(List<String> results, Container setup) {
    if (StringUtils.isEmpty(setup.getTarball()))
      results.add(ERROR + "You must provide an image for the container");
    else {
      if (!new RemoteRepository().objectExistsRemote(setup.getTarball()))
        results.add(ERROR + "Image does not exists in S3 bucket");
      else
        results.add(SUCCESS + "Image exists in S3 bucket");
    }
  }

  private void validateContainerHasName(List<String> results, Container setup) {
    if (StringUtils.isEmpty(setup.getTarballName()))
      results.add(WARNING + "Provide a name for the container setup");
    else
      results.add(SUCCESS + "Container name is provided");
  }

  private void validateMachineHasImageId(List<String> results, Machine machine) {
    if (StringUtils.isEmpty(machine.getImageId()))
      results.add(ERROR + "You must provide an image id for the machine");
    else
      results.add(SUCCESS + "Image id is provided");
  }

  private void validateMachineHasSecurityGroup(List<String> results, Machine machine) {
    if (StringUtils.isEmpty(machine.getSecurityGroup()))
      results.add(ERROR + "You must provide a security group for the machine");
    else
      results.add(SUCCESS + "Security group is provided for the machine");
  }

  private void validateMachineHasInstanceType(List<String> results, Machine machine) {
    if (StringUtils.isEmpty(machine.getInstanceType()))
      results.add(ERROR + "You must provide an instance type for the machine");
    else {
      String instanceType = machine.getInstanceType();

      try {
        InstanceType.fromValue(instanceType);
        results.add(SUCCESS + "Valid instance type");
      } catch (IllegalArgumentException e) {
        results.add(ERROR + "Unknown instance type");
      }
    }
  }

  private void validateMachineHasName(List<String> results, Machine machine) {
    if (StringUtils.isEmpty(machine.getName()))
      results.add(ERROR + "You must provide a name for the machine");
    else
      results.add(SUCCESS + "Machine name is provided");
  }

  private void validateFormationHasDefinition(List<String> results, Formation formation) {
    if (!formation.hasInstanceDefinition() && !formation.hasDatabaseDefinition())
      results.add(ERROR + "You must provide a instance or database definition");
    else
      results.add(SUCCESS + "Instance or database definition is provided");
  }

  private void validateFormationHasDescription(List<String> results, Formation formation) {
    if (StringUtils.isEmpty(formation.getDescription()))
      results.add(WARNING + "Provide a description for the formation");
    else
      results.add(SUCCESS + "Formation description is provided");
  }

  private void validateFormationHasName(List<String> results, Formation formation) {
    if (StringUtils.isEmpty(formation.getName()))
      results.add(WARNING + "Provide a name for the formation");
    else
      results.add(SUCCESS + "Formation name is provided");
  }
}
