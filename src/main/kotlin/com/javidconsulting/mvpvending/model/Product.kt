package com.javidconsulting.mvpvending.model

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document
data class Product(
    val id: String?,
    val productName: String,
    val amountAvailable: Int,
    val cost: Long,
    val sellerId: String
) {
    init {
        require(cost % 5 == 0L) { "Product.cost should be in multiples of 5" }
    }

    fun toProductForSeller(): ProductForSeller {
        return ProductForSeller(
            this.id!!,
            this.productName,
            this.amountAvailable,
            this.cost,
            this.sellerId
        )
    }

    fun toProductForBuyers(): ProductForBuyer {
        return ProductForBuyer(
            this.id!!,
            this.productName,
            this.amountAvailable,
            this.cost,
        )
    }
}

data class CreateProductDto(
    val productName: String,
    val amountAvailable: Int,
    val cost: Long,
) {
    fun toProduct(sellerId: String): Product {
        return Product(
            null,
            productName,
            amountAvailable,
            cost,
            sellerId
        )
    }
}

data class UpdateProductDto(
    val productName: String?,
    val amountAvailable: Int?,
    val cost: Long?,
//    val sellerId: String?
)


data class ProductForBuyer(
    val id: String,
    val productName: String,
    val amountAvailable: Int,
    val cost: Long,
)

data class ProductForSeller(
    val id: String,
    val productName: String,
    val amountAvailable: Int,
    val cost: Long,
    val sellerId: String
)

data class BuyDto(
    val productId: String,
    val amount: Int,
)

data class BoughtProduct(
    val productId: String,
    val productName: String,
    val amount: Int,
    val totalPrice: Long,
    val change: Long,
    val coins: Coins,
) {
    constructor(product: Product, amount: Int, totalPrice: Long, change: Long) : this(
        product.id!!,
        product.productName,
        amount,
        totalPrice,
        change,
        Coins.create(change)
    ) {


    }
}

data class Coins(
    val fiveCents: Int,
    val tenCents: Int,
    val twentyCents: Int,
    val fiftyCents: Int,
    val hundredCents: Int,
) {
    companion object Factory {
        fun create(totalPrice: Long): Coins {
            var price = totalPrice

            val mapOfCoins = mutableMapOf<Int, Int>()

            for (coin in arrayOf(100, 50, 20, 10, 5)) {
                if (price == 0L) {
                    break
                }
                if (price < coin) {
                    continue
                }
                val countOfCoin = price / coin
                price %= coin
                mapOfCoins[coin] = countOfCoin.toInt()
            }

            return Coins(
                mapOfCoins[5] ?: 0,
                mapOfCoins[10] ?: 0,
                mapOfCoins[20] ?: 0,
                mapOfCoins[50] ?: 0,
                mapOfCoins[100] ?: 0
            )
        }
    }

}

