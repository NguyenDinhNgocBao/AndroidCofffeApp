package com.example.cartfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cartfirebase.adapter.MyCartAdapter
import com.example.cartfirebase.databinding.ActivityCartBinding
import com.example.cartfirebase.listener.ICartLoadListener
import com.example.cartfirebase.model.CartModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var binding:ActivityCartBinding
class CartActivity : AppCompatActivity(), ICartLoadListener {

    var cartLoadListener: ICartLoadListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCart()
        loadCartFromFirebase()
    }

    private fun loadCartFromFirebase() {
        val cartModels: MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(cartSnapshot in snapshot.children){
                            val cartModel = cartSnapshot.getValue(CartModel::class.java)
                            cartModel!!.key = cartSnapshot.key
                            cartModels.add(cartModel)
                        }
                        cartLoadListener!!.onLoadCartSucess(cartModels)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener!!.onLoadCartFailed(error.message)
                }
            })
    }

    private fun initCart() {
        cartLoadListener = this
        val LayoutManager = LinearLayoutManager(this)
        binding.rvCart.layoutManager = LayoutManager
        binding.rvCart.addItemDecoration(DividerItemDecoration(this,LayoutManager.orientation))
        binding.btnBack.setOnClickListener{
            finish()
        }
    }

    override fun onLoadCartSucess(cartModeList: List<CartModel>) {
        var cartSum = 0.0
        for(i in cartModeList!!) cartSum  += i!!.totalPrice
        binding.txtTilte.text = StringBuilder("${cartSum}").append("d")
        val adapter = MyCartAdapter(this, cartModeList)
        binding.rvCart!!.adapter = adapter
    }

    override fun onLoadCartFailed(message: String?) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }
}