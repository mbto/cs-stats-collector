rootProject.name = "cs-stats-collector"

files("modules", "system").forEach { dirs ->
    dirs.listFiles()?.forEach {
        include(it.name)
        project(":${it.name}").projectDir = it
    }
}
