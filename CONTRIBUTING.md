# Contributing

Hi there! We're delighted that you'd like to contribute to this project.
It has been generous collaboration from many people all over the world
that has made it possible so far, and your help is key to keeping it
great.

First of all, we would *love* to hear from you! We have no way of
knowing who has discovered, explored, downloaded and tried the Beat
Link integration for Max. So if you have, please write a quick note on
the [Beat Link Trigger Zulip
channel](https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger)
to let us know! Even if it is only to explain why it didn’t
quite work for you.


Contributions to this project are [released][contributions-released]
to the public under the [project's open source license](LICENSE.md).

This project adheres to the
[Contributor Covenant Code of Conduct][covenant].
By participating, you are expected to uphold this code.

## Getting started

Before you can start contributing to Beat Link Max, you'll need to set
up your environment first. Fork and clone the repo and install
[Maven][maven] to install the project dependencies and manage builds.
We find [IntelliJ IDEA][idea] (even the free Community Edition) an
incredibly productive environment for Java work, but use whatever IDE
or editor works best for you (we always have [GNU Emacs][emacs] open
too).

Compilation also relies on the file `max.jar` which ships with Max,
but is not published to Maven Central or anywhere else, and we can't
redistribute it ourselves. So you will need to install a copy in your
local Maven repository, under the groupId `local`, artifactId
`max`, and version `0.9`.

1. First, find your copy of the file. In the macOS version of Max, you
   need to look inside the application bundle itself. Right-click on
   the application in the Finder, and choose **Show Package
   Contents**. This will open it as a folder, and you can find the
   file at
   `Max.app/Contents/Resources/C74/packages/max-mxj/java-classes/lib/max.jar`.
   The mxj tutorial inside Max says that on Windows machines the file
   can be found at `\Program Files\Common Files\Cycling
   '74\java\lib\max.jar`, but I can't verify if this information is
   current.

2. Open a terminal and `cd` into the same directory that contains
   `max.jar`, and issue the following command to install it in the
   right place in your local Maven repository:

       mvn install:install-file -Dfile=max.jar  -DgroupId=local -DartifactId=max -Dversion=0.9 -Dpackaging=jar

Once this is all in place, you can run `mvn package`. That will
create `target/beat-link-max-0.1.0-SNAPSHOT-jar-with-dependencies.jar`
which is the compiled code, including all the libraries it needs.
(The version number will vary depending on the version you are
working on.)

Copy that to `MaxPackage/beat-link-max/java-classes/lib/beat-link-max.jar`,
and then copy `MaxPackage/beat-link-max`, which is the Max package, to the Max
Packages folder as described in the Installation section of the
[Read Me](README.md#Installation).

> :bulb: You can also create an alias of the
> `MaxPackage/beat-link-max` folder inside your Max Packages folder to
> avoid having to copy it from the repository every time you want to
> use an updated version in Max. The compiled jar file is set up to be
> ignored by git. This makes working from source much more convenient.

When sharing the package online, Zip up the `MaxPackage/beat-link-max`
folder, and share that. In addition to the code itself, it includes the
description, documentation, help files, and so on, that teach people how
to use it.

For testing you are going to want some Pro DJ Link hardware on your
network, and a wired network is necessary for realistic performance.
If you will be trying to analyze the protocols, you will probably want
to invest in an inexpensive managed switch, to let you span (mirror)
ports, so you can listen in on the traffic players and mixers send
between themselves. The [Netgear GS105Ev2][switch] has worked great
for us.

## Giving back

Once you have something working you’d like to share, you can open a
[pull request][pulls]. It’d probably be a good idea before you get
to that point to get some feedback on what you’ve been doing through
the [Beat Link Trigger Zulip channel](https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger).

Or if you simply have an idea, or something that you wish worked
differently, feel free to discuss it on the [Beat Link Trigger Zulip
channel](https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger),
and if directed to do so by the community there, open an
[issue][issues].

## Maintainers

Beat Link is primarily maintained by [@brunchboy][brunchboy].

## License

<a href="https://deepsymmetry.org"><img align="right" alt="Deep Symmetry"
 src="assets/DS-logo-github.png" width="250" height="150"></a>

Copyright © 2024 [Deep Symmetry, LLC](http://deepsymmetry.org)

Distributed under the [Eclipse Public License
2.0](https://opensource.org/licenses/EPL-2.0). By using this software
in any fashion, you are agreeing to be bound by the terms of this
license. You must not remove this notice, or any other, from this
software. A copy of the license can be found in
[LICENSE.md](LICENSE.md) within this project.


[contributions-released]: https://help.github.com/articles/github-terms-of-service/#6-contributions-under-repository-license
[covenant]: http://contributor-covenant.org/
[maven]: https://maven.apache.org
[idea]: https://www.jetbrains.com/idea/
[emacs]: https://www.gnu.org/software/emacs/
[switch]: https://smile.amazon.com/gp/product/B00HGLVZLY/
[pulls]: https://github.com/Deep-Symmetry/beat-link/pulls
[issues]: https://github.com/Deep-Symmetry/beat-link/issues
[brunchboy]: https://github.com/brunchboy
