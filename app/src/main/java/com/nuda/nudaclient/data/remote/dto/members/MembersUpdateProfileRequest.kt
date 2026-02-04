package com.nuda.nudaclient.data.remote.dto.members

data class MembersUpdateProfileRequest(
    val username: String,
    val nickname: String,
    val email: String,
    val currentPassword: String,
    val newPassword: String
)