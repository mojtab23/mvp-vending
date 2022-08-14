package com.javidconsulting.mvpvending.repository

import com.javidconsulting.mvpvending.model.Product
import org.springframework.data.mongodb.repository.MongoRepository

interface ProductRepository : MongoRepository<Product, String> {

    fun deleteAllByIdAndSellerId(id: String, sellerId: String)

}
