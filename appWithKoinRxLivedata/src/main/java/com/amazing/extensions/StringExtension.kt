package com.amazing.extensions

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

val String.isJson: Boolean
    get() {
        try {
            JSONObject(this)
        } catch (ex: JSONException) {
            try {
                JSONArray(this)
            } catch (ex1: JSONException) {
                return false
            }
        }
        return true
    }
