package com.nuda.nudaclient.data.remote.dto.members

import com.nuda.nudaclient.data.remote.dto.common.Me

data class MembersUserInfoResponse(
    val me: Me,
    val survey: Survey
    ) {
        data class Survey(
            val keywords: List<String>
        )
    }
