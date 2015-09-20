/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 * Some of the setup is derived from the fllowing grails plugings(Appache Licence)
 * https://github.com/groovy/groovy-core/blob/4993b10737881b2491c2daa01526fb15dd889ac5/src/main/org/codehaus/groovy/transform/NewifyASTTransformation.java
 * https://github.com/grails-plugins/grails-redis/tree/master/src/main/groovy/grails/plugins/redis
 */


import com.security.DomainRole
import com.security.Role
import com.security.User
import com.security.UserRole
import com.virtualdogbert.ast.Enforce

class BootStrap {
    def enforcerService
    def springSecurityService

    def init    = { servletContext ->
        def adminRole = new Role('ROLE_ADMIN').save(flush: true, failOnError: true)
        def userRole = new Role('ROLE_USER').save(flush: true, failOnError: true)

        def testUser = new User(username: 'me', password: 'password').save(flush: true, failOnError: true)

        UserRole.create testUser, adminRole, true
        Sprocket sprocket = new Sprocket(material: 'metal')
        sprocket.save(failOnError: true)
        springSecurityService.metaClass.currentUser = {-> testUser}

        enforcerService.changeDomainRole('owner', 'Sprocket', sprocket.id, testUser)

        enforcerService.enforce({ hasDomainRole('owner', 'Sprocket', sprocket.id, testUser) })

        enforcerService.enforce({ hasRole('ROLE_ADMIN', testUser) })

        enforcerService.removeDomainRole('Sprocket', sprocket.id, testUser)
        println DomainRole.list() == []

        println 'test method 1'
        testMethod()
        println 'test method 2'
        testMethod2()
        println 'test method 3'
        testMethod3()
        println 'test method 4'
        testMethod4()
        println 'test method 5'
        testMethod5()
        println 'test method 6'
        testMethod6(5)

        println '****************'
        println 'test 1'
        enforcerService.enforce({ true })
        println 'test 2'
        enforcerService.enforce({ true }, { println "not nice" })
        println 'test 3'
        enforcerService.enforce({ false }, { println "nice" })
        println 'test 4'
        enforcerService.enforce({ true }, { println "not nice" }, { println "nice" })
        println 'test 5'
        enforcerService.enforce({ false }, { println "nice" }, { println "not nice" })
    }
    def destroy = {
    }

    @Enforce({ true })
    def testMethod() {
        println 'nice'
    }

    @Enforce(value = { true }, failure = { println "not nice" })
    def testMethod2() {
        println 'nice'
    }

    @Enforce(value = { false }, failure = { println "nice" })
    def testMethod3() {

    }

    @Enforce(value = { true }, failure = { println "not nice" }, success = { println "nice" })
    def testMethod4() {

    }

    @Enforce(value = { false }, failure = { println "nice" }, success = { println "not nice" })
    def testMethod5() {

    }

    @Enforce({ number == 5 })
    def testMethod6(def number) {
        println 'nice'
    }
}
