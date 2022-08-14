package com.javidconsulting.mvpvending

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.javidconsulting.mvpvending.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.Paths
import java.util.*


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest(
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val mongoTemplate: MongoTemplate,
    @LocalServerPort private val port: Int
) {


    private val sampleUserJson: File = Paths.get("src", "test", "resources", "data", "sample-users.json").toFile()
    private val sampleProductsJson: File =
        Paths.get("src", "test", "resources", "data", "sample-products.json").toFile()


    @BeforeEach
    @Throws(IOException::class)
    fun setUp() {
        val users = mapper.readValue(sampleUserJson, Array<User>::class.java)
        Arrays.stream(users).forEach(mongoTemplate::save)
        val products: Array<Product> = mapper.readValue(sampleProductsJson, Array<Product>::class.java)
        Arrays.stream(products).forEach(mongoTemplate::save)
    }

    @Test
    fun `test deposit and buy works correctly`() {
        // login
        val cookies = login()
        val headers = HttpHeaders()
        headers[HttpHeaders.COOKIE] = cookies
        val getMe = RequestEntity(Unit, headers, HttpMethod.GET, URI.create("http://localhost:$port/me"))
        val meResult = restTemplate.exchange(getMe, UserMe::class.java)
        assertThat(meResult.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(meResult.body).isNotNull
        val userId = meResult.body!!.id

        // add deposit
        val entity = RequestEntity(100, headers, HttpMethod.POST, URI.create("http://localhost:$port/deposit"))
        val firstEntity = restTemplate.exchange(entity, Unit::class.java)
        assertThat(firstEntity.statusCode).isEqualTo(HttpStatus.OK)

        // check user deposit
        val readUserRequest =
            RequestEntity(Unit, headers, HttpMethod.GET, URI.create("http://localhost:$port/user/$userId"))
        val userResult = restTemplate.exchange(readUserRequest, ViewUserDto::class.java)
        assertThat(userResult.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(userResult.body).isNotNull
        assertThat(userResult.body!!.deposit).isEqualTo(100)

        // add deposit second time
        val addToDeposit = RequestEntity(5, headers, HttpMethod.POST, URI.create("http://localhost:$port/deposit"))
        val addToDepositResult = restTemplate.exchange(addToDeposit, Unit::class.java)
        assertThat(addToDepositResult.statusCode).isEqualTo(HttpStatus.OK)

        // check user deposit
        val readUserRequest2 =
            RequestEntity(Unit, headers, HttpMethod.GET, URI.create("http://localhost:$port/user/$userId"))
        val userResult2 = restTemplate.exchange(readUserRequest2, ViewUserDto::class.java)
        assertThat(userResult2.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(userResult2.body).isNotNull
        assertThat(userResult2.body!!.deposit).isEqualTo(105)

        // get products
        val readProducts = RequestEntity(Unit, headers, HttpMethod.GET, URI.create("http://localhost:$port/products"))
        val productsResult = restTemplate.exchange(readProducts, JsonNode::class.java)
        assertThat(productsResult.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(productsResult.body).isNotNull
        val product = productsResult.body!!["content"].first()
        assertThat(product["productName"].textValue()).isEqualTo("Drink")

        // buy drink
        val buyDto = BuyDto(product["id"].textValue(), 2)
        val buy = RequestEntity(buyDto, headers, HttpMethod.POST, URI.create("http://localhost:$port/buy"))
        val buyResult = restTemplate.exchange(buy, BoughtProduct::class.java)
        assertThat(buyResult.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(buyResult.body).isNotNull
        assertThat(buyResult.body!!.amount).isEqualTo(2)
        assertThat(buyResult.body!!.change).isEqualTo(35)
        assertThat(buyResult.body!!.productName).isEqualTo("Drink")
        assertThat(buyResult.body!!.coins.fiveCents).isEqualTo(1)
        assertThat(buyResult.body!!.coins.tenCents).isEqualTo(1)
        assertThat(buyResult.body!!.coins.twentyCents).isEqualTo(1)
        assertThat(buyResult.body!!.coins.fiftyCents).isEqualTo(0)
        assertThat(buyResult.body!!.coins.hundredCents).isEqualTo(0)

    }

    private fun login(): MutableList<String>? {

        val entity = restTemplate.postForEntity(
            "http://localhost:$port/login",
            "{\n" + "  \"username\": \"admin\",\n" + "  \"password\": \"admin\"\n" + "}",
            Unit::class.java
        )

        val cookies = entity.headers[HttpHeaders.SET_COOKIE]
        assertThat(entity.statusCode).isEqualTo(HttpStatus.FOUND)
        return cookies
    }

    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(Product::class.java)
        mongoTemplate.dropCollection(User::class.java)
    }
}
