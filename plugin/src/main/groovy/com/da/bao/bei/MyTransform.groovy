package com.da.bao.bei

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils

class MyTransform extends Transform {
    @Override
    String getName() {
        return "MyTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        HashSet<QualifiedContent.ContentType> hashSet = new HashSet<>()
        hashSet.add(QualifiedContent.DefaultContentType.CLASSES)
        return hashSet
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        HashSet<QualifiedContent.Scope> hashSet = new HashSet<>()
        hashSet.add(QualifiedContent.Scope.PROJECT)
        return hashSet
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        printCopyRight()


        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        // Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，要分开遍历
        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                // 处理jar
                processJarInput(jarInput, outputProvider)
            }

            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 处理源码文件
                processDirectoryInput(directoryInput, outputProvider)
            }
        }

    }

    void processJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        // 重命名输出文件（同目录copyFile会冲突）
        def jarName = jarInput.name
        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }

        File dest = outputProvider.getContentLocation(
                jarName + md5Name,
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR
        )

        // TODO do some transform

        // 将 input 的目录复制到 output 指定目录
        FileUtils.copyFile(jarInput.getFile(), dest)
    }

    void processDirectoryInput(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(
                directoryInput.getName(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY
        )

        // TODO do some transform

        // 将 input 的目录复制到 output 指定目录
        FileUtils.copyDirectory(directoryInput.getFile(), dest)
    }

    static void printCopyRight() {
        println()
        println("******************************************************************************")
        println("******                                                                  ******")
        println("******                       欢迎使用 大包子 编译插件                       ******")
        println("******                                                                  ******")
        println("******************************************************************************")
        println()
    }

}
