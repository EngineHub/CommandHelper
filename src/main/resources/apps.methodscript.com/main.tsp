/*
 * This is the TypeSpec for apps.methodscript.com. If you make changes to this file, you must
 * regenerate the API code using the update-apps-api.cmd script at the root of the project (cross platform script).
 * To keep diffs to a minimum, changes to this file MUST be contained to its own pull request.
 * The TypeSpec reference can be found here https://typespec.io/docs
 * It is much easier to edit this file if you use the Visual Studio Code with the TypeSpec extension.
 */

import "@typespec/openapi";
import "@typespec/openapi3";
import "@typespec/http";

using TypeSpec.Http;
using TypeSpec.OpenAPI;

/**
 * This is the api for the apps.methodscript.com server. This provides
 * various public functionality for users of the MethodScript programming
 * language.
 */
@server("http://localhost:8080", "Test localhost server.")
@server("https://apps.methodscript.com", "The primary apps.methodscript.com server.")
// @externalDocs({
//   description: "Find out more about MethodScript",
//   url: "https://methodscript.com",
// })
@TypeSpec.OpenAPI.info({
  termsOfService: "apps.methodscript.com/terms",
  license: {
    name: "MIT",
    url: "https://opensource.org/licenses/MIT"
  },
})
@service({title: "MethodScript Apps", version: "1.0.0"})
namespace Service;

/**
 * This call should always be successful.
 */
model Ping200 {
  @header contentType: "text/plain";
  @body body: string;
}
/**
 * Service related calls.
 */
@tag("meta")
interface meta {
  /**
   * Lists the services that are available on this server. In general,
   * clients must load this page first to determine supported services, and
   * ensure that graceful fallbacks are provided if expected services are
   * missing.
   * @returns This call should always be successful.
   */
  @summary("List services on this server.")
  @route("/")
  @operationId("rootGet")
  @get root(): Array<string>;

  @summary("Pings the server to check its status.")
  @route("/ping")
  @operationId("pingGet")
  @get ping(): Ping200;
}

/**
 * If the payload was successfully forwarded.
 */
model TelemetryEvent200 {
  @statusCode _: 200;
}

/**
 * If the payload is malformed.
 */
@error
model TelemetryEvent400 {
  @statusCode _: 400
}

/**
 * If the key was rejected. This can happen for a number of reasons,
 * including things like server restart or key expiry. In any case,
 * simply re-request a new key and try again. It can also happen if the
 * server detects you're trying to do bad things.
*/
@error
model TelemetryEvent403 {
  @statusCode _: 403
}

/**
 * If the submission was successful, but Application Insights rejected
 * or otherwise unsuccessfully processed the request.
*/
@error
model TelemetryEvent502 {
  @statusCode _: 502
}

/**
 * Unless the client has been blocked, this call will always be successful.
 */
model Telemetry200 {
  @statusCode statusCode: 200;
  @header contentType: "text/plain";
  @body
  body: string
}

/**
 * If this client has been blocked, this will not return a key.
 */
@error
model Telemetry403 {
  @statusCode statusCode: 403;
}

/**
 * Telemetry related services.
 */
@tag("telemetry")
interface telemetry {

  @summary("Obtains a telemetry key, which is used for future requests.")
  @route("/telemetry")
  @operationId("telemetryGet")
  @get telemetry(): Telemetry200 | Telemetry403;

  /**
   * Submits a telemetry event, which is forwarded to the Application
   * Insights application.
   * @param key The previously obtained key.
   */
  @summary("Submits a telemetry event.")
  @route("/telemetry/{key}")
  @operationId("telemetryKeyPost")
  @post telemetryKey(
      key: string,
      @header contentType: "text/plain";
      @doc("""
      The telemetry event. The content should be text/plain, even though
      it will actually be json, as server merely forwards the content on to
      ApplicationInsights.
      """)
      @body body: string):
          TelemetryEvent200
          | TelemetryEvent400
          | TelemetryEvent403
          | TelemetryEvent502
}

/**
 * Metadata about an artifact.
 */
model BuildArtifact {
  /**
   * The artifact name.
   */
  artifact: string,
  /**
   * The build id, i.e. "build-123".
   */
  buildId: string,
  /**
   * The full artifact name, i.e. "build-123/artifact.jar".
   * This is what is used in the url to request the artifact.
   */
  name: string,
  /**
   * The datetime when the artifact was built. yyyy-MM-dd'T'HH:mm:ss.SSSZ
   */
  date: string,
  /**
   * The path for the url, i.e. "/builds/artifact/build-123%2Fartifact.jar".
   */
  link: string,
  /**
   * The full link to the artifact, including the host.
   */
  fullLink: url,
  /**
   * The git sha hash of the commit that was used to make this build.
   * May be an empty string, if the commit isn't known, never null.
   */
  sha: string,
  /**
   * The git commit message of the commit used to make this build.
   * May be an empty string, if the commit isn't known, never null.
   */
  commitDetails: string,
};

/**
 * If the specified build or artifact cannot be found.
 */
@error
model Build404 {
  @statusCode statusCode: 404;
}

model Build200 {
  @statusCode _: 200;
  @header contentType: "application/java-archive";
  @body
  ret: bytes
}

/**
 * Build system services.
 */
@tag("builds")
interface builds {
  @summary("Provides a list of artifact types available for download.")
  @route("/builds")
  @operationId("buildsGet")
  @get builds() : Array<string>;

  /**
   * Provides a list of builds of the specified artifact available for download.
   * Note that the order of the returned artifacts is arbitrary.
   */
  @summary("Provides a list of builds of the specified artifact available for download.")
  @route("/builds/{artifact}")
  @operationId("buildsArtifactGet")
  @get buildsArtifact(
    @doc("The artifact type to list builds for.")
    artifact: string
    ) : Array<BuildArtifact> | Build404;

  @summary("Downloads a specific build of the specified artifact type.")
  @route("/builds/{artifact}/{id}")
  @operationId("buildsArtifactIdGet")
  @get buildsArtifactId(
    @doc("The artifact type to download")
    artifact: string,
    @doc("The previously obtained id of the build you wish to download.")
    id: string,
  ) : Build200 | Build404;
}