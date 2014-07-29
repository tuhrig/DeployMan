/**************************************************************************************************
 *  ____  _  _  ____  _____  ____  __  __    __   ____  ____  ___    __   
 * (_  _)( \( )( ___)(  _  )(  _ \(  \/  )  /__\ (_  _)(_  _)/ __)  /__\  
 *  _)(_  )  (  )__)  )(_)(  )   / )    (  /(__)\  )(   _)(_( (__  /(__)\ 
 * (____)(_)\_)(__)  (_____)(_)\_)(_/\/\_)(__)(__)(__) (____)\___)(__)(__) 
 * 
 * Informatica PIM
 *
 * copyright: Informatica Corp. (c) 2003-2013.  All rights reserved.
 * 
 *************************************************************************************************/

package de.tuhrig.deployman.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AddRoleToInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileRequest;
import com.amazonaws.services.identitymanagement.model.CreateInstanceProfileResult;

import static de.tuhrig.deployman.DeployMan.*;

/**
 * @author tuhrig
 */
public class Aim implements IAws< AmazonIdentityManagement >
{
  @Override
  public AmazonIdentityManagement getClient()
  {
    AWSCredentials credentials = new Aws().getAwsCredentials();
    return new AmazonIdentityManagementClient( credentials );
  }

  public void createS3BucketProfile()
  {
    AmazonIdentityManagement aim = getClient();

    String profile = getUserProperty( REPO_PROFILE );
    String role = getUserProperty( REPO_ROLE );

    if ( instanceProfileExists( profile ) )
    {
      console.write( "Profile '" + profile + "' already exists" ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    CreateInstanceProfileResult result = aim.createInstanceProfile( new CreateInstanceProfileRequest().withInstanceProfileName( profile ) );

    // add roles to profil
    aim.addRoleToInstanceProfile( new AddRoleToInstanceProfileRequest().withInstanceProfileName( profile )
                                                                       .withRoleName( role ) );

    console.write( "Profile '" + profile + "' created at " + result.getInstanceProfile().getCreateDate() ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private boolean instanceProfileExists( String instanceProfile )
  {
    return getClient().listInstanceProfiles()
                      .getInstanceProfiles()
                      .stream()
                      .filter( profil -> profil.getInstanceProfileName()
                                               .equals( instanceProfile ) )
                      .count() > 0;
  }
}
