#!/usr/bin/env groovy


def call(String name = 'human') {
  echo "Env, ${name}."
  println(System.env)
}

