package com.javidconsulting.mvpvending.controller

import com.javidconsulting.mvpvending.model.*
import com.javidconsulting.mvpvending.service.UserService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.*
import javax.validation.Valid


@RestController
class UserController(
    private val userService: UserService,
    private val sessionRepository: FindByIndexNameSessionRepository<out Session?>,
) {
    @GetMapping("/user/{userId}")
    fun getUser(@PathVariable userId: String, @AuthenticationPrincipal user: UserPrincipal?): Optional<ViewUserDto> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return userService.findUser(userId, user)
    }

    @PostMapping("/user")
    fun createUser(
        @RequestBody @Valid createUserDto: CreateUserDto, @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<Unit> {
        val userId = userService.createUser(createUserDto, user)
        return ResponseEntity.created(URI.create("/user/$userId")).build()
    }

    @PutMapping("/user/{userId}")
    fun updateUser(
        @PathVariable userId: String, @RequestBody userDto: UpdateUserDto, @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<Unit> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        val id = userService.updateUser(userId, userDto, user)
        return ResponseEntity.created(URI.create("/user/$id")).build()
    }

    @DeleteMapping("/user/{userId}")
    fun deleteUser(@AuthenticationPrincipal user: UserPrincipal?, @PathVariable userId: String): String {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        userService.deleteUser(userId, user)
        return "Ok!"
    }

    @GetMapping("/user", "/users")
    fun allUsers(pageable: Pageable, @AuthenticationPrincipal user: UserPrincipal?): Page<ViewUserDto> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return userService.findAllUsers(pageable, user)
    }


    @PostMapping("/deposit")
    fun deposit(@RequestBody coin: Int, @AuthenticationPrincipal user: UserPrincipal?): ResponseEntity<Unit> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return when (coin) {
            5, 10, 20, 50, 100 -> {
                ResponseEntity.ok(userService.deposit(user.id, coin))
            }

            else -> {
                ResponseEntity.badRequest().build()
            }
        }

    }

    @PostMapping("/buy")
    fun buy(@RequestBody buyDto: BuyDto, @AuthenticationPrincipal user: UserPrincipal?): ResponseEntity<BoughtProduct> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return ResponseEntity.ok(userService.buy(buyDto, user))
    }

    @PostMapping("/reset")
    fun resetDeposit(
        @RequestBody buyDto: BuyDto,
        @AuthenticationPrincipal user: UserPrincipal?
    ): ResponseEntity<Coins> {
        if (user == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found")
        }
        return ResponseEntity.ok(userService.resetUserDeposit(user.id))
    }

}
