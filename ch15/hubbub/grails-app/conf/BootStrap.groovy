import com.grailsinaction.*
import grails.converters.*
import java.text.SimpleDateFormat
import static java.util.Calendar.*

class BootStrap {

    def init = { servletContext ->
        def dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
        JSON.registerObjectMarshaller(Post) { Post p ->
            return [ published: dateFormatter.format(p.dateCreated),
                    content: p.content,
                    user: p.user.profile?.fullName,
                    tags: p.tags.collect { it.name } ]
        }

        XML.registerObjectMarshaller(Post) { Post p, converter ->
            converter.build {
                post(published: dateFormatter.format(p.dateCreated)) {
                    content p.content
                    user p.user.profile?.fullName
                    tags {
                        for (t in p.tags) {
                            tag t.name
                        }
                    }
                }
            }
        }

        /*
        // Register an XML marshaller that returns a map rather than uses builder syntax.
        XML.registerObjectMarshaller(Post) { Post p ->
            return [ published: dateFormatter.format(p.dateCreated),
                    content: p.content,
                    user: p.user.profile.fullName,
                    tags: p.tags.collect { it.name } ]
        }
        */

        environments {
            development {
                if (!Post.count()) createSampleData()
            }
            test {
                if (!Post.count()) createSampleData()
            }
        }

        // Admin user is required for all environments
        createAdminUserIfRequired()
    }

    private createSampleData() {

        def now = new Date()
        def graeme = new User(
                loginId: "graeme",
                password: "willow",
                profile: new Profile(fullName: "Graeme Rocher", email: "graeme@nowhere.net"),
                dateCreated: now).save(failOnError: true)
        def jeff = new User(
                loginId: "jeff",
                password: "sheldon",
                profile: new Profile(fullName: "Jeff Brown", email: "jeff@nowhere.net"),
                dateCreated: now).save(failOnError: true)
        def burt = new User(
                loginId: "burt",
                password: "mandible",
                profile: new Profile(fullName: "Burt Beckwith", email: "burt@nowhere.net"),
                dateCreated: now).save(failOnError: true)
        def frankie = new User(
                loginId: "frankie",
                password: "testing",
                profile: new Profile(fullName: "Frankie Goes to Hollywood", email: "frankie@nowhere.net"),
                dateCreated: now).save(failOnError: true)
        def sara = new User(
                loginId: "sara",
                password: "crikey",
                profile: new Profile(fullName: "Sara Miles", email: "sara@nowhere.net"),
                dateCreated: now - 2).save(failOnError: true)
        def phil = new User(
                loginId: "phil",
                password: "thomas",
                profile: new Profile(fullName: "Phil Potts", email: "phil@nowhere.net"),
                dateCreated: now)
        def dillon = new User(loginId: "dillon",
                password: "crikey",
                profile: new Profile(fullName: "Dillon Jessop", email: "dillon@nowhere.net"),
                dateCreated: now - 2).save(failOnError: true)

        phil.addToFollowing(frankie)
        phil.addToFollowing(sara)
        phil.save(failOnError: true)

        phil.addToPosts(content: "Very first post")
        phil.addToPosts(content: "Second post")
        phil.addToPosts(content: "Time for a BBQ!")
        phil.addToPosts(content: "Writing a very very long book")
        phil.addToPosts(content: "Tap dancing")
        phil.addToPosts(content: "Pilates is killing me")
        phil.save()

        sara.addToPosts(content: "My first post")
        sara.addToPosts(content: "Second post")
        sara.addToPosts(content: "Time for a BBQ!")
        sara.addToPosts(content: "Writing a very very long book")
        sara.addToPosts(content: "Tap dancing")
        sara.addToPosts(content: "Pilates is killing me")
        sara.save(flush: true)

        dillon.addToPosts(content: "Pilates is killing me as well")
        dillon.save(flush: true)

        // We have to update the 'dateCreated' field after the initial save to
        // work around Grails' auto-timestamping feature. Note that this trick
        // won't work for the 'lastUpdated' field.
        def postsAsList = phil.posts as List
        postsAsList[0].addToTags(user: phil, name: "groovy")
        postsAsList[0].addToTags(user: phil, name: "grails")
        postsAsList[0].dateCreated = now.updated(YEAR: 2004, MONTH: MAY)

        postsAsList[1].addToTags(user: phil, name: "grails")
        postsAsList[1].addToTags(user: phil, name: "ramblings")
        postsAsList[1].addToTags(user: phil, name: "second")
        postsAsList[1].dateCreated = now.updated(YEAR: 2007, MONTH: FEBRUARY, DAY_OF_MONTH: 13)

        postsAsList[2].addToTags(user: phil, name: "groovy")
        postsAsList[2].addToTags(user: phil, name: "bbq")
        postsAsList[2].dateCreated = now.updated(YEAR: 2009, MONTH: OCTOBER)

        postsAsList[3].addToTags(user: phil, name: "groovy")
        postsAsList[3].dateCreated = now.updated(YEAR: 2011, MONTH: MAY, DAY_OF_MONTH: 1)

        postsAsList[4].dateCreated = now.updated(YEAR: 2011, MONTH: DECEMBER, DAY_OF_MONTH: 4)
        postsAsList[5].dateCreated = now.updated(YEAR: 2012, DAY_OF_MONTH: 10)
        phil.save(failOnError: true)

        postsAsList = sara.posts as List
        postsAsList[0].dateCreated = now.updated(YEAR: 2007, MONTH: MAY)
        postsAsList[1].dateCreated = now.updated(YEAR: 2008, MONTH: MARCH, DAY_OF_MONTH: 13)
        postsAsList[2].dateCreated = now.updated(YEAR: 2008, MONTH: APRIL, DAY_OF_MONTH: 24)
        postsAsList[3].dateCreated = now.updated(YEAR: 2011, MONTH: NOVEMBER, DAY_OF_MONTH: 8)
        postsAsList[4].dateCreated = now.updated(YEAR: 2011, MONTH: DECEMBER, DAY_OF_MONTH: 4)
        postsAsList[5].dateCreated = now.updated(YEAR: 2012, MONTH: AUGUST, DAY_OF_MONTH: 1)
        
        sara.dateCreated = now - 2
        sara.save(failOnError: true)

        dillon.dateCreated = now - 2
        dillon.save(failOnError: true)
    }

    private createAdminUserIfRequired() {
        if (!User.findByLoginId("admin")) {
            println "Fresh Database. Creating ADMIN user."

            def profile = new Profile(email: "admin@yourhost.com")
            def adminRole = new Role(authority: "ROLE_ADMIN").save(failOnError: true)
            def adminUser = new User(
                    loginId: "admin",
                    password: "secret",
                    profile: profile,
                    enabled: true).save(failOnError: true)
            UserRole.create adminUser, adminRole
        }
        else {
            println "Existing admin user, skipping creation"
        }
    }

}
