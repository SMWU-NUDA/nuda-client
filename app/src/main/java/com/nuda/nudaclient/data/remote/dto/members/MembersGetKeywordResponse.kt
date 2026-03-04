package com.nuda.nudaclient.data.remote.dto.members

import com.nuda.nudaclient.data.remote.dto.common.Me

data class MembersGetKeywordResponse(
    val irritationLevel: String,
    val scent: String,
    val adhesion: String,
    val thickness: String,
    val me: Me
)