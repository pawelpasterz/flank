import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

/*
https://github.com/bitrise-io/addons-test-frontend
https://github.com/bitrise-io/addons-firebase-testlab/


default local address is http://localhost:5001
https://github.com/bitrise-io/addons-test-frontend/blob/master/src/environments/environment.mock.ts

junit parsing logic:
https://github.com/bitrise-io/addons-test-frontend/blob/747f9f3612ea0c130689ea90183ba9b4f6f46c8f/src/app/services/provider/provider.service.ts#L108

backend API
https://github.com/bitrise-io/addons-test-frontend/blob/747f9f3612ea0c130689ea90183ba9b4f6f46c8f/src/app/services/backend/backend.service.ts#L24

getApp = (): Observable<AppResult> =>
    this.httpClient.get(`${environment.apiRootUrl}/api/app`).pipe(
      map((appResponse: any) => {
        return {
          slug: appResponse.app_slug,
          name: appResponse.app_title,
        };
      })
    )

  deserializeFirebaseTestlabTestCases -> parses XML

   getReportDetails = (buildSlug: string, testReport: TestReport): Observable<TestReportResult> =>
    this.httpClient.get(`${environment.apiRootUrl}/api/builds/${buildSlug}/test_reports/${testReport.id}`)
https://github.com/bitrise-io/addons-test-frontend/blob/master/src/app/services/backend/backend.service.ts#L69


---

Unit test example:
https://app.bitrise.io/build/9b359aa25b355e3b
https://addons-testing.bitrise.io/builds/9b359aa25b355e3b/summary?status=failed

UI test example:
https://app.bitrise.io/build/3613c570728bedae
https://addons-testing.bitrise.io/builds/3613c570728bedae/summary?status=failed


https://devcenter.bitrise.io/testing/test-reports/
*/

data class GetAppResponse(
    val app_slug: String,
    val app_title: String
)


// [{"id":"ftl","name":"Firebase TestLab"}]
data class TestReport(
    val id: String,
    val name: String
)
/*

[
  {
    "device_name": "iPhone 8",
    "api_level": "iOS 12.0",
    "status": "complete",
    "test_results": [
      {
        "total": 3
      },
    ],
    "outcome": "success",
    "orientation": "portrait",
    "locale": "English",
    "step_id": "bs.601d90502a92e348",
    "output_urls": {
      "video_url": "",
      "test_suite_xml_url": "test_result_0.xml",
      "log_urls": [],
      "asset_urls": {}
    },
    "test_type": "instrumentation",
    "step_duration_in_seconds": 295
  }
]
*/

data class TestResult(
    val total: Int
)

data class OutputURLs(
    val video_url: String,
    val test_suite_xml_url: String,
    val log_urls: List<Object>,
    val asset_urls: Object
)

data class FTLResponse(
  val device_name: String,
  val api_level: String,
  val status: String,
  val test_results: List<TestResult>,
  val outcome: String,
  val orientation: String,
  val locale: String,
  val step_id: String,
  val output_urls: OutputURLs,
  val test_type: String,
  val step_duration_in_seconds: Int
)

fun main() {
    val server = embeddedServer(Netty, port = 5001) {

        install(CORS) {
            anyHost()
        }
        install(ContentNegotiation) {
            gson {
            }
        }
        routing {
            get("/api/app") {
                call.respond(
                    GetAppResponse(
                        app_slug = "flank slug",
                        app_title = "flank name"
                    )
                )
            }

            get("/api/builds/{buildSlug}/test_reports") {
                call.respond(
                        listOf(
                            // [{"id":"ftl","name":"Firebase TestLab"}]
                            TestReport(id = "ftl",
                                name = "Firebase TestLab")
                        )
                )
            }

            // https://addons-testing.bitrise.io/api/builds/9b359aa25b355e3b/test_reports/c42975e8-a9b2-410a-8fed-d669dd2a2341
            get("/api/builds/{buildSlug}/test_reports/{reportId}") {
                // todo: example body with JUnit XML
                call.respond("")
            }


            // https://addons-testing.bitrise.io/api/builds/3613c570728bedae/test_reports/ftl
            get("/api/builds/{buildSlug}/test_reports/ftl") {

                // expects test_suites value to contain array of JUnitXMLTestSuiteResponse
                // https://github.com/bitrise-io/addons-test-frontend/blob/747f9f3612ea0c130689ea90183ba9b4f6f46c8f/src/app/services/provider/provider.service.ts#L59

                call.respond(FTLResponse(
                    device_name = "iPhone 8",
                    api_level = "iOS 12.0",
                    status = "complete",
                    test_results = listOf(TestResult(total = 3)),
                    outcome = "success",
                    orientation = "portrait",
                    locale = "English",
                    step_id = "123",
                    output_urls = OutputURLs(
                        video_url = "",
                        test_suite_xml_url = "test_result_9.xml",
                        log_urls = listOf(),
                        asset_urls = Object()
                    ),
                    test_type = "instrumentation",
                    step_duration_in_seconds = 123
                ))
            }
        }
    }
    server.start(wait = true)
}
