rootProject.name = "cs-stats-collector"

files("modules", "system", "web").forEach { dirs ->
    dirs.listFiles()?.forEach {
        include(it.name)
        project(":${it.name}").projectDir = it
    }
}
