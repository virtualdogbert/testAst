package com.security

/**
 * This is for adding roles at an object level for business rules
 */
class ObjectRole {
    String role        //The role to apply to the object
    Long   objectId    //The id of the object
    String domainName  //the domain name of the object
    Date   dateCreated
    Date   lastUpdated

    static constraints = {
        role inList: ['owner', 'editor', 'viewer']
        dateCreated nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        version false
        cache true
    }
}
