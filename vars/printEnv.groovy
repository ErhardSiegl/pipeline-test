#!/usr/bin/env groovy

import hudson.model.*
import hudson.model.TaskListener
 
def call(String name = 'human') {

  echo "Env, ${name}."
  println(System.env)

  def build = Thread.currentThread().executable
  def envVars = build.getEnvironment(TaskListener.NULL)
 
  // Access the BUILD_NUMBER variable
  def buildNumber = envVars.get('BUILD_NUMBER')
 
  if (buildNumber) {
      println "Current build number: ${buildNumber}"
  } else {
      println "BUILD_NUMBER not found!"
  }
}

