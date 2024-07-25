package com.laurens.utils

import com.laurens.model.ModelNote

interface onClickItemListener {
    fun onClick(modelNote: ModelNote, position: Int)
}