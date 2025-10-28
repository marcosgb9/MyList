package com.example.mylist

import UserReview
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ReviewAdapter(
    private var reviews: List<UserReview>,
    private val onClick: (UserReview) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.textTitulo)
        val genero: TextView = itemView.findViewById(R.id.textGenero)
        val comentario: TextView = itemView.findViewById(R.id.textComentario)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarItem)
        val imageView: ImageView = itemView.findViewById(R.id.imageResena)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_resena, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.titulo.text = review.titulo
        holder.genero.text = review.genero
        holder.comentario.text = review.comentario
        holder.ratingBar.rating = review.valoracion.toFloat()

        Glide.with(holder.itemView.context)
            .load(review.imagen)
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .into(holder.imageView)

        holder.itemView.setOnClickListener { onClick(review) }
    }


    override fun getItemCount(): Int = reviews.size

    fun actualizarDatos(newList: List<UserReview>) {
        reviews = newList
        notifyDataSetChanged()
    }
}
