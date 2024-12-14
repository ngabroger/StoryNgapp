package com.ngabroger.storyngapp.costumview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.textfield.TextInputLayout
import com.ngabroger.storyngapp.R

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {
    private var inputLayout: TextInputLayout? = null
    private var validationType: ValidationType? = null

    enum class ValidationType { EMAIL, PASSWORD, NAME }

    init {
        setPadding(40, 40, 20, 40)
        textAlignment = TEXT_ALIGNMENT_VIEW_START
        gravity = Gravity.CENTER_VERTICAL
        isFocusable = true
        isClickable = true
        isFocusableInTouchMode = true

        textSize = 16f
    }

    fun setValidationType(type: ValidationType) {
        validationType = type
        addValidation()
    }

    fun setInputLayout(layout: TextInputLayout) {
        inputLayout = layout
    }

    private fun addValidation() {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                inputLayout?.error = when (validationType) {
                    ValidationType.EMAIL -> validateEmail(s)
                    ValidationType.PASSWORD -> validatePassword(s)
                    ValidationType.NAME -> validateName(s)
                    else -> null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }
        })
    }

    private fun validateEmail(s: CharSequence?): String? {
        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]+$"
        return when {
            s.isNullOrEmpty() -> context.getString(R.string.email_cannot_be_empty)
            !s.matches(Regex(emailPattern)) -> context.getString(R.string.email_is_not_valid)
            else -> null
        }
    }

    private fun validatePassword(s: CharSequence?): String? {
        return when {
            s.isNullOrEmpty() -> context.getString(R.string.password_cannot_be_empty)
            s.length < 8 -> context.getString(R.string.password_must_be_at_least_8_characters)
            else -> null
        }
    }

    private fun validateName(s: CharSequence?): String? {
        return when {
            s.isNullOrEmpty() -> context.getString(R.string.name_cannot_be_empty)
            else -> null
        }
    }
}