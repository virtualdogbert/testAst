import com.virtualdogbert.ast.Enforce

class BootStrap {

    def init    = { servletContext ->
        testMethod()
        testMethod2()
        testMethod3()
        testMethod4()
    }
    def destroy = {
    }

    @Enforce({ true})
    def testMethod() {
        println 'nice'
    }

    @Enforce(value = { true }, failure = { println "nice" })
    def testMethod2() {

    }

    @Enforce(value = { true }, failure = { println "nice" }, success = { println "not nice" })
    def testMethod3() {

    }

    @Enforce(value = { false }, failure = { println "not nice" }, success = { println "nice" })
    def testMethod4() {

    }
}
