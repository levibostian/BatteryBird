#!/usr/bin/env -S deno run --quiet --allow-all --no-lock

import $ from "jsr:@david/dax"
import { getDeployStepInput } from "jsr:@levibostian/decaf-sdk"
import { walk } from "jsr:@std/fs/walk"
import { resolve } from "jsr:@std/path"

const input = getDeployStepInput()

const nowInSeconds = Math.floor(Date.now() / 1000).toString()

Deno.env.set("ANDROID_APP_BUILD_NUMBER", nowInSeconds.toString())
Deno.env.set("ANDROID_APP_VERSION_NAME", input.nextVersionName)

await $`echo "android build number: $ANDROID_APP_BUILD_NUMBER"`
await $`echo "android version name: $ANDROID_APP_VERSION_NAME"`
await $`./gradlew bundleRelease`.printCommand()

// Find .aab file in app/ directory
let aabFilePath: string | null = null
for await (const entry of walk("app/build/outputs/bundle", { exts: [".aab"] })) {
  if (entry.isFile) {
    aabFilePath = resolve(entry.path)
    break // Get the first .aab file found
  }
}

if (aabFilePath) {
  console.log(`Found AAB file: ${aabFilePath}`)
} else {
  console.log("No .aab file found in app/ directory")
  Deno.exit(1)
}

const argsToUploadToGooglePlay = [
  `--aab-file`,
  aabFilePath,
  `--package-name`,
  `earth.levi.bluetoothbattery`,
  `--service-account`,
  Deno.env.get("GOOGLE_PLAY_SERVICE_ACCOUNT_FILE_PATH")!,
  `--track`,
  `production`,
]

if (input.testMode) {
  console.log("Running in test mode, skipping uploading to Google Play.")
  console.log(`Command to upload to Google Play: ./upload-android-app-to-google-play ${argsToUploadToGooglePlay.join(" ")}`)
} else {
  // https://github.com/dsherret/dax#providing-arguments-to-a-command
  await $`./upload-android-app-to-google-play ${argsToUploadToGooglePlay}`.printCommand()
}

const argsToCreateGithubRelease = [
  `release`,
  `create`,
  input.nextVersionName,
  `--generate-notes`,
  `--latest`,
  `--target`,
  'main',
]

if (input.testMode) {
  console.log("Running in test mode, skipping creating GitHub release.")
  console.log(`Command to create GitHub release: gh ${argsToCreateGithubRelease.join(" ")}`)
} else {
  await $`gh ${argsToCreateGithubRelease}`.printCommand()
}