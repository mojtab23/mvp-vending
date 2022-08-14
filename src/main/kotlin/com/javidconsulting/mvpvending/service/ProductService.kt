package com.javidconsulting.mvpvending.service

import com.javidconsulting.mvpvending.model.*
import com.javidconsulting.mvpvending.repository.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository
) {
    fun findProduct(productId: String, user: UserPrincipal): Optional<Any> {
        return productRepository.findById(productId).map {
            if (user.hasRole(UserRole.Seller)) {
                it.toProductForSeller()
            } else {
                it.toProductForBuyers()
            }
        }
    }

    fun createProduct(createProductDto: CreateProductDto, user: UserPrincipal): String {
        if (!user.hasRole(UserRole.Seller)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "only sellers can add product")
        }
        val product = createProductDto.toProduct(user.id)
        val savedProduct = productRepository.save(product)
        return savedProduct.id!!
    }

    @Transactional
    fun updateProduct(productId: String, updateDto: UpdateProductDto, user: UserPrincipal) {
        val product = findProductToEdit(user, productId)
        val updatedProduct = Product(
            product.id,
            updateDto.productName ?: product.productName,
            updateDto.amountAvailable ?: product.amountAvailable,
            updateDto.cost ?: product.cost,
            product.sellerId
        )
        productRepository.save(updatedProduct)
    }

    @Transactional
    fun deleteProduct(productId: String, user: UserPrincipal) {
        findProductToEdit(user, productId)
        productRepository.deleteAllByIdAndSellerId(productId, user.id)
    }

    private fun findProductToEdit(user: UserPrincipal, productId: String): Product {
        if (!user.hasRole(UserRole.Seller)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "you are not a seller")
        }
        val productToDelete = productRepository.findById(productId)
        val product = productToDelete.orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "product not found")
        }
        if (product.sellerId != user.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "you are not the creator of this product")
        }
        return product
    }

    fun findAllProducts(pageable: Pageable, user: UserPrincipal): Page<Any> {
        return productRepository.findAll(pageable).map {
            if (user.hasRole(UserRole.Seller)) {
                it.toProductForSeller()
            } else {
                it.toProductForBuyers()
            }
        }
    }

    @Transactional
    fun buy(buyDto: BuyDto, deposit: Long): BoughtProduct {
        val productToBuy = productRepository.findById(buyDto.productId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "product not found")
        }
        if (productToBuy.amountAvailable < buyDto.amount) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough product available")
        }
        // calculate total price
        val totalPrice = productToBuy.cost * buyDto.amount
        if (totalPrice > deposit) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough deposit, total price is: $totalPrice")
        }
        val change = deposit - totalPrice
        // update amount available
        val newAmount = productToBuy.amountAvailable - buyDto.amount
        val updatedProduct = Product(
            productToBuy.id,
            productToBuy.productName,
            newAmount,
            productToBuy.cost,
            productToBuy.sellerId
        )
        productRepository.save(updatedProduct)
        return BoughtProduct(productToBuy, buyDto.amount, totalPrice, change)
    }

}
