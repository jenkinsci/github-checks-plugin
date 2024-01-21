# GitHub Checks API Plugin

[![Join the chat at https://gitter.im/jenkinsci/github-checks-api](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/jenkinsci/github-checks-api)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/XiongKezhi/checks-api-plugin/issues)
[![Jenkins](https://ci.jenkins.io/job/Plugins/job/github-checks-plugin/job/master/badge/icon?subject=Jenkins%20CI)](https://ci.jenkins.io/job/Plugins/job/github-checks-plugin/job/master/)
[![GitHub Actions](https://github.com/jenkinsci/github-checks-plugin/workflows/CI/badge.svg?branch=master)](https://github.com/jenkinsci/github-checks-plugin/actions)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/2c7fa67496a743778ca60cc9604212d2)](https://www.codacy.com/gh/jenkinsci/github-checks-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/github-checks-plugin&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/jenkinsci/github-checks-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/jenkinsci/github-checks-plugin)

![GitHub Checks Plugin Cover](docs/images/github-checks-plugin-cover.png)

This plugin publishes checks to GitHub through [GitHub Checks API](https://docs.github.com/en/rest/reference/checks#runs).
It implements the extension points defined in [Checks API Plugin](https://github.com/jenkinsci/checks-api-plugin). 

This plugin has been installed, along with the [General API Plugin](https://github.com/jenkinsci/checks-api-plugin) on [ci.jenkins.io](https://ci.jenkins.io/Plugins) to help maintain over 1500 Jenkins plugins. You can take a look at the [action](https://github.com/jenkinsci/github-checks-plugin/runs/1025018883) for this repository or other plugin repositories under [Jenkins organization](https://github.com/jenkinsci) for the results.

- [Features](#features)
  - [Build Status Check](#build-status-check)
  - [Rerun Failed Build](#rerun-failed-build)
 - [Contributing](#contributing)
 - [Acknowledgements](#acknowledgements)
 - [LICENSE](#license)

## Getting started

Only GitHub Apps with proper permissions can publish checks, this [guide](https://github.com/jenkinsci/github-branch-source-plugin/blob/master/docs/github-app.adoc) helps you authenticate your Jenkins instance as a GitHub App. 
The permission *read/write* on *Checks* needs to be granted in addition to the ones already mentioned in the guide.

## Features

### Build Status Check

![GitHub Status](docs/images/github-status.png)

This plugin implements [the status checks feature from Checks API Plugin](https://github.com/jenkinsci/checks-api-plugin#build-status-check) to publish statuses (pending, in progress, and completed) to GitHub.

You can customize it by configuring the "Status Checks Properties" behavior for your GitHub SCM Source or Git SCM projects:

![Status Checks Properties](docs/images/status-checks-properties.png)

*Note: If you are using [GitHub Branch Source Plugin](https://github.com/jenkinsci/github-branch-source-plugin), it will also send status notifications to GitHub through [Status API](https://docs.github.com/en/rest/reference/repos#statuses). You can disable those notifications by configuring Skip GitHub Branch Source notifications option.*

### Rerun Failed Build

![Failed Checks](docs/images/failed-checks.png)

If your Jenkins build failed, a failed check will be published here.
A "Re-run" button will be added automatically by GitHub, by clicking it, you can schedule a new build for the **last** commit of this branch.

### Configuration

![Checks Config](docs/images/github-checks-config.png)

- *Verbose Console Log* : check for verbose build console log, the default is false

## Contributing

Refer to our [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## Acknowledgements

This plugin was started as a [Google Summer of Code 2020 project](https://www.jenkins.io/projects/gsoc/2020/projects/github-checks/), special thanks to the support from [Jenkins GSoC SIG](https://www.jenkins.io/sigs/gsoc/) and the entire community.

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE)
