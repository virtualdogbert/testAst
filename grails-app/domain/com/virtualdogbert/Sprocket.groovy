package com.virtualdogbert
/**
 * Just a test domain
 */
class Sprocket {
    String material
    Date   dateCreated
    Date   lastUpdated

    static constraints = {
        dateCreated nullable: true
        lastUpdated nullable: true
    }
}
