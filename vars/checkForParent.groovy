#!/usr/bin/env groovy
import at.gepardec.DtUtils

def call() {

  call(
        env.deptrack_url,
        deptrack_api_key,
        env.deptrack_project,
        env.deptrack_version
  )
}

def call(String url, String key, String projectName, String projectVer = null) {

  def dt = new DtUtils()
  dt.connectWith(url, key)
  if ( !dt.dTcheckForParent1( projectName, projectVer) ){
    throw new RuntimeException( "FAIL: ${projectName} ${projectVer} failed")
  }
}


/* -------------------------------------------------------
 * MAIN
 * ----------------------------------------------------- */


def inJenkins = System.env['JENKINS_HOME'] != null

if ( inJenkins ){
  println( "Running in Jenkins")
}
else{
  println( "Running standalone")
  def dt = new DtUtils()
  (projectName, projectVer, projectUuid, listOnly) = dt.handleOptions(args)
  if (listOnly) {
    dt.listProjects()
    System.exit(0)
  }

  if (  dt.dTcheckForParent( projectName, projectVer, projectUuid) ){
    System.err.println("OK: ${projectName} ${projectVer}")
    System.exit(0)
  }
  else{
    System.err.println("FAIL: ${projectName} ${projectVer} failed")
    System.exit(1)
  }
}




