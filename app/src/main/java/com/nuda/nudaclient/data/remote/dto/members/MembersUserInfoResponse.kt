package com.nuda.nudaclient.data.remote.dto.members

import com.nuda.nudaclient.data.remote.dto.common.Me

data class MembersUserInfoResponse(
    val me: Me,
    val keywords: List<String>
    )
