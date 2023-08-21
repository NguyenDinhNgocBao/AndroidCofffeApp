package com.example.cartfirebase.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cartfirebase.R
import com.example.cartfirebase.databinding.LayoutDrinkItemBinding
import com.example.cartfirebase.evenbus.UpdateCartEvent
import com.example.cartfirebase.listener.ICartLoadListener
import com.example.cartfirebase.listener.IClickItemListener
import com.example.cartfirebase.listener.IDrinkLoadListener
import com.example.cartfirebase.model.CartModel
import com.example.cartfirebase.model.DrinkModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder
private lateinit var binding : LayoutDrinkItemBinding

class MyDrinkAdapter(private val context: Context,private val ds:List<DrinkModel>,private val cartListener: ICartLoadListener):RecyclerView.Adapter<MyDrinkAdapter.MyDrinkViewHolder>() {
    class MyDrinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
         var imageView:ImageView? = null
         var txtName:TextView? = null
         var txtPrice:TextView? = null

        private var clickListener: IClickItemListener? = null

        fun setClickListener(clickListener: IClickItemListener){
            this.clickListener = clickListener
        }

        init {
            imageView = itemView.findViewById(R.id.imgLogo) as ImageView
            txtName = itemView.findViewById(R.id.txtName) as TextView
            txtPrice = itemView.findViewById(R.id.txtPrice) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            clickListener!!.onItemClickListener(p0,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDrinkViewHolder {
        return MyDrinkViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_drink_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyDrinkViewHolder, position: Int) {
        Glide.with(context)
            .load(ds[position].image) // Đường dẫn URL của hình ảnh
            .into(holder.imageView!!)

        holder.txtName!!.text = StringBuilder().append(ds[position].name)
        holder.txtPrice!!.text = StringBuilder().append(ds[position].price).append("d")

        holder.setClickListener(object : IClickItemListener{
            override fun onItemClickListener(view: View?, position: Int) {
                addToCart(ds[position])
            }
        })
    }

    private fun addToCart(drinkModel: DrinkModel) {
        val userCart = FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
        userCart.child(drinkModel.key!!).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //If item has in Cart, just Update
                if(snapshot.exists()){
                    val cartModel = snapshot.getValue(CartModel::class.java)
                    val updateData: MutableMap<String,Any> = HashMap()
                    cartModel!!.quantity = cartModel!!.quantity+1
                    updateData["quantity"] = cartModel!!.quantity
                    updateData["totalPrice"] = cartModel!!.quantity * cartModel.price!!.toFloat()

                    userCart.child(drinkModel.key!!).updateChildren(updateData)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
                            cartListener.onLoadCartFailed("Success add to cart")
                        }
                        .addOnFailureListener { err -> cartListener.onLoadCartFailed(err.message) }
                }else{
                    //if item not current in Cart, add to cart
                    val cartModel = CartModel()
                    cartModel.key = drinkModel.key
                    cartModel.name = drinkModel.name
                    cartModel.image = drinkModel.image
                    cartModel.price = drinkModel.price
                    cartModel.quantity = 1
                    cartModel.totalPrice = drinkModel.price!!.toFloat()

                    userCart.child(drinkModel.key!!).setValue(cartModel)
                        .addOnSuccessListener {
                            EventBus.getDefault().postSticky(UpdateCartEvent())
                            cartListener.onLoadCartFailed("Success add to cart")
                        }
                        .addOnFailureListener { err -> cartListener.onLoadCartFailed(err.message) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cartListener.onLoadCartFailed(error.message)
            }
        })
    }

    override fun getItemCount(): Int {
        return ds.size
    }
}

