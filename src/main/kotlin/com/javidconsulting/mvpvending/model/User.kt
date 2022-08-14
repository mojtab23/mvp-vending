package com.javidconsulting.mvpvending.model

import com.javidconsulting.mvpvending.validation.ValidPassword
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import javax.validation.constraints.NotBlank

@Document
data class User(
    val id: String?,
    val username: String,
    val password: String,
    val deposit: Long,
    val roles: Set<UserRole>
) : java.io.Serializable

class UserPrincipal(private val user: User) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return user.roles.toMutableSet()
    }

    val id: String get() = user.id ?: ""
    override fun getPassword() = user.password
    override fun getUsername() = user.username
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
    fun hasRole(role: UserRole) =
        this.user.roles.contains(role)

}

data class UserMe(val id: String, val username: String, val session: Map<String, Any>)

enum class UserRole : GrantedAuthority {
    Seller {
        override fun getAuthority(): String = this.name
    },
    Buyer {
        override fun getAuthority(): String = this.name
    };
}

data class ViewUserDto(
    val id: String,
    val username: String,
    val deposit: Long,
    val roles: Set<UserRole>
)

data class CreateUserDto(
    @field:NotBlank val username: String,
    @field:ValidPassword val password: String,
    val roles: Set<UserRole>?
) {
    fun toUser(roles: Set<UserRole>, passwordEncoder: PasswordEncoder): User {
        return User(null, username, passwordEncoder.encode(password), 0, roles)
    }
}

data class UpdateUserDto(
    val password: String?,
    val deposit: Long?,
    val roles: Set<UserRole>?
)

fun userToViewDto(user: User): ViewUserDto {
    return ViewUserDto(
        user.id!!,
        user.username,
        user.deposit,
        user.roles
    )

}
