#!/usr/bin/env groovy
import at.gepardec.DtUtils

def call(String url, String key, String projectName, String projectVer = null) {

  def dt = new DtUtils()
  dt.connectWith(url, key)
  dt.dTcheckForParent( projectName, projectVer, (String)null)
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
    listProjects()
    System.exit(0)
  }

  try {
    dt.dTcheckForParent( projectName, projectVer, projectUuid)
  } catch (Exception e) {
    System.err.println("ERROR: ${e.message}")
    System.exit(1)
  }
}




