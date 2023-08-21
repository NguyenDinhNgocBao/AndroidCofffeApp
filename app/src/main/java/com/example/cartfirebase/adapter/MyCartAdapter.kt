package com.example.cartfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cartfirebase.R
import com.example.cartfirebase.databinding.LayoutCartItemBinding
import com.example.cartfirebase.evenbus.UpdateCartEvent
import com.example.cartfirebase.model.CartModel
import com.google.firebase.database.FirebaseDatabase
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder
import java.nio.file.attribute.AclEntry.Builder

private lateinit var binding:LayoutCartItemBinding

class MyCartAdapter(private val context: Context, private val ds: List<CartModel>) : RecyclerView.Adapter<MyCartAdapter.MyCartViewHolder>() {
    class MyCartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var btnMinus:ImageView? = null
        var btnPlus:ImageView? = null
        var imgLogo:ImageView? = null
        var txtName:TextView? = null
        var txtPrice:TextView? = null
        var txtQuantity:TextView? = null
        var btnDelete:ImageView? = null

        init {
            btnMinus = itemView.findViewById(R.id.btnMinus) as ImageView
            btnPlus = itemView.findViewById(R.id.btnPlus) as ImageView
            imgLogo = itemView.findViewById(R.id.imgLogo) as ImageView
            txtName = itemView.findViewById(R.id.txtName) as TextView
            txtPrice = itemView.findViewById(R.id.txtPrice) as TextView
            txtQuantity = itemView.findViewById(R.id.txtQuantity) as TextView
            btnDelete = itemView.findViewById(R.id.btnDelete) as ImageView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCartViewHolder {
        return MyCartViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false))
    }

    override fun onBindViewHolder(holder: MyCartViewHolder, position: Int) {
        Glide.with(context)
            .load(ds[position].image) // Đường dẫn URL của hình ảnh
            .into(holder.imgLogo!!)

        holder.txtName!!.text = StringBuilder().append(ds[position].name)
        holder.txtPrice!!.text = StringBuilder().append(ds[position].price).append("d")
        holder.txtQuantity!!.text = StringBuilder().append(ds[position].quantity)

        //Event
        holder.btnMinus!!.setOnClickListener{_ -> minusCartItem(holder, ds[position])}
        holder.btnPlus!!.setOnClickListener{_ -> plusCartItem(holder, ds[position])}
        holder.btnDelete!!.setOnClickListener{_ ->
            val dialog = AlertDialog.Builder(context).setTitle("Delete Item ?")
                .setMessage("Do you wanna delete this item ?")
                .setNegativeButton("Cancel") {dialog, _ -> dialog.dismiss()}
                .setPositiveButton("Delete") {dialog, _ ->
                    notifyItemRemoved(position)
                    FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
                        .child(ds[position].key!!)
                        .removeValue()
                        .addOnSuccessListener { EventBus.getDefault().postSticky(UpdateCartEvent()) }
                }
                .create()
            dialog.show()
        }
    }

    private fun plusCartItem(holder: MyCartAdapter.MyCartViewHolder, cartModel: CartModel) {
        cartModel.quantity += 1
        cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()

        holder.txtQuantity!!.text = StringBuilder().append(cartModel.quantity)
        updateFireBase(cartModel)
    }

    private fun minusCartItem(holder: MyCartAdapter.MyCartViewHolder, cartModel: CartModel) {
        if(cartModel.quantity > 1){
            cartModel.quantity -= 1
            cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
            holder.txtQuantity!!.text = StringBuilder().append(cartModel.quantity)
            updateFireBase(cartModel)
        }else if(cartModel.quantity == 1){
            val dialog = AlertDialog.Builder(context).setTitle("Delete Item ?")
                .setMessage("?")
                .setNegativeButton("OK") {dialog, _ -> dialog.dismiss()}
                .create()
            dialog.show()
        }
    }

    private fun updateFireBase(cartModel: CartModel) {
        FirebaseDatabase.getInstance().getReference("Cart").child("UNIQUE_USER_ID")
            .child(cartModel.key!!)
            .setValue(cartModel)
            .addOnSuccessListener {
                EventBus.getDefault().postSticky(UpdateCartEvent())
            }
    }

    override fun getItemCount(): Int {
        return ds.size
    }
}