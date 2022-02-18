package com.amazing.constant

class Constant {

    // put the constant value here, if you are interesting in how the encrypt the constant value, we can discuss on the interview, I can share my experience.

    fun getApiTeacherSchedule(teacherName: String, startAt: String) = "https://en.amazingtalker.com/v1/guest/teachers/$teacherName/schedule?started_at=$startAt"

    // in order to prevent user change local time to access earlier time, get the real time from server
    // but I don't have server for this homework, so fetch the real time from google is a workaround
    fun getApiCurrentTime() = "https://www.google.com/"
}
