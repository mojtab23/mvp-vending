package com.javidconsulting.mvpvending.service

import com.javidconsulting.mvpvending.model.*
import com.javidconsulting.mvpvending.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val productService: ProductService,
) {
    private val log = LoggerFactory.getLogger(UserService::class.java)


    fun findAllUsers(pageable: Pageable, user: UserPrincipal): Page<ViewUserDto> {
        if (!user.hasRole(UserRole.Seller)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "you can't see other users")
        }
        return userRepository.findAll(pageable).map(::userToViewDto)
    }

    fun findUser(userId: String, user: UserPrincipal): Optional<ViewUserDto> {
        if (!user.hasRole(UserRole.Seller) && user.id != userId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "you can't see other users")
        }
        return userRepository.findById(userId).map(::userToViewDto)
    }

    fun loadUserByUsername(username: String): Optional<User> {
        return userRepository.findUserByUsername(username)
    }

    fun createUser(createUserDto: CreateUserDto, user: UserPrincipal?): String {
        val userOptional = userRepository.findUserByUsername(createUserDto.username)
        if (userOptional.isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "user already exists")
        }

        val roles = if (user != null && user.authorities.contains(UserRole.Seller) && createUserDto.roles != null) {
            createUserDto.roles
        } else {
            setOf(UserRole.Buyer)
        }

        val newUser = createUserDto.toUser(roles, passwordEncoder)
        val savedUser = userRepository.save(newUser)
        return savedUser.id!!
    }

    fun deleteUser(userId: String, user: UserPrincipal) {
        if (!user.hasRole(UserRole.Seller)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "you can't do this")
        }
        userRepository.deleteById(userId)
    }

    fun deposit(userId: String, coin: Int) {
        when (coin) {
            5, 10, 20, 50, 100 -> {
                userRepository.deposit(userId, coin)
            }

            else -> {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid coin!")
            }
        }


    }

    @Transactional
    fun buy(buyDto: BuyDto, user: UserPrincipal): BoughtProduct {
        val buyerUser = userRepository.findById(user.id).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }

        val boughtProduct = productService.buy(buyDto, buyerUser.deposit)
        doResetUserDeposit(buyerUser)
        return boughtProduct
    }

    @Transactional
    fun resetUserDeposit(userId: String): Coins {
        val user = userRepository.findById(userId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
        val deposit = user.deposit
        doResetUserDeposit(user)
        return Coins.create(deposit)
    }

    fun doResetUserDeposit(user: User) {
        val updatedUser = User(
            user.id, user.username, user.password, 0, user.roles
        )
        userRepository.save(updatedUser)
    }

    @Transactional
    fun updateUser(userId: String, userDto: UpdateUserDto, user: UserPrincipal): String {
        log.debug("update user: userId:$userId, userDto:$userDto, user:$user")
        if (!user.hasRole(UserRole.Seller)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "you can't do this")
        }
        val userToUpdate = userRepository.findById(userId).orElseThrow {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "user not found")
        }
        val password = if (userDto.password != null) {
            passwordEncoder.encode(userDto.password)
        } else {
            userToUpdate.password
        }
        if (userDto.deposit != null && userDto.deposit % 5 != 0L) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "deposit should be divisible by 5")
        }

        val userUpdate = User(
            userToUpdate.id,
            userToUpdate.username,
            password,
            userDto.deposit ?: userToUpdate.deposit,
            userDto.roles ?: userToUpdate.roles,
        )
        val savedUser = userRepository.save(userUpdate)
        return savedUser.id!!

    }

}
