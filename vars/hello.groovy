#!/usr/bin/env groovy

def envVar = System.env

println(envVar)

def call() {
  echo "Printing Environment"
  println(envVar)
}

