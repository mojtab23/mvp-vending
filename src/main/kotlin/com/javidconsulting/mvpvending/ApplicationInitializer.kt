package com.javidconsulting.mvpvending

import com.javidconsulting.mvpvending.model.*
import com.javidconsulting.mvpvending.repository.ProductRepository
import com.javidconsulting.mvpvending.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ApplicationInitializer(
    @Value("\${mvp-vending.initial-data:true}") private val initialData: Boolean,
    private val userService: UserService,
    private val productRepository: ProductRepository
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(ApplicationInitializer::class.java)

    override fun run(args: ApplicationArguments?) {
        if (!initialData) {
            return
        }
        val fakeUser = User(null, "", "", 0, setOf(UserRole.Seller, UserRole.Buyer))
        try {
            val admin = CreateUserDto("admin", "admin", setOf(UserRole.Seller, UserRole.Buyer))
            val adminId = userService.createUser(admin, UserPrincipal(fakeUser))
            log.debug("Admin created with Id: $adminId")

        } catch (_: ResponseStatusException) {
        }
        try {
            val buyer = CreateUserDto("user", "user", setOf(UserRole.Buyer))
            val buyerId = userService.createUser(buyer, UserPrincipal(fakeUser))
            log.debug("Buyer created with Id: $buyerId")
        } catch (_: ResponseStatusException) {
        }
        try {
            val admin = userService.loadUserByUsername("admin")
            val drink = Product("62f68a4da89d000d797d9e06", "Drink", 5, 35, admin.get().id!!)
            val savedDrink = productRepository.save(drink)
            log.debug("drink saved:{}", savedDrink)
        } catch (_: Exception) {
        }
    }
}
