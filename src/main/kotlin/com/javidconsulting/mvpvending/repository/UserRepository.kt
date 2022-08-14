package com.javidconsulting.mvpvending.repository

import com.javidconsulting.mvpvending.model.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface UserRepository : MongoRepository<User, String>, CustomUserRepository {

    fun findUserByUsername(username: String): Optional<User>

}

interface CustomUserRepository {

    fun deposit(userId: String, coin: Int)

}

class CustomUserRepositoryImpl(
    @Autowired private val mongoTemplate: MongoTemplate
) : CustomUserRepository {
    override fun deposit(userId: String, coin: Int) {
        val query = Query()
        query.addCriteria(Criteria.where("_id").`is`(userId))
        val update = Update().inc("deposit", coin)
        mongoTemplate.updateFirst(query, update, User::class.java)
    }


}

