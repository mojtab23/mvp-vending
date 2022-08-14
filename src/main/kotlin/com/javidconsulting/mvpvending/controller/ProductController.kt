package com.javidconsulting.mvpvending.controller

import com.javidconsulting.mvpvending.model.CreateProductDto
import com.javidconsulting.mvpvending.model.UpdateProductDto
import com.javidconsulting.mvpvending.model.UserPrincipal
import com.javidconsulting.mvpvending.service.ProductService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*

@RestController
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("/product/{productId}")
    fun getProduct(@PathVariable productId: String, @AuthenticationPrincipal user: UserPrincipal?): Optional<Any> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return productService.findProduct(productId, user)
    }

    @PostMapping("/product")
    fun createProduct(
        @RequestBody createProductDto: CreateProductDto,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<Unit> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        val productId = productService.createProduct(createProductDto, user)
        return ResponseEntity.created(URI.create("/product/$productId")).build()
    }

    @PutMapping("/product/{productId}")
    fun updateProduct(
        @PathVariable productId: String,
        @RequestBody updateDto: UpdateProductDto,
        @AuthenticationPrincipal user: UserPrincipal?
    ): String {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        productService.updateProduct(productId, updateDto, user)
        return "Ok!"
    }

    @DeleteMapping("/product/{productId}")
    fun deleteProduct(@PathVariable productId: String, @AuthenticationPrincipal user: UserPrincipal?): String {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        productService.deleteProduct(productId, user)
        return "Ok!"
    }

    @GetMapping("/product", "/products")
    fun allProducts(pageable: Pageable, @AuthenticationPrincipal user: UserPrincipal?): Page<Any> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return productService.findAllProducts(pageable, user)
    }

}
