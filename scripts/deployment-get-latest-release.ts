#!/usr/bin/env -S deno run --quiet --allow-all --no-lock

import { getLatestReleaseStepInput, setLatestReleaseStepOutput } from "jsr:@levibostian/decaf-sdk"
import $ from "jsr:@david/dax"

const input = getLatestReleaseStepInput()

const latestReleaseJsonText = await $`gh release list --exclude-drafts --order desc --json name,isLatest,isPrerelease,tagName --jq '.[0]'`.text()

if (!latestReleaseJsonText) {
  // no releases found. meaning we have not yet made a release.
  // exit early without setting output. decaf will make a first time release.
  Deno.exit(0)
}
const latestRelease: {
  name: string
  tagName: string
} = JSON.parse(latestReleaseJsonText)

console.log(`latest release found: ${latestRelease.name} (${latestRelease.tagName})`)

const commitMatchingRelease = input.gitCommitsCurrentBranch.find((commit) => {
  return commit.tags?.includes(latestRelease.tagName)
})!

console.log(`commit matching release found: ${commitMatchingRelease.title} (${commitMatchingRelease.sha})`)

setLatestReleaseStepOutput({
  versionName: latestRelease.name,
  commitSha: commitMatchingRelease.sha,
})