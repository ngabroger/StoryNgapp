package com.ngabroger.storyngapp.costumview

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class EmailEditText@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle

) :AppCompatEditText(context ,attrs,defStyleAttr) {

    init {
        hint = "Email"
        setPadding(40, 40, 20, 40)
        textSize = 16f
        setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                val email = text.toString()
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    error = "Invalid email"
                }
            }
        }
    }

}