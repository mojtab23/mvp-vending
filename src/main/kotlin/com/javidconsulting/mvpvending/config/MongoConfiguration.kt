package com.javidconsulting.mvpvending.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager


@Configuration
class MongoConfiguration {

    @Bean
    fun transactionManager(dbFactory: MongoDatabaseFactory?): MongoTransactionManager? {
        return MongoTransactionManager(dbFactory!!)
    }

}
