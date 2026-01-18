#!/usr/bin/env groovy

 
def call(String name = 'human') {

  echo "Env, ${name}."
  def envVar = System.env['HOME']
  echo "HOME: ${envVar}."

  def envVars = System.env
  echo "All: ${envVars}."
}

def inJenkins = System.env['JENKINS_HOME'] != null

if ( inJenkins ){
  println( "Running in Jenkins")
}
else{
  println( "Running standalone")
}
