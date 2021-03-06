package ftl.args

import com.google.common.annotations.VisibleForTesting
import ftl.args.yml.Type
import ftl.ios.xctest.XcTestRunData
import ftl.ios.xctest.calculateXcTestRunData
import ftl.ios.xctest.common.XctestrunMethods
import ftl.run.exception.FlankConfigurationError

data class IosArgs(
    val commonArgs: CommonArgs,
    val xctestrunZip: String,
    val xctestrunFile: String,
    val xcodeVersion: String?,
    val testTargets: List<String>,
    val obfuscateDumpShards: Boolean,
    val additionalIpas: List<String>,
    val app: String,
    val testSpecialEntitlements: Boolean?
) : IArgs by commonArgs {

    override val useLegacyJUnitResult = true
    val xcTestRunData: XcTestRunData by lazy { calculateXcTestRunData() }

    companion object : IosArgsCompanion()

    override fun toString(): String {
        return """
IosArgs
    gcloud:
      results-bucket: $resultsBucket
      results-dir: $resultsDir
      record-video: $recordVideo
      timeout: $testTimeout
      async: $async
      client-details:${ArgsToString.mapToString(clientDetails)}
      network-profile: $networkProfile
      results-history-name: $resultsHistoryName
      # iOS gcloud
      test: $xctestrunZip
      xctestrun-file: $xctestrunFile
      xcode-version: $xcodeVersion
      device:${ArgsToString.objectsToString(devices)}
      num-flaky-test-attempts: $flakyTestAttempts
      directories-to-pull:${ArgsToString.listToString(directoriesToPull)}
      other-files:${ArgsToString.mapToString(otherFiles)}
      additional-ipas:${ArgsToString.listToString(additionalIpas)}
      scenario-numbers:${ArgsToString.listToString(scenarioNumbers)}
      type: ${type?.ymlName}
      app: $app
      test-special-entitlements: $testSpecialEntitlements
      fail-fast: $failFast

    flank:
      max-test-shards: $maxTestShards
      shard-time: $shardTime
      num-test-runs: $repeatTests
      smart-flank-gcs-path: $smartFlankGcsPath
      smart-flank-disable-upload: $smartFlankDisableUpload
      default-test-time: $defaultTestTime
      use-average-test-time-for-new-tests: $useAverageTestTimeForNewTests
      test-targets-always-run:${ArgsToString.listToString(testTargetsAlwaysRun)}
      files-to-download:${ArgsToString.listToString(filesToDownload)}
      keep-file-path: $keepFilePath
      full-junit-result: $fullJUnitResult
      # iOS flank
      test-targets:${ArgsToString.listToString(testTargets)}
      disable-sharding: $disableSharding
      project: $project
      local-result-dir: $localResultDir
      run-timeout: $runTimeout
      ignore-failed-tests: $ignoreFailedTests
      output-style: ${outputStyle.name.toLowerCase()}
      disable-results-upload: $disableResultsUpload
      default-class-test-time: $defaultClassTestTime
        """.trimIndent()
    }
}

val IosArgs.isXcTest: Boolean
    get() = type == Type.XCTEST

@VisibleForTesting
internal fun filterTests(
    validTestMethods: XctestrunMethods,
    testTargets: List<String>
): XctestrunMethods =
    if (testTargets.isEmpty()) validTestMethods
    else testTargets.map { testTarget ->
        try {
            testTarget.toRegex()
        } catch (e: Exception) {
            throw FlankConfigurationError("Invalid regex: $testTarget", e)
        }
    }.let { testTargetRgx ->
        validTestMethods.mapValues { (_, tests) ->
            tests.filter { test ->
                testTargetRgx.any { regex -> test.matches(regex) }
            }
        }
    }
