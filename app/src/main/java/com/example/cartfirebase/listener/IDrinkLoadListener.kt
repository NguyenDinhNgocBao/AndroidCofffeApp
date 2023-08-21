package com.example.cartfirebase.listener

import com.example.cartfirebase.model.DrinkModel

interface IDrinkLoadListener {
    fun onLoadSucces(drinkModelList: List<DrinkModel>?)
    fun onLoadFailed(message:String?)
}