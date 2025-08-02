package com.vibecoder.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns

object PhoneUtils {
    
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotBlank() && 
               (Patterns.PHONE.matcher(phoneNumber).matches() || 
                phoneNumber.matches(Regex("^[+]?[0-9]{10,15}$")))
    }
    
    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except '+'
        val cleaned = phoneNumber.replace(Regex("[^+0-9]"), "")
        
        return when {
            cleaned.length == 10 -> {
                // Format as (XXX) XXX-XXXX
                "${cleaned.substring(0, 3)}-${cleaned.substring(3, 6)}-${cleaned.substring(6)}"
            }
            cleaned.length == 11 && cleaned.startsWith("1") -> {
                // Format as +1 (XXX) XXX-XXXX
                "+1 ${cleaned.substring(1, 4)}-${cleaned.substring(4, 7)}-${cleaned.substring(7)}"
            }
            cleaned.startsWith("+") -> {
                // Keep international format as is
                cleaned
            }
            else -> phoneNumber // Return original if no standard format matches
        }
    }
    
    fun makePhoneCall(context: Context, phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun dialPhoneNumber(context: Context, phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun cleanPhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^+0-9]"), "")
    }
}