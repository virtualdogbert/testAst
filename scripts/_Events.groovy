

eventCompileStart = { kind ->
    compileAST(basedir, classesDirPath)
}


def compileAST(def srcBaseDir, def destDir) {
	ant.sequential {
		echo "Precompiling AST Transformations ..."
		echo "src ${srcBaseDir} ${destDir}"

		path id: "grails.compile.classpath", compileClasspath
		def classpathId = "grails.compile.classpath"
		mkdir dir: destDir

		groovyc(destdir: destDir,
				srcDir: "$srcBaseDir/src/groovy/com/virtualdogbert/ast",
				classpathref: classpathId,
				verbose: grailsSettings.verboseCompile,
				stacktrace: "yes",
				encoding: "UTF-8")

		echo "done precompiling AST Transformations"
	}
}
