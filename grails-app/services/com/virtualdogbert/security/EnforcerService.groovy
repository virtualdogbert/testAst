package com.virtualdogbert.security

import grails.transaction.Transactional
import grails.util.Environment

@Transactional
class EnforcerService implements RoleTrait,ObjectRoleTrait{

    def grailsApplication

    def enforce(Closure predicate, Closure failure = { throw new Exception("Access Denied") }, Closure success = { return true }) {

        if (Environment.current != "TEST" || grailsApplication.config.enforcer.enabled) {
            predicate.delegate = this
            failure.delegate = this
            success.delegate = this

            if (predicate()) {
                success()
            } else {
                failure()
            }
        }

    }
}
