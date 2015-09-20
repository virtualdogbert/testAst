package com.virtualdogbert.security

import com.security.DomainRole
import com.security.Role
import com.security.User
import com.security.UserRole
import com.virtualdogbert.Sprocket
import com.virtualdogbert.ast.Enforce
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */

@Mock([Role, User, UserRole, Sprocket, DomainRole])
@TestFor(EnforcerService)
class EnforcerServiceSpec extends Specification {

    def testUser

    def setup() {
        def adminRole = new Role('ROLE_ADMIN').save(flush: true, failOnError: true)
        def userRole = new Role('ROLE_USER').save(flush: true, failOnError: true)
        testUser = new User(username: 'me', password: 'password').save(flush: true, failOnError: true)

        UserRole.create testUser, adminRole, true
        UserRole.create testUser, userRole, true

        SpringSecurityService.metaClass.currentUser = {-> testUser }

        service.grailsApplication = new DefaultGrailsApplication()
        service.grailsApplication.config = [enforcer: [enabled: true]]//This enables Enforcer for unit tests because it is turned off by default.
    }

    //Testing EnforcerService
    void 'test enforce { true }'() {
        when:
            service.enforce({ true })
        then:
            true
    }

    void 'test enforce { false }'() {
        when:
            service.enforce({ false })
        then:
            Exception e = thrown()
            e.message == 'Access Denied'
    }

    void 'test enforce { true }, { throw Exception("not nice") }'() {
        when:
            service.enforce({ true }, { throw Exception("not nice") })
        then:
            true
    }

    void 'test enforce { false }, { throw Exception("nice") }'() {
        when:
            service.enforce({ false }, { throw Exception("nice") })
        then:
            thrown Exception
    }

    void 'test enforce { true }, { throw Exception("not nice")}, { println "nice" }'() {
        when:
            service.enforce({ true }, { throw Exception("not nice") }, { println "nice" })
        then:
            true
    }

    void 'test enforce { false }, { throw Exception("nice") }, { throw Exception("not nice") }'() {
        when:
            service.enforce({ false }, { throw Exception("nice") }, { println("not nice") })
        then:
            thrown Exception
    }

    //Testing DomainRoleTrait
    void 'test enforce hasDomainRole(\'owner\', \'Sprocket\', sprocket.id, testUser)'() {
        when:
            Sprocket sprocket = new Sprocket(material: 'metal')
            sprocket.save(failOnError: true)
            service.changeDomainRole('owner', 'Sprocket', sprocket.id, testUser)
            service.enforce({ hasDomainRole('owner', 'Sprocket', sprocket.id, testUser) })
        then:
            true
    }

    void 'test fail enforce hasDomainRole(\'owner\', \'Sprocket\', sprocket.id, testUser)'() {
        when:
            Sprocket sprocket = new Sprocket(material: 'metal')
            sprocket.save(failOnError: true)
            service.enforce({ hasDomainRole('owner', 'Sprocket', sprocket.id, testUser) })
        then:
            thrown Exception
    }

     //Testing RoleTrait
    void 'test enforce hasRole(\'ROLE_ADMIN\', testUser)'(){
        when:
            service.enforce({ hasRole('ROLE_ADMIN', testUser) })
        then:
            true
    }

    void 'test enforce hasRole(\'ROLE_USER\', testUser)'(){
        when:
            service.enforce({ hasRole('ROLE_USER', testUser) })
        then:
            true
    }

    void 'test enforce hasRole(\'ROLE_ADMIN\', testUser)'(){
        when:
            service.enforce({ hasRole('ROLE_SUPER_USER', testUser) })
        then:
            thrown Exception
    }

    //Testing Enforce AST transform
    void 'test method 1'() {
        when:
            method1()
        then:
            true
    }

    void 'test method 2'() {
        when:
            method2()
        then:
            true
    }

    void 'test method 3'() {
        when:
            method3()
        then:
            thrown Exception
    }

    void 'test method 4'() {
        when:
            method4()
        then:
            true
    }

    void 'test method 5'() {
        when:
            method5()
        then:
            thrown Exception
    }

    void 'test method 6'() {
        when:
            method6(5)
        then:
            true
    }


    //Test methods for testing Enforce AST transform
    @Enforce({ true })
    def method1() {
        println 'nice'
    }

    @Enforce(value = { true }, failure = { throw Exception("not nice") })
    def method2() {
        println 'nice'
    }

    @Enforce(value = { false }, failure = { throw Exception("nice") })
    def method3() {
        throw Exception("this shouldn't happen on method3")
    }

    @Enforce(value = { true }, failure = { throw Exception("not nice") }, success = { println "nice" })
    def method4() {

    }

    @Enforce(value = { false }, failure = { throw Exception("nice") }, success = { println "not nice" })
    def method5() {
        throw Exception("this shouldn't happen on method5")
    }

    @Enforce({ number == 5 })
    def method6(def number) {
        println 'nice'
    }
}
