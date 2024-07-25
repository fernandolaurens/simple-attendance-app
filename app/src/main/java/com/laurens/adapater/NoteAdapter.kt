package com.laurens.adapater

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.laurens.absensiapp.R
import com.laurens.model.ModelNote
import com.laurens.utils.onClickItemListener
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.list_item_note.view.cvNote
import kotlinx.android.synthetic.main.list_item_note.view.roundedImage
import kotlinx.android.synthetic.main.list_item_note.view.tvText
import kotlinx.android.synthetic.main.list_item_note.view.tvTime
import kotlinx.android.synthetic.main.list_item_note.view.tvTitle

class NoteAdapter(private val modelNoteListFilter: List<ModelNote>,
                  private val onClickItem: onClickItemListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val modelNote = modelNoteListFilter[position]

        holder.title.text = modelNote.title
        holder.text.text = modelNote.noteText
        holder.timeDate.text = modelNote.dateTime

        if (modelNote.imagePath != null) {
            holder.roundedImageView.setImageBitmap(BitmapFactory.decodeFile(modelNote.imagePath))
            holder.roundedImageView.visibility = View.VISIBLE
        } else {
            holder.roundedImageView.visibility = View.GONE
        }

        holder.cvNote.setOnClickListener {
            onClickItem.onClick(modelNote, position)
        }
    }

    override fun getItemCount(): Int {
        return modelNoteListFilter.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class NoteViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView
        var text: TextView
        var timeDate: TextView
        var cvNote: CardView
        var roundedImageView: RoundedImageView

        init {
            title = itemView.tvTitle
            text = itemView.tvText
            timeDate = itemView.tvTime
            roundedImageView = itemView.roundedImage
            cvNote = itemView.cvNote
        }
    }
}