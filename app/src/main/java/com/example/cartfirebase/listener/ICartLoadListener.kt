package com.example.cartfirebase.listener

import com.example.cartfirebase.model.CartModel

interface ICartLoadListener {
    fun onLoadCartSucess(cartModeList: List<CartModel>)
    fun onLoadCartFailed(message:String?)
}