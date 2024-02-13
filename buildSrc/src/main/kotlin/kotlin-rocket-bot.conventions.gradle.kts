import app.cash.licensee.UnusedAction

group = "at.rueckgr.kotlin.rocketbot"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

plugins {
    id("app.cash.licensee")
}

licensee {
    unusedAction(UnusedAction.IGNORE)

    allow("MIT")
    allow("Apache-2.0")

    allowUrl("http://www.gnu.org/licenses/lgpl-2.1.html")

    // Unicode, Inc. License Agreement for Data Files and Software (#Unicode)
    allowUrl("https://raw.githubusercontent.com/unicode-org/icu/main/icu4c/LICENSE")

    // BSD-2 Clause
    allowUrl("https://jdbc.postgresql.org/about/license.html")

    // BSD-3 Clause
    allowUrl("http://www.eclipse.org/org/documents/edl-v10.php")
    allowUrl("http://www.abego-software.de/legal/apl-v10.html")
    allowUrl("http://antlr.org/license.html")
    allowUrl("http://www.antlr.org/license.html")
    allowUrl("https://www.antlr.org/license.html")
    allowUrl("https://raw.githubusercontent.com/ThreeTen/threeten-extra/main/LICENSE.txt")

    // GPL-2.0-only
    allowUrl("https://oss.oracle.com/licenses/CDDL+GPL-1.1")

    // GPL-3.0-only
    ignoreDependencies("at.rueckgr.kotlin.rocketbot")
}

