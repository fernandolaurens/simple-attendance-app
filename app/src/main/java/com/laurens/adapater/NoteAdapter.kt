package com.laurens.adapater

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.laurens.absensiapp.databinding.ListItemNoteBinding
import com.laurens.model.ModelNote
import com.laurens.utils.onClickItemListener

class NoteAdapter(
    private val modelNoteListFilter: List<ModelNote>,
    private val onClickItem: onClickItemListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ListItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val modelNote = modelNoteListFilter[position]

        holder.binding.tvTitle.text = modelNote.title
        holder.binding.tvText.text = modelNote.noteText
        holder.binding.tvTime.text = modelNote.dateTime

        if (modelNote.imagePath != null) {
            holder.binding.roundedImage.setImageBitmap(BitmapFactory.decodeFile(modelNote.imagePath))
            holder.binding.roundedImage.visibility = View.VISIBLE
        } else {
            holder.binding.roundedImage.visibility = View.GONE
        }

        holder.binding.cvNote.setOnClickListener {
            onClickItem.onClick(modelNote, position)
        }
    }

    override fun getItemCount(): Int {
        return modelNoteListFilter.size
    }

    inner class NoteViewHolder(val binding: ListItemNoteBinding) : RecyclerView.ViewHolder(binding.root)
}