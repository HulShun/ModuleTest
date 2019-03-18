package com.fhs.module.support.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.logging.Logger

public class PluginImpl implements Plugin<Project> {

    static final PLUGIN_NAME = "module.run"
    static final EX_NAME = "moduleTest"
    Project project
    static Logger mLogger;

    @Override
    void apply(Project project) {
        mLogger = project.logger
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new RuntimeException(
                    "'com.android.application' plugin must be applied", null)
        }
        this.project = project

        project.extensions.create(EX_NAME, PluginExtension)
        project.afterEvaluate {
            PluginExtension ext = project.extensions.getByName(EX_NAME)
            if (!ext.test) {
                getLogger().info("[${PLUGIN_NAME}] Set moduleTest { test true } to start plugin")
                return
            }
            getLogger().info("[${PLUGIN_NAME}] plugin is running")
            if (ext.moduleName == null || ext.moduleName == "") {
                println("moduleName is null")
                return
            }
            addBuildConfigField(ext)
            replaceDependency(ext)
        }
    }


    private void addBuildConfigField(PluginExtension ext) {
        //写入BuildConfig文件中 当前debug 的module 以及启动的Activity
        getLogger().info("[${PLUGIN_NAME}]Write to BuildConfig.DEBUG_MODULE:${ext.moduleName}")
        project.android.defaultConfig.buildConfigField("String", "DEBUG_MODULE", "\"${ext.moduleName}\"")

        getLogger().info("[${PLUGIN_NAME}]Write to BuildConfig.DEBUG_MODULE_MAIN_ACTIVITY:${ext.mainActivity}")
        project.android.defaultConfig.buildConfigField("String", "DEBUG_MODULE_MAIN_ACTIVITY", "\"${ext.mainActivity}\"")
    }

    private void replaceDependency(PluginExtension ext) {
        project.configurations.all { Configuration con ->
            if (con.dependencies.size() == 0) {
                return
            }

            //app module 根据当前测试的module来移除其他module依赖
            con.dependencies.all { Dependency dependency ->
                if (dependency instanceof ProjectDependency) {
                    String name = dependency.getDependencyProject().getPath().replace(":", "")
                    if (name != ext.moduleName && (ext.excludes == null || !ext.excludes.contains(name))) {
                        getLogger().info("[${PLUGIN_NAME}]Remove ProjectDependency ${dependency.getName()}")
                        con.dependencies.remove(dependency)
                    }
                }
            }
        }
    }

    static getLogger() {
        return mLogger
    }
}