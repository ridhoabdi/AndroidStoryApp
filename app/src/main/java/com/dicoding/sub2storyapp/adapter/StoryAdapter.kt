package com.dicoding.sub2storyapp.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.sub2storyapp.R
import com.dicoding.sub2storyapp.data.remote.response.ListStoryItem
import com.dicoding.sub2storyapp.databinding.ItemRowBinding
import com.dicoding.sub2storyapp.model.StoryModel
import com.dicoding.sub2storyapp.ui.detailstory.DetailStoryActivity

class StoryAdapter : PagingDataAdapter<ListStoryItem, StoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    class MyViewHolder(private val binding: ItemRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStoryItem) {
            binding.tvTittle.text = data.name
            binding.tvDesc.text = data.description
            Glide.with(binding.root.context)
                .load(data.photoUrl)
                .error(R.drawable.ic_baseline_broken_image_black_24)
                .placeholder(R.drawable.ic_baseline_image_black_24)
                .into(binding.imgStory)
            binding.root.setOnClickListener {
                val story = StoryModel(
                    data.name,
                    data.photoUrl,
                    data.description,
                    null,
                    null
                )

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        binding.root.context as Activity,
                        Pair(binding.imgStory, "image"),
                        Pair(binding.tvTittle, "name"),
                        Pair(binding.tvDesc, "description")
                    )

                val intent = Intent(binding.root.context, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.DETAIL_STORY, story)
                binding.root.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}