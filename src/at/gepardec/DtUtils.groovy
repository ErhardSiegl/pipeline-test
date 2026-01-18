package at.gepardec

import groovy.json.JsonSlurper
import java.net.HttpURLConnection

class DtUtils {

/**
 * Dependency-Track project checker
 *
 * Checks whether a Dependency-Track project has a parent set.
 * See Option -h for basic usage
 *
 * Required env vars (or change below):
 *   DT_API_URL      e.g. https://dtrack.example.com/api/v1
 *   DT_API_KEY      e.g. odt_12345678901234567890
 *
 * Exit codes:
 *   0 = parent is set
 *   2 = parent NOT set
 *   1 = error
 */

def dtrackUrl   = (System.getenv("DT_API_URL") ?: "").trim()
def apiKey      = (System.getenv("DT_API_KEY") ?: "").trim()

def connectWith( String url, String key){
  dtrackUrl = url
  apiKey = key
}

/* -------------------------------------------------------
 * Debugging and Error Handling^
 * ----------------------------------------------------- */
def log = { String message ->
  def ERROR = 0
  def WARN = 1
  def INFO = 2
  def DEBUG = 3
  def debugLevels = [ "ERROR", "WARN", "INFO", "DEBUG" ]

  def debugLevel = WARN
  def level = INFO

  if ( debugLevel >= level ){
    def l = debugLevels[ level]
    System.err.println( l + ": " + message)
  }
}

def fatal = { String message ->
  throw new RuntimeException( message)
}

/* -------------------------------------------------------
 * HTTP Call 
 * return body
 * ----------------------------------------------------- */
def request = { String method, String path ->

  if (!dtrackUrl || !apiKey) {
    fatal( "ERROR: DT_API_URL and DT_API_KEY must be set.")
  }
  log("dtrackUrl: " + dtrackUrl)

  def slurper = new JsonSlurper()
  def url = new URL("${dtrackUrl.replaceAll('/\$','')}${path}")
  HttpURLConnection conn = (HttpURLConnection) url.openConnection()
  conn.setRequestMethod(method)
  conn.setRequestProperty("X-Api-Key", apiKey)
  conn.setRequestProperty("Accept", "application/json")
  conn.setConnectTimeout(15000)
  conn.setReadTimeout(30000)

  int code = conn.responseCode
  def stream = (code >= 200 && code < 300) ? conn.inputStream : conn.errorStream
  def body = stream != null ? stream.getText("UTF-8") : ""

  if (!(code >= 200 && code < 300)) {
    fatal("HTTP ${code} calling ${url}: ${body}")
  }

  return body ? slurper.parseText(body) : null
}

/* -------------------------------------------------------
 * List Projects
 * ----------------------------------------------------- */
def listProjects = { 

  int page = 1
  int pageSize = 100
  boolean more = true

  println(String.format("%-38s  %-40s  %s", "UUID", "NAME", "VERSION"))
  println("-" * 100)

  while (more) {
    def projects = request(
      "GET",
      "/project?pageNumber=${page}&pageSize=${pageSize}"
    )

    if (!projects || projects.isEmpty()) {
      more = false
      break
    }

    projects.each { p ->
      println(String.format(
        "%-38s  %-40s  %s",
        p.uuid,
        p.name ?: "",
        p.version ?: ""
      ))
    }

    page++
  }
}

/* -------------------------------------------------------
 * Resolve project UUID if not provided
 * ----------------------------------------------------- */
def resolveProjectUuid = { String uuid, String name, String version ->
  if (uuid) return uuid
  if (!name ) {
    fatal( "PROJECT_UUID or PROJECT_NAME + PROJECT_VERSION must be set.")
  }
  // DTrack endpoint: GET /project/lookup?name=...&version=...
  def encName = URLEncoder.encode(name, "UTF-8")
  def p
  if ( version ) {
    def encVer  = URLEncoder.encode(version, "UTF-8")
    p = request("GET", "/project/lookup?name=${encName}&version=${encVer}")
  } else{
    p = request("GET", "/project/lookup?name=${encName}")
  }
  return (p?.uuid ?: "").toString()
}

/* -------------------------------------------------------
 * Process the command line
 * ----------------------------------------------------- */
def handleOptions = { args ->
  def cliBuilder = "groovy.cli.commons.CliBuilder" as Class
  
  def cli = cliBuilder.newInstance(
    usage: 'check-parent.groovy [options]', 
    footer: 
'''function: Checks wether a Dependency-Track project has a parent project defined.
  mandatory environment variables   
     DT_API_URL      e.g. https://dtrack.example.com/api/v1
     DT_API_KEY      e.g. odt_12345678901234567890
'''
  )
  cli.h(longOpt: 'help', 'Print help page and exit')
  cli.l(longOpt: 'list', 'List all projects and exit')
  cli.p(longOpt: 'project', args: 1, 'Project name (PROJECT_NAME)')
  cli.v(longOpt: 'version', args: 1, 'Project version (PROJECT_VERSION)')
  cli.u(longOpt: 'uuid', args: 1, 'Project uuid (PROJECT_UUID)')

  def opts = cli.parse(args)
  if (!opts) fatal("Error parsing command line")
  if( opts.h ){
    cli.usage()
    System.exit(0)
  }

  def projectName = (opts.project ?: System.getenv("PROJECT_NAME") ?: "").trim()
  def projectVer  = (opts.version ?: System.getenv("PROJECT_VERSION") ?: "").trim()
  def projectUuid = (opts.uuid    ?: System.getenv("PROJECT_UUID") ?: "").trim()

  def listOnly = opts.l

  return [projectName, projectVer, projectUuid, listOnly]
}

/* -------------------------------------------------------
 * do the check
 * ----------------------------------------------------- */
def dTcheckForParent = { String projectName, String projectVer, String projectUuid ->
  def uuid = resolveProjectUuid( projectUuid, projectName, projectVer)
  if (!uuid) {
    fatal("Project not found (no UUID resolved).")
  }

  // DTrack endpoint: GET /api/v1/project/{uuid}
  def project = request("GET", "/project/${uuid}")

  // Depending on DTrack versions, the parent may appear as:
  // - project.parent (object) OR
  // - project.parentUuid (string) OR
  // - absent/null if not set
  def parentObj  = project?.parent
  def parentUuid = (project?.parentUuid ?: project?.parent?.uuid ?: "").toString()

  if (parentObj || parentUuid) {
    println("OK: Parent project is set. parentUuid=${parentUuid ?: '(embedded parent object)'}")
  } else {
    System.err.println("FAIL: No parent project set for project uuid=${uuid} (name=${project?.name}, version=${project?.version})")
  }
}

}
