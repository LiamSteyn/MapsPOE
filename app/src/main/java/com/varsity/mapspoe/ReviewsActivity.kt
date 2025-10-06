package com.varsity.mapspoe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varsity.mapspoe.data.DataRepository
import com.varsity.mapspoe.domain.Review
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReviewsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)

        val storeId = intent.getStringExtra("storeId") ?: "st_ck" // default for demo
        val recycler = findViewById<RecyclerView>(R.id.reviewsList)
        val adapter = ReviewsAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        DataRepository.init(this)

        lifecycleScope.launch {
            DataRepository.observeReviews(storeId).collectLatest { list ->
                adapter.submit(list)
            }
        }

        // Optionally try to refresh from providers (non-blocking)
        lifecycleScope.launch {
            DataRepository.refreshReviews(storeId)
        }
    }

    private class ReviewsAdapter : RecyclerView.Adapter<ReviewVH>() {
        private val items = mutableListOf<Review>()
        fun submit(newItems: List<Review>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ReviewVH {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_review, parent, false)
            return ReviewVH(view)
        }
        override fun getItemCount() = items.size
        override fun onBindViewHolder(holder: ReviewVH, position: Int) = holder.bind(items[position])
    }

    private class ReviewVH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        fun bind(r: Review) {
            itemView.findViewById<android.widget.TextView>(R.id.authorText).text = r.author
            itemView.findViewById<android.widget.TextView>(R.id.ratingText).text = "★ " + r.rating.toString()
            itemView.findViewById<android.widget.TextView>(R.id.commentText).text = r.comment
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(java.util.Date(r.createdAt))
            itemView.findViewById<android.widget.TextView>(R.id.dateText).text = date
        }
    }
}