#!/usr/bin/env groovy

import hudson.model.*
import hudson.model.TaskListener
 
def call(String name = 'human') {

  echo "Env, ${name}."
  def envVar = System.env['HOME']
  echo "HOME: ${envVar}."

  def envVars = System.env
  echo "All: ${envVars}."
}

