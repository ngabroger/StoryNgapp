package com.ngabroger.storyngapp.costumview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class PasswordEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle

) : AppCompatEditText(context, attrs, defStyleAttr)

{
    init {
        setPadding(40, 40, 30, 40)
        hint = "Password"
        addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, start: Int, count:  Int, after : Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()
                if (password.length<8){
                    error = "Password must be at least 8 characters"
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }
}