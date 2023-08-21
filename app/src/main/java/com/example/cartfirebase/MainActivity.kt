package com.example.cartfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cartfirebase.adapter.MyDrinkAdapter
import com.example.cartfirebase.databinding.ActivityMainBinding
import com.example.cartfirebase.evenbus.UpdateCartEvent
import com.example.cartfirebase.listener.ICartLoadListener
import com.example.cartfirebase.listener.IDrinkLoadListener
import com.example.cartfirebase.model.CartModel
import com.example.cartfirebase.model.DrinkModel
import com.example.cartfirebase.util.SpaceItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private lateinit var binding:ActivityMainBinding
class MainActivity : AppCompatActivity(), IDrinkLoadListener,ICartLoadListener {

    lateinit var drinkLoadListener : IDrinkLoadListener
    lateinit var cartLoadListener : ICartLoadListener

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java)){
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public fun onUpdateCartEvent(event: UpdateCartEvent){
        countCartFromFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        loadDrinkFromFirebase()
        countCartFromFirebase()
    }

    private fun countCartFromFirebase() {
        val cartModels: MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(cartSnapshot in snapshot.children){
                            val cartModel = cartSnapshot.getValue(CartModel::class.java)
                            cartModel!!.key = cartSnapshot.key
                            cartModels.add(cartModel)
                        }
                        cartLoadListener.onLoadCartSucess(cartModels)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener.onLoadCartFailed(error.message)
                }
            })
    }

    private fun loadDrinkFromFirebase() {
        val drinkModels: MutableList<DrinkModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Drink")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(drinkSnapshot in snapshot.children){
                            val drinkModel = drinkSnapshot.getValue(DrinkModel::class.java)
                            drinkModel!!.key = drinkSnapshot.key
                            drinkModels.add(drinkModel)
                        }
                        drinkLoadListener.onLoadSucces(drinkModels)
                        // In ra Logcat để kiểm tra dữ liệu đã tải
                        for (model in drinkModels) {
                            Log.d("FirebaseData", "Name: ${model.name}, Image: ${model.image}, Price: ${model.price}")
                        }
                    }else{
                        drinkLoadListener.onLoadFailed("Drink item dosen't exists!!!")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    drinkLoadListener.onLoadFailed(error.message)
                }
        })
    }

    private fun init() {
        drinkLoadListener = this
        cartLoadListener = this

        val gridLayoutManager = GridLayoutManager(this,2)
        binding.rvList.layoutManager = gridLayoutManager
        //binding.rvList.addItemDecoration(SpaceItemDecoration())

        binding.btnCart.setOnClickListener{
            val i = Intent(this, CartActivity::class.java)
            startActivity(i)
        }
    }

    override fun onLoadSucces(drinkModelList: List<DrinkModel>?) {
        val adapter = MyDrinkAdapter(this,drinkModelList!!, cartLoadListener)
        binding.rvList.adapter = adapter
    }

    override fun onLoadFailed(message: String?) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    override fun onLoadCartSucess(cartModeList: List<CartModel>) {
        var cartSum = 0
        for(i in cartModeList!!) cartSum += i.quantity
        binding.badge.setNumber(cartSum)
    }

    override fun onLoadCartFailed(message: String?) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
}